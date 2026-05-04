-- Functions ----------
-- returns the number of photos in a given album

drop function if exists fn_PhotoCount;

delimiter $$

create function fn_PhotoCount(album_id int)
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

-- Stored Procedures --
/*
stored procedure: remove_photo_from_album
purpose is to remove a specific photo from a specific album.
parameters: albumid, photoid
validation: ensures the album is not a protected system album, such as 'library' or 'auto', before removing the photo.
*/
drop procedure if exists sp_RemovePhotoFromAlbum;

delimiter $$

create procedure sp_RemovePhotoFromAlbum(
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


-- Triggers -----------
/* 
Trigger Name is album_update
Purpose is to automatically updates the album's last modified timestamp whenever a new photo is added to an album.
Event: after insert on album_photo
*/
drop trigger if exists tg_AlbumUpdate;

delimiter $$

create trigger tg_AlbumUpdate
after insert on Album_Photo
for each row
begin
    update Album
    set albumupdated = current_timestamp()
    where AlbumID = new.AlbumID;
end $$

delimiter ;
describe Album;
