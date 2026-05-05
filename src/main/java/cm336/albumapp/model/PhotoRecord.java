package cm336.albumapp.model;

import java.time.LocalDateTime;

/**
 * Represents a row from the Photo table.
 */
public record PhotoRecord(
    int photoId,
    Integer cameraId,       // nullable
    String filepath,
    long fileSize,
    Double latitude,        // nullable
    Double longitude,       // nullable
    int imageWidth,
    int imageHeight,
    LocalDateTime dateTimeTaken,    // nullable
    LocalDateTime dateTimeAdded
) {}
