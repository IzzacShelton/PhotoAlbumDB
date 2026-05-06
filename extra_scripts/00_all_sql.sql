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

# AlbumShares - Izzac
CREATE TABLE AlbumShares(
  ReceiverID int,
  AlbumID int,
  
  PRIMARY KEY (ReceiverID, AlbumID),
  
  FOREIGN KEY (ReceiverID) 
    REFERENCES Users(UserID),
  FOREIGN KEY (AlbumID)
    REFERENCES Album(AlbumID)
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
    WHERE SerialNumber <=> p_SerialNumber
      OR (Brand = p_Brand AND Model = p_Model)
    LIMIT 1;

    -- If not found, insert new camera
    IF p_CameraID IS NULL THEN
        INSERT INTO Camera (Brand, Model, SerialNumber)
        VALUES (p_Brand, p_Model, p_SerialNumber);
        -- Get the new CameraID
        SET p_CameraID = LAST_INSERT_ID();
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
--     IF v_LibraryAlbumID IS NULL THEN
--         INSERT INTO Album (OwnerID, AlbumName, AlbumType)
--         VALUES (v_UserID, concat(new.Name,"\'s Library"), 'All uploaded photos.', 'Library');
-- 
--         SET v_LibraryAlbumID = LAST_INSERT_ID();
--     END IF;
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

-- Data Inserts
