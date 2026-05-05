package cm336.albumapp.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.gif.GifHeaderDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.drew.lang.GeoLocation;
import com.drew.metadata.exif.GpsDirectory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Extracts image metadata into a flat PhotoMetadata record.
 *
 * Supports any format Drew's ImageMetadataReader handles (JPEG, PNG, TIFF,
 * GIF, BMP, WEBP, etc.). Fields absent from a given format come back null —
 * a PNG will never have EXIF camera info, for example, and that is fine.
 *
 * All format-specific directory lookups are isolated here. Nothing outside
 * this class should import com.drew.* directly.
 */
public final class MetadataExtractor {

    private MetadataExtractor() {}

    /**
     * Reads all extractable metadata from an image file.
     *
     * @param file the image file to read
     * @return a PhotoMetadata record with all available fields populated
     * @throws IOException if the file cannot be read
     * @throws ImageProcessingException if Drew cannot parse the file format
     */
    public static PhotoMetadata extract(File file)
            throws IOException, ImageProcessingException {

        Metadata metadata = ImageMetadataReader.readMetadata(file);

        return new PhotoMetadata(
            file.getAbsolutePath(),
            file.length(),
            extractWidth(metadata),
            extractHeight(metadata),
            extractCameraBrand(metadata),
            extractCameraModel(metadata),
            extractCameraSerial(metadata),
            extractDateTimeTaken(metadata),
            extractLatitude(metadata),
            extractLongitude(metadata)
        );
    }

    // Dimension extraction — tries JPEG -> PNG -> GIF 
    private static int extractWidth(Metadata metadata) {
        JpegDirectory jpeg = metadata.getFirstDirectoryOfType(JpegDirectory.class);
        if (jpeg != null && jpeg.containsTag(JpegDirectory.TAG_IMAGE_WIDTH))
            return jpeg.getInteger(JpegDirectory.TAG_IMAGE_WIDTH);

        PngDirectory png = metadata.getFirstDirectoryOfType(PngDirectory.class);
        if (png != null && png.containsTag(PngDirectory.TAG_IMAGE_WIDTH))
            return png.getInteger(PngDirectory.TAG_IMAGE_WIDTH);

        GifHeaderDirectory gif = metadata.getFirstDirectoryOfType(GifHeaderDirectory.class);
        if (gif != null && gif.containsTag(GifHeaderDirectory.TAG_IMAGE_WIDTH))
            return gif.getInteger(GifHeaderDirectory.TAG_IMAGE_WIDTH);

        return 0;
    }

    private static int extractHeight(Metadata metadata) {
        JpegDirectory jpeg = metadata.getFirstDirectoryOfType(JpegDirectory.class);
        if (jpeg != null && jpeg.containsTag(JpegDirectory.TAG_IMAGE_HEIGHT))
            return jpeg.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT);

        PngDirectory png = metadata.getFirstDirectoryOfType(PngDirectory.class);
        if (png != null && png.containsTag(PngDirectory.TAG_IMAGE_HEIGHT))
            return png.getInteger(PngDirectory.TAG_IMAGE_HEIGHT);

        GifHeaderDirectory gif = metadata.getFirstDirectoryOfType(GifHeaderDirectory.class);
        if (gif != null && gif.containsTag(GifHeaderDirectory.TAG_IMAGE_HEIGHT))
            return gif.getInteger(GifHeaderDirectory.TAG_IMAGE_HEIGHT);

        return 0;
    }

    // Camera extraction — (JPEG) EXIF IFD0 only. null if not present.
    private static String extractCameraBrand(Metadata metadata) {
        ExifIFD0Directory exif = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (exif == null) return null;
        return exif.getString(ExifIFD0Directory.TAG_MAKE);
    }

    private static String extractCameraModel(Metadata metadata) {
        ExifIFD0Directory exif = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (exif == null) return null;
        return exif.getString(ExifIFD0Directory.TAG_MODEL);
    }

    private static String extractCameraSerial(Metadata metadata) {
        ExifIFD0Directory exif = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (exif == null) return null;
        return exif.getString(ExifIFD0Directory.TAG_BODY_SERIAL_NUMBER);
    }

    // Datetime extraction. prefers DateTimeOriginal, falls back to DateTime.
    private static LocalDateTime extractDateTimeTaken(Metadata metadata) {
        ExifSubIFDDirectory sub = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (sub != null) {
            Date d = sub.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (d != null) return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (ifd0 != null) {
            Date d = ifd0.getDate(ExifIFD0Directory.TAG_DATETIME);
            if (d != null) return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        return null;
    }

    // GPS extraction. Null otherwise
    private static Double extractLatitude(Metadata metadata) {
        GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gps == null) return null;
        GeoLocation loc = gps.getGeoLocation();
        return loc != null ? loc.getLatitude() : null;
    }

    private static Double extractLongitude(Metadata metadata) {
        GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gps == null) return null;
        GeoLocation loc = gps.getGeoLocation();
        return loc != null ? loc.getLongitude() : null;
    }
}
