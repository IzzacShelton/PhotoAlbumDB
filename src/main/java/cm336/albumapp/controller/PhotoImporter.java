package cm336.albumapp.controller;

import cm336.albumapp.Session;
import cm336.albumapp.db.DatabaseManager;
import cm336.albumapp.metadata.MetadataExtractor;
import cm336.albumapp.metadata.PhotoMetadata;
import cm336.albumapp.model.PhotoRecord;
import com.drew.imaging.ImageProcessingException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public final class PhotoImporter {

    public static final List<String> IMAGE_EXTENSIONS =
        Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".webp");

    public static final FileChooser.ExtensionFilter IMAGE_FILTER =
        new FileChooser.ExtensionFilter("Image Files",
            "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.tiff", "*.webp");

    private PhotoImporter() {}

    /**
     * Extracts metadata from a file and imports it into the database.
     * Returns the new PhotoRecord, or null if the file was skipped.
     */
    public static PhotoRecord importFile(File file) {
        try {
            PhotoMetadata meta = MetadataExtractor.extract(file);
            return DatabaseManager.importPhoto(meta, Session.getCurrentUser().userId());
        } catch (IOException | ImageProcessingException e) {
            System.err.println("Skipped unreadable file: " + file.getName());
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    /**
     * Imports a file and additionally associates it with a specific album.
     */
    public static PhotoRecord importFileToAlbum(File file, int albumId) {
        PhotoRecord photo = importFile(file);
        if (photo != null) {
            try {
                DatabaseManager.addPhotoToAlbum(albumId, photo.photoId());
            } catch (SQLException e) {
                e.printStackTrace(System.err);
            }
        }
        return photo;
    }

    /** Returns all image files in a directory, non-recursively. */
    public static List<File> listImages(File dir) {
        File[] files = dir.listFiles(f ->
            IMAGE_EXTENSIONS.stream().anyMatch(ext ->
                f.getName().toLowerCase().endsWith(ext)));
        return files != null ? Arrays.asList(files) : List.of();
    }

    /** Opens a single-file image picker dialog. */
    public static File pickFile(Window owner, String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(IMAGE_FILTER);
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        return fc.showOpenDialog(owner);
    }

    /** Opens a multi-file image picker dialog. */
    public static List<File> pickFiles(Window owner, String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(IMAGE_FILTER);
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        List<File> files = fc.showOpenMultipleDialog(owner);
        return files != null ? files : List.of();
    }

    /** Opens a directory picker dialog. */
    public static File pickDirectory(Window owner, String title) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle(title);
        dc.setInitialDirectory(new File(System.getProperty("user.home")));
        return dc.showDialog(owner);
    }
}
