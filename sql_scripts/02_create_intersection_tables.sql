use PhotoApp;
 
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
create table Album_Shares(
  ReceiverID int,
  AlbumID int,
  
  primary key (ReceiverID, AlbumID),
  
  foreign key (ReceiverID) 
    references Users(UserID)
       on delete cascade,
  foreign key (AlbumID)
    references Album(AlbumID)
       on delete cascade
);









