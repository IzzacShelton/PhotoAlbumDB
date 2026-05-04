DELIMITER $$

CREATE FUNCTION GetYearAlbumID (
    p_UserID INT,
    p_Year INT
)
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE v_AlbumID INT;

    SELECT AlbumID INTO v_AlbumID
    FROM Album
    WHERE OwnerID = p_UserID
      AND YEAR(CreatedAt) = p_Year
    LIMIT 1;

    RETURN v_AlbumID;
END $$

DELIMITER ;