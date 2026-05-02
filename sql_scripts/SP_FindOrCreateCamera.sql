DELIMITER //

Create Procedure FindOrCreateCamera(
	IN p_Brand VARCHAR(100),
    IN p_Model VARCHAR(100),
    IN p_SerialNumber VARCHAR(100),
    OUT p_CameraID INT
    )
Begin
	-- Try to find existing camera using SerialNumber
    SELECT CameraID INTO p_CameraID
    FROM Camera
    WHERE SerialNumber = p_SerialNumber
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