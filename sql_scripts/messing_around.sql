use PhotoApp;

delimiter $$
create trigger tg_NewUser
  after insert 
  on Users for each row
    begin
      insert into Album(OwnerID, AlbumName, AlbumDescription) values 
	  	(new.userID, concat(new.Name,"'s Library"), "All uploaded photos.");
    end $$

create trigger tg_NewAlbum
  after insert 
  on Album for each row
    begin
      insert into Album(OwnerID, AlbumName, AlbumDescription) values 
	  	(new.userID, concat(new.Name,"'s Library"), "All uploaded photos.");
    end $$
delimiter ;

set @name = "Izzac";
set @email = "izzacshelton@gmail.com";

insert into Users (Name, Email) values (@name, @email);

select userID into @userID from Users where name = @name;

-- insert into Album(OwnerID, AlbumName, AlbumDescription) values 
-- (@userID, concat(@name,"'s Library"), "All uploaded photos."),
-- (@userID, "Vacation Photos", "Pictures from my vacation.");

# gets
select min(AlbumID) 
	into @libraryID
from Album 
where OwnerID = @userID;

insert into Photo (CameraID, Filepath, Filesize, Latitude, Longitude, ImageWidth, ImageHeight, DateTimeTaken) Values 
(NULL, '/home/soms/Old/Java-Projects/Cellular-Automata/Colored/New/073-233-073_256x256.png', 20762, NULL, NULL, 256, 256, NULL);






