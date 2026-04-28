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













# AlbumShares












