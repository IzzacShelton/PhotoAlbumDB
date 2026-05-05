package cm336.albumapp.model;

/**
 * Represents a row from the Camera table.
 */
public record CameraRecord(
    int cameraId,
    String brand,
    String model,
    String serialNumber  // nullable
) {}
