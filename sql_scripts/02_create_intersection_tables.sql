use PhotoApp;

# PhotoTag_Int
create table PhotoTag (
    PhotoID int,
    TagID int,
    primary key (PhotoID, TagID),
    foreign key (PhotoID) references Photo(PhotoID),
    foreign key (TagID) references Tags(TagID)
);

# AlbumPhoto_Int
Create table Album_Photo (
  AlbumID INT, 
  PhotoID int,

  primary key (AlbumID, PhotoID),
  foreign key (AlbumID) References Album(AlbumID),
  foreign key (PhotoID) References Photo(PhotoID)
);

# AlbumShares
CREATE TABLE AlbumShares(
  ReceiverID int,
  AlbumID int,
  
  PRIMARY KEY (ReceiverID, AlbumID),
  
  FOREIGN KEY (ReceiverID) 
    REFERENCES Users(UserID),
  FOREIGN KEY (AlbumID)
    REFERENCES Album(AlbumID)
);











