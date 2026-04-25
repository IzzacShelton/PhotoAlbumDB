CREATE TABLE Album (
    AlbumID INT PRIMARY KEY,
    OwnerID INT,
    AlbumName VARCHAR(255),
    AlbumDescription TEXT,
    CreatedAt DATETIME,
    AlbumUpdated DATETIME,
    
    FOREIGN KEY (OwnerID) REFERENCES Users(UserID)
);