drop database if exists PhotoApp;
create database PhotoApp;
use PhotoApp;

-- 01_create_tables.sql
create table Tags(
	TagID int auto_increment primary key,
	Title varchar(64) not null unique,
	TagColor mediumint unsigned default 0xFFFFFF, -- Default to 'white'
	TagType varchar(64)
);

# create the user table
create table Users (
	UserId int auto_increment,
	Name varchar(100) not null,
	Email varchar(100) null,
	DateJoined datetime default current_timestamp(),
	constraint users_pk primary key (UserId),
	constraint user_name_uk unique (Name)
);

# create the camera table
create table Camera (
	CameraId int auto_increment,
	Brand varchar(100) not null,
	Model varchar(100) not null,
	SerialNumber varchar(100),
	constraint camera_pk primary key (CameraId),
	constraint camera_serialnumber_uk unique (SerialNumber),
	constraint camera_brand_model_uk unique (Brand, Model)
);

# create the photo table 
create table Photo(
	PhotoID int auto_increment primary key,
	CameraID int,
	Filepath varchar(256) unique,
	FileSize bigint,
	Latitude decimal(8,6),
	Longitude decimal(9,6),
	ImageWidth int unsigned,
	ImageHeight int unsigned,
	DateTimeTaken datetime,
	DateTimeAdded datetime default current_timestamp,
	
	foreign key (CameraID) 
		references Camera(CameraID)
);

# create the album table
CREATE TABLE Album (
	AlbumID INT AUTO_INCREMENT PRIMARY KEY,
	OwnerID INT,
	AlbumName VARCHAR(255),
	AlbumDescription TEXT,
	AlbumType Enum('Library', 'Auto', 'User'),
	CreatedAt DATETIME default current_timestamp,
	AlbumUpdated DATETIME,

	FOREIGN KEY (OwnerID)
		REFERENCES Users(UserID)
			on delete cascade
			on update cascade
);
	
-- 02_create_intersection_tables.sql

# PhotoTag_Int - Salina
create table Photo_Tag (
    PhotoID int,
    TagID int,
    primary key (PhotoID, TagID),
    foreign key (PhotoID) references Photo(PhotoID)
      on delete cascade,
    foreign key (TagID) references Tags(TagID)
      on delete cascade
);

# AlbumPhoto_Int - Emily
Create table Album_Photo (
  AlbumID INT, 
  PhotoID int,
  primary key (AlbumID, PhotoID),
  foreign key (AlbumID) References Album(AlbumID)
  	on delete cascade,
  foreign key (PhotoID) References Photo(PhotoID)
    on delete cascade
);

# Album_Shares - Izzac
create table Album_Shares(
  ReceiverID int,
  AlbumID int,
  
  primary key (ReceiverID, AlbumID),
  
  foreign key (ReceiverID) 
    references Users(UserID)
       on delete cascade,
  foreign key (AlbumID)
    references Album(AlbumID)
       on delete cascade
);
  


-- Functions ----------
-- returns the number of photos in a given album	
delimiter $$ -- 03_fnsptg_salina.sql
create function  if not exists fn_PhotoCount(album_id int)
returns int
deterministic
begin
    declare photo_total int;

    select count(*)
    into photo_total
    from Album_Photo
    where AlbumID = album_id;

    return photo_total;
end $$
delimiter ;

DELIMITER $$ -- 04_fnsptg_emily.sql
CREATE FUNCTION if not exists fn_GetYearAlbumID (
    p_UserID INT,
    p_Year INT
)
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE v_AlbumID INT;

    SELECT AlbumID INTO v_AlbumID
    FROM Album
    WHERE OwnerID = p_UserID
      AND AlbumType = 'Auto'
      AND AlbumName = CONCAT(p_Year, ' Photos')
    LIMIT 1;

    RETURN v_AlbumID;
END $$
DELIMITER ;

delimiter $$ -- 05_fnsptg_izzac.sql
-- gets the AlbumID for the 'Library' album for the given user
create function if not exists fn_GetLibraryID(userID int) returns int 
	READS SQL DATA 
	begin  
		declare libraryID int;
		select AlbumID into libraryID
			from Album A 
			where A.OwnerID = userID
			and A.AlbumType = 'Library'
		limit 1;
	  	return libraryID;
	end $$ 
delimiter ;

-- Stored Procedures --

/*
	stored procedure: remove_photo_from_album
	purpose is to remove a specific photo from a specific album.
	parameters: albumid, photoid
	validation: ensures the album is not a protected system album, such as 'library' or 'auto', before removing the photo.
*/	
delimiter $$ -- 03_fnsptg_salina.sql
create procedure if not exists sp_RemovePhotoFromAlbum(
    in p_albumid int,
    in p_photoid int
)
begin
    delete from Album_Photo
    where AlbumID = p_albumid
      and PhotoID = p_photoid
      and p_albumid not in (
          select AlbumID
          from Album
          where AlbumType in ('Library', 'Auto')
      );
end $$
delimiter ;

DELIMITER $$ -- 04_fnsptg_emily.sql 
Create Procedure if not exists sp_FindOrCreateCamera(
    IN p_Brand VARCHAR(100),
    IN p_Model VARCHAR(100),
    IN p_SerialNumber VARCHAR(100),
    OUT p_CameraID INT
    )
Begin
    -- Try to find existing camera using SerialNumber
    SELECT CameraID INTO p_CameraID
    FROM Camera
    WHERE (SerialNumber <=> p_SerialNumber AND p_SerialNumber IS NOT NULL)
      OR (Brand = p_Brand AND Model = p_Model)
    LIMIT 1;

    -- If not found, insert new camera
    IF p_CameraID IS NULL THEN
        INSERT INTO Camera (Brand, Model, SerialNumber)
        VALUES (p_Brand, p_Model, p_SerialNumber);
        -- Get the new CameraID
        select CameraID into p_CameraID 
        from Camera 
        where Brand <=> p_Brand 
    	  and Model <=> p_Model 
          and SerialNumber <=> p_SerialNumber;
    END IF;
END $$
DELIMITER ;

delimiter $$ -- 05_fnsptg_izzac.sql
create procedure if not exists sp_InsertPhoto(
	-- passed from application
	in u_UserID int,
	-- Photo data (Some may be null)
	in p_Filepath varchar(256),
	in p_FileSize bigint,
	in p_Latitude decimal(8,6),
	in p_Longitude decimal(9,6),
	in p_ImageWidth int unsigned,
	in p_ImageHeight int unsigned,
	in p_DateTimeTaken datetime,
	-- Camera data (Might be null if photo has none)
	in c_Brand varchar(100),
	in c_Model varchar(100),
	in c_SerialNumber varchar(100)
)
begin 
	declare c_CameraID int;
	-- not sure if needed but since this effects multiple tables, seems appropriate
	declare exit handler for SQLEXCEPTION
		begin
			rollback;
		end;
	
	start transaction;
		-- check if a matching Camera exists already
		if c_Brand is not null and c_Model is not null then
			call sp_FindOrCreateCamera(c_Brand, c_Model, c_SerialNumber, c_CameraID); -- sets c_CameraID
		end if;
	  
	  -- set user session variable for the trigger 
	  SET @CurrentUserID = u_UserID;
		insert into Photo 
			(CameraID, Filepath, FileSize, Latitude, Longitude, ImageWidth, ImageHeight, DateTimeTaken)
		values
			(c_CameraID, p_Filepath, p_FileSize, p_Latitude, p_Longitude, p_ImageWidth, p_ImageHeight, p_DateTimeTaken);
	  
	  select * 
	  from Photo 
	  where PhotoID = last_insert_id();
	commit;
end $$
delimiter ;

-- Triggers -----------
/* 
	Trigger Name is album_update
	Purpose is to automatically updates the album's last modified timestamp whenever a new photo is added to an album.
	Event: after insert on album_photo
*/
delimiter $$ -- 03_fnsptg_salina.sql
create trigger if not exists tg_AlbumUpdate
after insert on Album_Photo
for each row
begin
    update Album
    set albumupdated = current_timestamp()
    where AlbumID = new.AlbumID;
end $$

delimiter ; -- 

DELIMITER $$ -- 04_fnsptg_emily.sql
CREATE TRIGGER if not exists tg_NewPhoto
AFTER INSERT ON Photo
FOR EACH ROW
BEGIN
    DECLARE v_UserID INT;
    DECLARE v_LibraryAlbumID INT;
    DECLARE v_YearAlbumID INT;
    DECLARE v_Year INT;

    -- Get user from session (set in sp_InsertPhoto)
    SET v_UserID = @CurrentUserID;
    -- Check userID valid
    IF v_UserID IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'UserID not set before inserting photo';
    END IF;
    -- Get year
    SET v_Year = YEAR(COALESCE(NEW.DateTimeTaken, NOW()));
    -- Library album
    SELECT fn_GetLibraryID(@CurrentUserID) INTO v_LibraryAlbumID;
    IF v_LibraryAlbumID IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No Library Album found UserID';
    END IF;
    -- Year album
    SELECT fn_GetYearAlbumID(@CurrentUserID, v_Year) INTO v_YearAlbumID;

    IF v_YearAlbumID IS NULL THEN
        INSERT INTO Album (OwnerID, AlbumName, AlbumType)
        VALUES (v_UserID, CONCAT(v_Year, ' Photos'), 'Auto');
        SET v_YearAlbumID = LAST_INSERT_ID();
    END IF;
    -- Insert into junction table
    INSERT IGNORE INTO Album_Photo (AlbumID, PhotoID)
    VALUES (v_LibraryAlbumID, NEW.PhotoID);
    INSERT IGNORE INTO Album_Photo (AlbumID, PhotoID)
    VALUES (v_YearAlbumID, NEW.PhotoID);
END $$
DELIMITER ;

delimiter $$ -- 05_fnsptg_izzac.sql
create trigger if not exists tg_NewUser
  after insert 
  on Users for each row
    begin
	  -- 'Library' album type stores all photos uploaded by a User. 
      insert into Album(OwnerID, AlbumName, AlbumDescription, AlbumType) values 
	  	(new.userID, concat(new.Name,"\'s Library"), 'All uploaded photos.', 'Library');
    end $$
delimiter ;

-- check for all procedures and triggers
select *
from information_schema.TRIGGERS
where trigger_schema = 'PhotoApp';
select *
from information_schema.ROUTINES
where ROUTINE_SCHEMA = 'PhotoApp';

# -- Data Inserts -- #

# inserts from inserts_sql_Emily.sql
-- Insert to user table
INSERT INTO Users (Name, Email)
VALUES
    ('Bill_Nye', 'ScienceGuy@example.com'),
    ('Steve_Irwin', 'AustralianMan@example.com'),
    ('George_Lopez', 'LowRider@example.com'),
    ('Elton_John', 'TinyDancer@example.com'),
    ('Robin_Williams', 'carpeperdiem@example.com');
    
-- Insert to tags table
INSERT INTO Tags (Title, TagColor, TagType)
VALUES 
    ('Vacation', 0xFFD700, 'Category'),
    ('Family - Emily', 0xFF69B4, 'People'),
    ('Nature', 0x32CD32, 'Category'),
    ('Work', 0x1E90FF, 'Category'),
    ('Friends - Emily', 0xFFA500, 'People');
    
-- Insert to camera table
INSERT INTO Camera (Brand, Model, SerialNumber)
VALUES
    ('Canon', 'EOS Rebel T7', 'SN1001'),
    ('Nikon', 'D3500', 'SN1002'),
    ('Sony', 'Alpha a6000', 'SN1003'),
    ('Fujifilm', 'X-T30', 'SN1004'),
    ('Panasonic', 'Lumix GH5', 'SN1005');
    
-- Insert to album table
INSERT INTO Album (OwnerID, AlbumName, AlbumDescription, AlbumType, CreatedAt, AlbumUpdated)
VALUES 
(1, 'Vacation to Mexico', 'Photos from cruise trip', 'User', NOW(), NOW()),
(2, 'Pets', 'Pics of my furry friends', 'Auto', NOW(), NOW()),
(3, 'Friends', 'Pics of my besties', 'Library', NOW(), NOW()),
(1, 'Family Album', 'Family images', 'User', NOW(), NOW()),
(2, 'Me', 'Selfies and other pictures of me', 'Auto', NOW(), NOW());

SELECT UserID INTO @Bill_Nye FROM Users WHERE Name = 'Bill_Nye';
SELECT UserID INTO @Steve_Irwin FROM Users WHERE Name = 'Steve_Irwin';
SELECT UserID INTO @George_Lopez FROM Users WHERE Name = 'George_Lopez';
SELECT UserID INTO @Elton_John FROM Users WHERE Name = 'Elton_John';
SELECT UserID INTO @Robin_Williams FROM Users WHERE Name = 'Robin_Williams';

-- Emily's insert into photo
call sp_InsertPhoto(@Bill_Nye, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_4850.jpeg', 
 2988641, NULL, NULL, 4032, 3024, '2024-04-20 18:53:09', 
 'Apple', 'iPhone 13 Pro Max', NULL);

call sp_InsertPhoto(@Elton_John, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_6011.jpeg', 
 8163523, NULL, NULL, 8914, 3910, '2025-11-07 09:49:17', 
 'Apple', 'iPhone 11', NULL);

call sp_InsertPhoto(@Steve_Irwin, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_3916.jpeg', 
 2323241, NULL, NULL, 4000, 3000, NULL, 
 NULL, NULL, NULL);

call sp_InsertPhoto(@George_Lopez, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_5978.jpeg', 
 1434221, NULL, NULL, 3088, 2316, '2025-11-04 15:34:35', 
 'Apple', 'iPhone 11', NULL);

call sp_InsertPhoto(@Robbin_Williams, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_1403.jpeg', 
 2105270, 39.060300, -95.646089, 4032, 3024, '2023-12-17 17:33:19', 
 'Apple', 'iPhone 12 Pro Max', NULL);

call sp_InsertPhoto(@Bill_Nye, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_5979.jpeg', 
 3210044, NULL, NULL, 4032, 3024, '2025-11-05 07:33:28', 
 'Apple', 'iPhone 11', NULL);

call sp_InsertPhoto(@Robbin_Williams, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_5579.jpeg', 
 2003150, NULL, NULL, 3088, 2316, '2025-03-18 13:59:46', 
 'Apple', 'iPhone 11', NULL);

call sp_InsertPhoto(@Steve_Irwin, '/home/soms/Vault/DBMS Project Photos/Emily/IMG_1845.jpeg', 
 841901, 39.060342, -95.646150, 1612, 2222, '2024-09-28 20:16:35', 
 'Apple', 'iPhone 12 Pro Max', NULL);

# inserts from data_queries_salina.sql
insert into Users (Name) 
	values ('Salina'), ('Leo'), ('Emma'), ('Jonah'), ('Amy');

select UserID into @salinaUserID
from Users 
where Name = 'Salina';

select @salinaUserID;

insert ignore into Camera (Brand, Model, SerialNumber) 
	values ('Apple', 'iPhone XS Max', NULL),
		   ('Apple', 'iPhone 16', NULL),
		   ('Canon', 'EOS R50', NULL),
		   ('Sony', 'Alpha A7 III', NULL),
		   ('Nikon', 'D3500', NULL);

insert into Tags (Title, TagColor, TagType) 
	values 
		('Travel', 0x00FF00, 'Custom'),
		('Friends - Salina',0xFF00FF, 'Custom'),
		('Campus', 0xF0FFFE, 'Custom'),
		('Food',   0xEEB00F, 'Custom'),
		('Family - Salina', 0xAEAEAE, 'Custom'); 

call sp_InsertPhoto(@salinaUserID, '/home/soms/Vault/DBMS Project Photos/Salina/IMG_3941.jpg', 
	3398560, NULL, NULL, 4032, 3024, '2023-12-26 13:26:39', 
	'Apple', 'iPhone XS Max', NULL);

call sp_InsertPhoto(@salinaUserID, '/home/soms/Vault/DBMS Project Photos/Salina/IMG_4125.jpg', 
	4485951, NULL, NULL, 4032, 3024, '2023-05-03 14:27:27', 
	'Apple', 'iPhone XS Max', NULL);

call sp_InsertPhoto(@salinaUserID, '/home/soms/Vault/DBMS Project Photos/Salina/IMG_4635.jpg', 
	2386504, NULL, NULL, 4032, 3024, '2023-05-04 13:39:38', 
	'Apple', 'iPhone XS Max', NULL);

call sp_InsertPhoto(@salinaUserID, '/home/soms/Vault/DBMS Project Photos/Salina/IMG_3347.jpg', 
	1241136, NULL, NULL, 1242, 2208, NULL, 
	NULL, NULL, NULL);

call sp_InsertPhoto(@salinaUserID, '/home/soms/Vault/DBMS Project Photos/Salina/IMG_3347.jpg', 
	1241136, NULL, NULL, 1242, 2208, NULL, 
	'Apple', 'iPhone 16', NULL);
    
insert into Album (OwnerID, AlbumName, AlbumDescription, AlbumType)
	values
		(@salinaUserID, 'Travel Album', 'Travel photos collection', 'User'),
		(@salinaUserID, 'Campus Album', 'Campus memories', 'User'),
		(@salinaUserID, 'Friends Album', 'Photos with friends', 'User'),
		(@salinaUserID, 'Food Album', 'Food pictures', 'User'),
		(@salinaUserID, 'Family Album', 'Family memories', 'User');

select TagID into @travelTagID 
from Tags
where Title = 'Travel';

select TagID into @friendsTagID 
from Tags
where Title = 'Friends - Salina';

select TagID into @campusTagID 
from Tags
where Title = 'Campus';

select TagID into @foodTagID 
from Tags
where Title = 'Food';

select TagID into @familyTagID 
from Tags
where Title = 'Family - Salina';

select AlbumID into @travelAlbumID
from Album
where AlbumName = 'Travel Album'
and OwnerID = @salinaUserID;

select AlbumID into @campusAlbumID
from Album
where AlbumName = 'Campus Album'
and OwnerID = @salinaUserID;

select AlbumID into @friendsAlbumID
from Album
where AlbumName = 'Friends Album'
and OwnerID = @salinaUserID;

select AlbumID into @foodAlbumID
from Album
where AlbumName = 'Food Album'
and OwnerID = @salinaUserID;

select AlbumID into @familyAlbumID
from Album
where AlbumName = 'Family Album'
and OwnerID = @salinaUserID;

insert into Photo_Tag (PhotoID, TagID)
	select PhotoID, @travelTagID
	from Photo P
	where P.Filepath like '%IMG_3941%';

insert into Photo_Tag (PhotoID, TagID)
	select PhotoID, @friendsTagID
	from Photo P
	where P.Filepath like '%IMG_4125%';

insert into Photo_Tag (PhotoID, TagID)
	select PhotoID, @campusTagID
	from Photo P
	where P.Filepath like '%IMG_4635%';

insert into Photo_Tag (PhotoID, TagID)
	select PhotoID, @foodTagID
	from Photo P
	where P.Filepath like '%IMG_3347%';

insert into Photo_Tag (PhotoID, TagID)
	select PhotoID, @familyTagID
	from Photo P
	where P.Filepath like '%IMG_3348%';

insert into Album_Photo (AlbumID, PhotoID)
	select @travelAlbumID, PhotoID
	from Photo P
	where P.Filepath like '%IMG_3941%';

insert into Album_Photo (AlbumID, PhotoID)
	select @friendsAlbumID, PhotoID
	from Photo P
	where P.Filepath like '%IMG_4125%';

insert into Album_Photo (AlbumID, PhotoID)
	select @campusAlbumID, PhotoID
	from Photo P
	where P.Filepath like '%IMG_4635%';

insert into Album_Photo (AlbumID, PhotoID)
	select @foodAlbumID, PhotoID
	from Photo P
	where P.Filepath like '%IMG_3347%';

insert into Album_Photo (AlbumID, PhotoID)
	select @familyAlbumID, PhotoID
	from Photo P
	where P.Filepath like '%IMG_3348%';

insert into Album_Shares (ReceiverID, AlbumID)
	values
		(2, @travelAlbumID),
		(3, @campusAlbumID),
		(4, @friendsAlbumID),
		(5, @foodAlbumID),
		(2, @familyAlbumID);

# inserts from data_queries_izzac.sql
insert into Users (Name) 
	values ('Izzac'), ('Alice'), ('Brandon'), ('Bob'), ('Eve');

select UserID into @izzacID
from Users 
where Name = 'Izzac';
select @izzacID;

insert ignore into Camera (Brand, Model, SerialNumber) 
	values ('Google', 'Pixel 10', NULL),
		   ('Google', 'Pixel 8', NULL), -- Shouldn't get associated with any photo inserts
		   ('Apple', 'iPhone 6s', NULL),
		   ('Apple', 'iPhone XR', NULL),
		   ('NIKON CORPORATION', 'NIKON D300', NULL);

insert into Tags (Title, TagColor, TagType) 
	values 
		('Pets',    0x00FF00, 'Custom'),
		('Flowers', 0xFF00FF, 'Custom'),
		('Cats',    0x00FFFF, 'Custom'),
		('Zox',	    0xBEEEEF, 'Custom'),
		('Old Pics',0xFF00FF, 'Custom'); 

call sp_InsertPhoto(@izzacID, '/home/soms/Vault/DBMS Project Photos/Izzac/20260502_153441Z.jpg', 
	6219023, NULL, NULL, 4000, 3000, '2026-05-02 10:34:41', 
	'Google', 'Pixel 10', NULL);

call sp_InsertPhoto(@izzacID, '/home/soms/Vault/DBMS Project Photos/Izzac/20260502_153450Z.jpg', 
	5226087, NULL, NULL, 4000, 3000, '2026-05-02 10:34:50', 
	'Google', 'Pixel 10', NULL);

call sp_InsertPhoto(@izzacID, '/home/soms/Vault/DBMS Project Photos/Izzac/Zox/20171225_052343250_iOS-X4.jpg', 
	914784, NULL, NULL, 2048, 1872, '2017-12-24 21:23:43', 
	'Apple', 'iPhone 6s', NULL);

call sp_InsertPhoto(@izzacID, '/home/soms/Vault/DBMS Project Photos/Izzac/Zox/IMG_0764.jpg', 
	305595, 45.480800, -122.388725, 1942, 1944, '2016-05-13 12:17:32', 
	'Apple', 'iPhone 6s', NULL);

call sp_InsertPhoto(@izzacID, '/home/soms/Vault/DBMS Project Photos/Izzac/Zox/zox.jpg', 
	661589, NULL, NULL, 1814, 1617, '2007-10-16 21:33:22', 
	NULL, NULL, NULL);

call sp_InsertPhoto(@izzacID, '/home/soms/Vault/DBMS Project Photos/Izzac/Zox/DSC_0962MOD-X4.jpg', 
	462539, NULL, NULL, 2048, 1360, '2009-12-06 21:24:09', 
	'NIKON CORPORATION', 'NIKON D300', NULL);

call sp_InsertPhoto(@izzacID, '/home/soms/Vault/DBMS Project Photos/Izzac/Zox/DSC_3874_1861-X4.jpg', 
	183621, NULL, NULL, 2048, 2044, '2010-06-16 11:41:11', 
	'NIKON CORPORATION', 'NIKON D300', NULL);

call sp_InsertPhoto(@izzacID, '/home/soms/Vault/DBMS Project Photos/Izzac/Some Flowers/2021042419182066--8274272085398414930-F76FFEA3-55B4-418F-B79D-40ECB3B4C878-X4.jpg', 
	220092, NULL, NULL, 1639, 2048, '2021-04-24 11:13:19', 
	'Apple', 'iPhone XR', NULL);


# -- Database SQL Queries -- # 

# queries from sql_statements_emily.sql
-- Combine camera and photo data
SELECT P.PhotoID, P.Filepath, C.Brand, C.Model
FROM Photo P
LEFT JOIN Camera C ON P.CameraID = C.CameraID;

-- Count the # of photos in an album
SELECT A.AlbumName, COUNT(AP.PhotoID) AS PhotoCount
FROM Album A
LEFT JOIN Album_Photo AP ON A.AlbumID = AP.AlbumID
GROUP BY A.AlbumID, A.AlbumName;

-- Find photos from a specific year
SELECT PhotoID, Filepath, DateTimeTaken
FROM Photo
WHERE YEAR(DateTimeTaken) = 2024;

-- Update album description for all ‘Library’ albums
UPDATE Album
SET AlbumDescription = 'Default library album for user photos'
WHERE AlbumType = 'Library';

-- Delete photos not linked to any album
DELETE FROM Photo
WHERE PhotoID NOT IN (
    SELECT PhotoID FROM Album_Photo
);

# queries from data_queries_salina.sql

-- Query 1
select * from Photo;

-- Query 2
select P.Filepath, C.Brand, C.Model
from Photo P
left join Camera C on P.CameraID = C.CameraID;

-- Query 3
select AlbumName, fn_PhotoCount(AlbumID) as PhotoCount
from Album
where OwnerID = @salinaUserID;

-- Query 4
update Tags 
	set Title = 'Trips'
where Title = 'Travel';

-- Query 5
delete from Album_Shares
where ReceiverID = 999;

# queries from data_queries_izzac.sql
-- select a random photo
select PhotoID into @randomPhotoID 
from Photo 
order by Rand()
limit 1;

select TagID into @oldPicsTagID from Tags where Title = 'Old Pics';
select TagID into @flowerTagID from Tags where Title = 'Flowers';
select TagID into @zoxTagID from Tags where Title = 'Zox';
select TagID into @petTagID from Tags where Title = 'Pets';
select TagID into @catTagID from Tags where Title = 'Cats';

select PhotoID
from Photo
where DateTimeTaken < '2017-01-01 00:00:00';

-- tags all photos taken before 2017 with 'Old Pics'
insert into Photo_Tag (PhotoID, TagID)
	select PhotoID, @oldPicsTagID
	from Photo P
	where P.DateTimeTaken < '2017-01-01 00:00:00';

insert into Photo_Tag (PhotoID, TagID)
	select PhotoID, @flowerTagID
	from Photo P 
    where P.Filepath like '%Flowers%';

select * from Photo_Tag;

-- selects photos with any tags
select P.Filepath from Photo P where exists(
	select distinct PT.PhotoID 
	from Photo_Tag PT 
	where PT.PhotoID = P.PhotoID
);

-- format list of all tags for each Photo file, 
select P.Filepath, group_concat(T.Title)
from (Photo P join Photo_Tag PT on P.PhotoID = PT.PhotoID) 
right join Tags T on PT.TagID = T.TagID
group by P.Filepath;

-- rename 'old pics' tag to something more accurate
update Tags 
	set Title = 'Pre-2017'
where TagID = @oldPicsTagID;

-- tags all photos with 'zox' in their filepath with the 'Zox', 'Cats' and 'Pets' tags
create temporary table ZoxPix as
	select PhotoID 
	from Photo P
    where P.Filepath like '%zox%';

insert into Photo_Tag (PhotoID, TagID)
	select PhotoID, @zoxTagID
	from ZoxPix;

insert into Photo_Tag (PhotoID, TagID)
	select PhotoID, @petTagID
	from ZoxPix;

insert into Photo_Tag (PhotoID, TagID)
	select PhotoID, @catTagID
	from ZoxPix;

drop temporary table ZoxPix;

-- create new tag for photos with/without GPS coords
insert into Tags (Title) values ('Has GPS');
set @hasGPSTagID = last_insert_id();
insert into Tags (Title) values ('No GPS');
set @noGPSTagID = last_insert_id();

select * from Tags where TagID = @noGPSTagID;

-- tag the Photos with and without GPS
insert ignore into Photo_Tag (PhotoID, TagID)
	select PhotoID, @hasGPSTagID
	from Photo P
	where P.Latitude is not null 
	  and P.Longitude is not null;

insert ignore into Photo_Tag (PhotoID, TagID)
	select PhotoID, @noGPSTagID
	from Photo P
	where P.Latitude is null 
	  and P.Longitude is null;

-- Delete a random photo from the database >:)
delete from Photo
where PhotoID = @randomPhotoID;

-- format list of all tags for each Photo file again to show diff
select coalesce(P.Filepath, 'None'), group_concat(T.Title)
from (Photo P join Photo_Tag PT on P.PhotoID = PT.PhotoID) 
right join Tags T on PT.TagID = T.TagID
group by P.Filepath;

select concat(J.Name, '\'s - ', J.AlbumName) as DisplayTitle, count(*) as PhotoCount
from Album_Photo AP join (
	select Name, AlbumName, AlbumID 
	from Album A join Users U
		on A.OwnerID = U.UserID
) J on J.AlbumID = AP.AlbumID
group by DisplayTitle
order by PhotoCount desc;


-- queries for cleaning up + prep for application demo

select * 
from Album A
where not exists (
	select * 
	from Album_Photo AP
	where A.AlbumID = AP.AlbumID
);

insert into Users (Name) 
	values ('Emily');

select UserID into @emilyUserID 
from Users 
where Name = 'Emily';

select fn_GetLibraryID(@emilyUserID) into @emilyLibraryID;

create temporary table E as
select distinct PhotoID
	from Album_Photo AP
		join (select distinct OwnerID, AlbumID from Album) Q
			on Q.AlbumID = AP.AlbumID
		join (select UserID, Name from Users where Email is not null) J
			on J.UserID = Q.OwnerID;

insert ignore into Album_Photo (AlbumID, PhotoID)
	select @emilyLibraryID, PhotoID
	from E;

drop temporary table E;
	
delete from Album A
where not exists (
	select * 
	from Album_Photo AP
	where A.AlbumID = AP.AlbumID
);

delete from Users U
where not exists(
	select distinct PhotoID
	from Album_Photo
	where AlbumID = fn_GetLibraryID(U.UserID)
);

