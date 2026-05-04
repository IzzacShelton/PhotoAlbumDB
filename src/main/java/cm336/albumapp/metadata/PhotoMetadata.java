package cm336.albumapp.metadata;

import java.time.LocalDateTime;

/**
 * Flat record of all metadata fields extractable from an image file.
 * Nullable fields are genuinely absent in the source image — the extractor
 * never guesses. DatabaseManager.importPhoto() maps these directly to
 * prepared statement parameters.
 *
 * Camera fields (brand, model, serialNumber) are grouped here for convenience.
 * DatabaseManager handles the camera upsert separately before inserting Photo.
 */
public record PhotoMetadata(
    // --- File info (always present) ---
    String filepath,
    long fileSize,
    int imageWidth,
    int imageHeight,

    // --- Camera info (EXIF) ---
    String cameraBrand,      // nullable - EXIF Make tag
    String cameraModel,      // nullable - EXIF Model tag
    String cameraSerial,     // nullable - EXIF BodySerialNumber tag

    // --- Capture info (EXIF) ---
    LocalDateTime dateTimeTaken,  // nullable - EXIF DateTimeOriginal tag

    // --- GPS info ---
    Double latitude,    // nullable
    Double longitude    // nullable
) {
    /** True if enough camera info exists */
    public boolean hasCameraInfo() {
        return cameraBrand != null && cameraModel != null;
    }

    /** True if GPS coordinates are present. */
    public boolean hasGps() {
        return latitude != null && longitude != null;
    }
}
