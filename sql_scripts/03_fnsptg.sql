-- Functions ----------
-- returns the number of photos in a given album

drop function if exists photo_count;

delimiter $$

create function photo_count(album_id int)
returns int
deterministic
begin
    declare photo_total int;

    select count(*)
    into photo_total
    from album_photo
    where albumid = album_id;

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
drop procedure if exists remove_photo_from_album;

delimiter $$

create procedure remove_photo_from_album(
    in p_albumid int,
    in p_photoid int
)
begin
    delete from album_photo
    where albumid = p_albumid
      and photoid = p_photoid
      and p_albumid not in (
          select albumid
          from album
          where albumname in ('library', 'auto')
      );
end $$

delimiter ;


-- Triggers -----------
/* 
Trigger Name is album_update
Purpose is to automatically updates the album's last modified timestamp whenever a new photo is added to an album.
Event: after insert on album_photo
*/
drop trigger if exists album_update;

delimiter $$

create trigger album_update
after insert on album_photo
for each row
begin
    update album
    set albumupdates = current_timestamp
    where albumid = new.albumid;
end $$

delimiter ;
describe album;
