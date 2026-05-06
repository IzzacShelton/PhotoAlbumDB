insert into Users (Name) 
	values ('Salina'), ('Leo'), ('Emma'), ('Jonah'), ('Amy');

select UserID into @userID
from Users 
where Name = 'Salina';

select @userID;

insert ignore into Camera (Brand, Model, SerialNumber) 
	values ('Apple', 'iPhone XS Max', NULL),
		   ('Apple', 'iPhone 16', NULL),
		   ('Canon', 'EOS R50', NULL),
		   ('Sony', 'Alpha A7 III', NULL),
		   ('Nikon', 'D3500', NULL);

insert into Tags (Title, TagColor, TagType) 
	values 
		('Travel', 0x00FF00, 'Custom'),
		('Friends', 0xFF00FF, 'Custom'),
		('Campus', 0x00FFFF, 'Custom'),
		('Food', 0xBEEEEF, 'Custom'),
		('Family', 0xFF00FF, 'Custom'); 

call sp_InsertPhoto(@userID, '/home/soms/Vault/DBMS Project Photos/Salina/IMG_3941.jpg', 
	3398560, NULL, NULL, 4032, 3024, '2023-12-26 13:26:39', 
	'Apple', 'iPhone XS Max', NULL);

call sp_InsertPhoto(@userID, '/home/soms/Vault/DBMS Project Photos/Salina/IMG_4125.jpg', 
	4485951, NULL, NULL, 4032, 3024, '2023-05-03 14:27:27', 
	'Apple', 'iPhone XS Max', NULL);

call sp_InsertPhoto(@userID, '/home/soms/Vault/DBMS Project Photos/Salina/IMG_4635.jpg', 
	2386504, NULL, NULL, 4032, 3024, '2023-05-04 13:39:38', 
	'Apple', 'iPhone XS Max', NULL);

call sp_InsertPhoto(@userID, '/home/soms/Vault/DBMS Project Photos/Salina/IMG_3347.jpg', 
	1241136, NULL, NULL, 1242, 2208, NULL, 
	NULL, NULL, NULL);

call sp_InsertPhoto(@userID, '/home/soms/Vault/DBMS Project Photos/Salina/IMG_3347.jpg', 
	1241136, NULL, NULL, 1242, 2208, NULL, 
	'Apple', 'iPhone 16', NULL);
    
insert into Album (OwnerID, AlbumName, AlbumDescription, AlbumType)
	values
		(@userID, 'Travel Album', 'Travel photos collection', 'User'),
		(@userID, 'Campus Album', 'Campus memories', 'User'),
		(@userID, 'Friends Album', 'Photos with friends', 'User'),
		(@userID, 'Food Album', 'Food pictures', 'User'),
		(@userID, 'Family Album', 'Family memories', 'User');

select TagID into @travelTagID 
from Tags
where Title = 'Travel';

select TagID into @friendsTagID 
from Tags
where Title = 'Friends';

select TagID into @campusTagID 
from Tags
where Title = 'Campus';

select TagID into @foodTagID 
from Tags
where Title = 'Food';

select TagID into @familyTagID 
from Tags
where Title = 'Family';

select AlbumID into @travelAlbumID
from Album
where AlbumName = 'Travel Album'
and OwnerID = @userID;

select AlbumID into @campusAlbumID
from Album
where AlbumName = 'Campus Album'
and OwnerID = @userID;

select AlbumID into @friendsAlbumID
from Album
where AlbumName = 'Friends Album'
and OwnerID = @userID;

select AlbumID into @foodAlbumID
from Album
where AlbumName = 'Food Album'
and OwnerID = @userID;

select AlbumID into @familyAlbumID
from Album
where AlbumName = 'Family Album'
and OwnerID = @userID;

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

insert into AlbumShares (ReceiverID, AlbumID)
	values
		(2, @travelAlbumID),
		(3, @campusAlbumID),
		(4, @friendsAlbumID),
		(5, @foodAlbumID),
		(2, @familyAlbumID);

-- Query 1
select * from Photo;

-- Query 2
select P.Filepath, C.Brand, C.Model
from Photo P
left join Camera C on P.CameraID = C.CameraID;

-- Query 3
select AlbumName, fn_PhotoCount(AlbumID) as PhotoCount
from Album
where OwnerID = @userID;

-- Query 4
update Tags 
	set Title = 'Trips'
where Title = 'Travel';

-- Query 5
delete from AlbumShares
where ReceiverID = 999;
