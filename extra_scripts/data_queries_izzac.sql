insert into Users (Name) 
	values ('Izzac'), ('Alice'), ('Brandon'), ('Bob'), ('Eve'), ('soms');

select UserID into @userID
from Users 
where Name = 'soms';
select @userID;

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

call sp_InsertPhoto(@userID, '/home/soms/Vault/DBMS Project Photos/Izzac/20260502_153441Z.jpg', 
	6219023, NULL, NULL, 4000, 3000, '2026-05-02 10:34:41', 
	'Google', 'Pixel 10', NULL);

call sp_InsertPhoto(@userID, '/home/soms/Vault/DBMS Project Photos/Izzac/20260502_153450Z.jpg', 
	5226087, NULL, NULL, 4000, 3000, '2026-05-02 10:34:50', 
	'Google', 'Pixel 10', NULL);

call sp_InsertPhoto(@userID, '/home/soms/Vault/DBMS Project Photos/Izzac/Zox/20171225_052343250_iOS-X4.jpg', 
	914784, NULL, NULL, 2048, 1872, '2017-12-24 21:23:43', 
	'Apple', 'iPhone 6s', NULL);

call sp_InsertPhoto(@userID, '/home/soms/Vault/DBMS Project Photos/Izzac/Zox/IMG_0764.jpg', 
	305595, 45.480800, -122.388725, 1942, 1944, '2016-05-13 12:17:32', 
	'Apple', 'iPhone 6s', NULL);

call sp_InsertPhoto(@userID, '/home/soms/Vault/DBMS Project Photos/Izzac/Zox/zox.jpg', 
	661589, NULL, NULL, 1814, 1617, '2007-10-16 21:33:22', 
	NULL, NULL, NULL);

call sp_InsertPhoto(@userID, '/home/soms/Vault/DBMS Project Photos/Izzac/Zox/DSC_0962MOD-X4.jpg', 
	462539, NULL, NULL, 2048, 1360, '2009-12-06 21:24:09', 
	'NIKON CORPORATION', 'NIKON D300', NULL);

call sp_InsertPhoto(@userID, '/home/soms/Vault/DBMS Project Photos/Izzac/Zox/DSC_3874_1861-X4.jpg', 
	183621, NULL, NULL, 2048, 2044, '2010-06-16 11:41:11', 
	'NIKON CORPORATION', 'NIKON D300', NULL);

call sp_InsertPhoto(@userID, '/home/soms/Vault/DBMS Project Photos/Izzac/Some Flowers/2021042419182066--8274272085398414930-F76FFEA3-55B4-418F-B79D-40ECB3B4C878-X4.jpg', 
	220092, NULL, NULL, 1639, 2048, '2021-04-24 11:13:19', 
	'Apple', 'iPhone XR', NULL);


-- select a random photo
select PhotoID into @randomPhotoID 
from Photo 
order by Rand()
limit 1;

select TagID into @oldPicsTagID 
from Tags
where Title = 'Old Pics';

select TagID into @flowerTagID 
from Tags
where Title = 'Flowers';

select TagID into @zoxTagID 
from Tags
where Title = 'Zox';

select TagID into @petTagID 
from Tags
where Title = 'Pets';

select TagID into @catTagID 
from Tags
where Title = 'Cats';

select @oldPicsTagID;

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

-- format list of all Photos for each Tag, with 'None' displayed for 
select T.Title, coalesce(group_concat(substring_index(P.Filepath, '/', -1)), 'None') as TaggedFiles
from (Photo P join Photo_Tag PT on P.PhotoID = PT.PhotoID) 
right join Tags T on PT.TagID = T.TagID
group by T.Title;

select * from Tags;

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

-- delete Photos with no Tags
delete from Photo P
where not exists(
	select distinct PT.PhotoID 
	from Photo_Tag PT 
	where PT.PhotoID = P.PhotoID
);

-- format list of all tags for each Photo file again to show diff
select coalesce(P.Filepath, 'None'), group_concat(T.Title)
from (Photo P join Photo_Tag PT on P.PhotoID = PT.PhotoID) 
right join Tags T on PT.TagID = T.TagID
group by P.Filepath;
