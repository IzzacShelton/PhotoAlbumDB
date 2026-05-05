package cm336.albumapp.controller;

import cm336.albumapp.db.DatabaseManager;
import cm336.albumapp.model.PhotoRecord;
import cm336.albumapp.model.TagRecord;
import java.sql.SQLException;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;

public final class TagActionUtil {

    private TagActionUtil() {}

    /**
     * Prompts the user to select a tag, then applies it to all photos in the specified album.
     * * @param albumId The ID of the album containing the photos.
     * @param albumName The name of the album (for display purposes).
     * @param onSuccess Callback executed if the tagging is completed successfully.
     */
    public static void promptTagAllPhotos(int albumId, String albumName, Runnable onSuccess) {
        try {
            List<TagRecord> allTags = DatabaseManager.getAllTags();
            if (allTags.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "No tags available. Please create a tag first.");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }

            // Wrapper class for nice formatting in the ChoiceDialog
            class TagOption {
                final TagRecord tag;
                TagOption(TagRecord tag) { this.tag = tag; }
                @Override public String toString() { return tag.title(); }
            }
            
            List<TagOption> options = allTags.stream().map(TagOption::new).toList();
            
            ChoiceDialog<TagOption> dialog = new ChoiceDialog<>(options.get(0), options);
            dialog.setTitle("Tag All Photos");
            dialog.setHeaderText("Apply a tag to all photos in '" + albumName + "'");
            dialog.setContentText("Select tag:");

            dialog.showAndWait().ifPresent(selectedOption -> {
                try {
                    List<PhotoRecord> photos = DatabaseManager.getPhotosInAlbum(albumId);
                    for (PhotoRecord photo : photos) {
                        DatabaseManager.tagPhoto(photo.photoId(), selectedOption.tag.tagId());
                    }
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION, 
                        "Successfully applied tag to " + photos.size() + " photos!");
                    success.setHeaderText(null);
                    success.showAndWait();
                    
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    
                } catch (SQLException e) {
                    e.printStackTrace(System.err);
                }
            });
            
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }
}