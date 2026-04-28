Create table Album_Photo (
AlbumID INT, 
PhotoID serial,

foreign key (AlbumID) References Album(AlbumID),
foreign key (PhotoID) References Photo(PhotoID)
);
