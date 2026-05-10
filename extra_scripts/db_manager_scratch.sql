use PhotoApp;


# findOrCreateUser(username)
-- insert ignore into Users (Name) 
--   values (?);
-- 
-- select * from Users 
-- where Name = ? limit 1;

# getAlbumsForUser(userId)
-- select * from Album where OwnerID = ?;

# getRandomThumbnailPath(albumId): 
-- select Filepath 
-- from Photo P join  Album_Photo AP 
--   on P.PhotoID = AP.PhotoID
--   where AlbumID = ?
-- order by rand()
-- limit 1;

# getPhotoCount(albumId): 
-- select fn_PhotoCount(?);

# getPhotosInAlbum(albumId): 
-- select P.* 
-- from Photo P join  Album_Photo AP 
--   on P.PhotoID = AP.PhotoID
--   where AlbumID = ?
-- order by P.DateTimeTaken, P.DateTimeAdded;

# createAlbum(ownerId, name, description): 
-- insert into Album (OwnerID, AlbumName, AlbumDescription, AlbumType) 
--   values (?, ?, ?, 'User');
-- 
-- select * from Album
-- where AlbumID = last_insert_id();

# deleteAlbum(albumId): 
-- delete from Album
-- where AlbumID = ? and AlbumType = 'User';

# getPhotosInAlbumByTag(albumId, tagId): 
-- select P.*
-- from 
--   Photo P join (
--     select PhotoID, TagID, AlbumID
--     from Album_Photo AP join Photo_Tag PT
--     on AP.PhotoID = PT.PhotoID
--   ) J on J.PhotoID = P.PhotoID
-- where AlbumID = ?
--   and TagID = ?;


# importPhoto(meta, userId): 
-- sp_InsertPhoto(UserID, p_Filepath, p_FileSize, p_Latitude, p_Longitude, p_ImageWidth, p_ImageHeight, p_DateTimeTaken, c_Brand, c_Model, c_SerialNumber);
-- call sp_InsertPhoto(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
CALL sp_InsertPhoto(@uservariable, '/home/soms/Vault/DBMS Project Photos/IMG_4850.jpeg',
	2988641, NULL, NULL, 4032, 3024, '2024-04-20 18:53:09', 'Apple', 'iPhone 11', NULL);

# addPhotoToAlbum(albumId, photoId): 
-- insert ignore into Album_Photo (AlbumID, PhotoID)
--   values (?, ?);

# removePhotoFromAlbum(albumId, photoId): 
-- call sp_RemovePhotoFromAlbum(?, ?);

# getAllTags(): 
-- select * 
-- from Tags 
-- order by Title;

# createTag(title, color, tagType): 
-- insert into Tags (Title, TagColor, TagType)
--   values (?, ?, ?);
-- 
-- select * from Tags
-- where TagID = last_insert_id();

# deleteTag(tagId):
-- delete from Tags 
-- where TagID = ?;

# tagPhoto(photoId, tagId): 
-- insert ignore into Photo_Tag (PhotoID, TagID)
--   values (?, ?);

# untagPhoto(photoId, tagId): 
-- delete from Photo_Tag 
--   where PhotoID = ?
--     and TagID = ?;

# getPhotosByTag(tagId): 
-- select P.* 
-- from Photo P join Photo_Tag PT
--   on P.PhotoID = PT.PhotoID
--   where TagID = ?;
