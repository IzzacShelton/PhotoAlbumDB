-- Combine camera and photo data
SELECT P.PhotoID, P.Filepath, C.Brand, C.Model
FROM Photo P
LEFT JOIN Camera C ON P.CameraID = C.CameraID;

-- Count the # of photos in an album
SELECT A.AlbumName, COUNT(AP.PhotoID) AS PhotoCount
FROM Album A
LEFT JOIN Album_Photo AP ON A.AlbumID = AP.AlbumID
GROUP BY A.AlbumID, A.AlbumName;

-- Find photos from a specific year
SELECT PhotoID, Filepath, DateTimeTaken
FROM Photo
WHERE YEAR(DateTimeTaken) = 2024;

-- Update album description for all ‘Library’ albums
UPDATE Album
SET AlbumDescription = 'Default library album for user photos'
WHERE AlbumType = 'Library';

-- Delete photos not linked to any album
DELETE FROM Photo
WHERE PhotoID NOT IN (
    SELECT PhotoID FROM Album_Photo
);
