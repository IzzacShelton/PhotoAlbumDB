use PhotoApp;

delimiter $$
create trigger if not exists tg_NewUser
  after insert 
  on Users for each row
    begin
	  -- 'Library' album type stores all photos uploaded by a User. 
      insert into Album(OwnerID, AlbumName, AlbumDescription, AlbumType) values 
	  	(new.userID, concat(new.Name,"\'s Library"), 'All uploaded photos.', 'Library');
    end $$
delimiter ;

-- insert into Users(Name) values ('Bob');

delimiter $$
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

delimiter $$
create procedure if not exists sp_InsertPhoto(
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
	-- not sure if needed but since this effects multiple tables seems appropriate
	declare exit handler for SQLEXCEPTION
		begin
			rollback;
		end;
	
	start transaction;
		-- check if a matching Camera exists already
		if c_Brand is not null and c_Model is not null then
			call FindOrCreateCamera(c_Brand, c_Model, c_SerialNumber, c_CameraID);
		end if;
		insert into Photo 
			(CameraID, Filepath, Filesize, Latitude, Longitude, ImageWidth, ImageHeight, DateTimeTaken)
		values
			(c_CameraID, p_Filepath, p_Filesize, p_Latitude, p_Longitude, p_ImageWidth, p_ImageHeight, p_DateTimeTaken);
	commit;
end $$
delimiter ;


