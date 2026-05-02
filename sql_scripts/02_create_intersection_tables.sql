use PhotoApp;
 
# PhotoTag_Int - Salina
create table PhotoTag (
    PhotoID int,
    TagID int,
    primary key (PhotoID, TagID),
    foreign key (PhotoID) references Photo(PhotoID),
    foreign key (TagID) references Tags(TagID)
      on delete cascade
);

# AlbumPhoto_Int - Emily
Create table Album_Photo (
  AlbumID INT on delete cascade, 
  PhotoID int,
  primary key (AlbumID, PhotoID),
  foreign key (AlbumID) References Album(AlbumID)
  	on delete cascade,
  foreign key (PhotoID) References Photo(PhotoID)
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











