DELIMITER $$
CREATE FUNCTION if not exists fn_GetYearAlbumID (
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
      AND AlbumType = 'Auto'
      AND AlbumName = CONCAT(p_Year, ' Photos')
    LIMIT 1;

    RETURN v_AlbumID;
END $$
DELIMITER ;

-- SET @CurrentUserID = UserID;
DELIMITER $$
CREATE TRIGGER if not exists tg_NewPhoto
AFTER INSERT ON Photo
FOR EACH ROW
BEGIN
    DECLARE v_UserID INT;
    DECLARE v_LibraryAlbumID INT;
    DECLARE v_YearAlbumID INT;
    DECLARE v_Year INT;

    -- Get user from session (set in sp_InsertPhoto)
    SET v_UserID = @CurrentUserID;

    -- Check userID valid
    IF v_UserID IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'UserID not set before inserting photo';
    END IF;

    -- Get year
    SET v_Year = YEAR(COALESCE(NEW.DateTimeTaken, NOW()));
    
    -- Library album
    SELECT fn_GetLibraryID(@CurrentUserID) INTO v_LibraryAlbumID;

    IF v_LibraryAlbumID IS NULL THEN
        INSERT INTO Album (OwnerID, AlbumName, CreatedAt)
        VALUES (v_UserID, 'Library', NOW());

        SET v_LibraryAlbumID = LAST_INSERT_ID();
    END IF;
    
    -- Year album
    SELECT fn_GetYearAlbumID(@CurrentUserID, v_Year) INTO v_YearAlbumID;

    IF v_YearAlbumID IS NULL THEN
        INSERT INTO Album (OwnerID, AlbumName, AlbumType)
        VALUES (v_UserID, CONCAT(v_Year, ' Photos'), 'Auto');
        SET v_YearAlbumID = LAST_INSERT_ID();
    END IF;

    -- Insert into junction table
    INSERT IGNORE INTO Album_Photo (AlbumID, PhotoID)
    VALUES (v_LibraryAlbumID, NEW.PhotoID);

    INSERT IGNORE INTO Album_Photo (AlbumID, PhotoID)
    VALUES (v_YearAlbumID, NEW.PhotoID);

END $$
DELIMITER ;


DELIMITER //
Create Procedure if not exists sp_FindOrCreateCamera(
    IN p_Brand VARCHAR(100),
    IN p_Model VARCHAR(100),
    IN p_SerialNumber VARCHAR(100),
    OUT p_CameraID INT
    )
Begin
    -- Try to find existing camera using SerialNumber
    SELECT CameraID INTO p_CameraID
    FROM Camera
    WHERE SerialNumber <=> p_SerialNumber
      OR (Brand = p_Brand AND Model = p_Model)
    LIMIT 1;

    -- If not found, insert new camera
    IF p_CameraID IS NULL THEN
        INSERT INTO Camera (Brand, Model, SerialNumber)
        VALUES (p_Brand, p_Model, p_SerialNumber);
        -- Get the new CameraID
        SET p_CameraID = LAST_INSERT_ID();
    END IF;
END //
DELIMITER ;
