SET @CurrentUserID = UserID;

DELIMITER $$

CREATE TRIGGER NewPhoto
AFTER INSERT ON Photo
FOR EACH ROW
BEGIN
    DECLARE v_UserID INT;
    DECLARE v_LibraryAlbumID INT;
    DECLARE v_YearAlbumID INT;
    DECLARE v_Year INT;

    -- Get user from session
    SET v_UserID = @CurrentUserID;

    -- Check userID valid
    IF v_UserID IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'UserID not set before inserting photo';
    END IF;

    -- Get year
    SET v_Year = YEAR(COALESCE(NEW.DateTimeTaken, NOW()));

    -- Library album
    SELECT AlbumID INTO v_LibraryAlbumID
    FROM Album
    WHERE OwnerID = v_UserID AND AlbumName = 'Library'
    LIMIT 1;

    IF v_LibraryAlbumID IS NULL THEN
        INSERT INTO Album (OwnerID, AlbumName, CreatedAt)
        VALUES (v_UserID, 'Library', NOW());

        SET v_LibraryAlbumID = LAST_INSERT_ID();
    END IF;

    -- Year album
    SELECT AlbumID INTO v_YearAlbumID
    FROM Album
    WHERE OwnerID = v_UserID
      AND AlbumName = CONCAT('Auto-', v_Year)
    LIMIT 1;

    IF v_YearAlbumID IS NULL THEN
        INSERT INTO Album (OwnerID, AlbumName, CreatedAt)
        VALUES (v_UserID, CONCAT('Auto-', v_Year), NOW());

        SET v_YearAlbumID = LAST_INSERT_ID();
    END IF;

    -- Insert into junction table
    INSERT IGNORE INTO Album_Photo (AlbumID, PhotoID)
    VALUES (v_LibraryAlbumID, NEW.PhotoID);

    INSERT IGNORE INTO Album_Photo (AlbumID, PhotoID)
    VALUES (v_YearAlbumID, NEW.PhotoID);

END $$

DELIMITER ;