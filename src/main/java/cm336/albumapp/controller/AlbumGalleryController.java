package cm336.albumapp.controller;

import cm336.albumapp.App;
import cm336.albumapp.Session;
import cm336.albumapp.db.DatabaseManager;
import cm336.albumapp.model.AlbumRecord;
import cm336.albumapp.model.PhotoRecord;
import cm336.albumapp.model.TagRecord;
import java.sql.SQLException;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

public class AlbumGalleryController {

    @FXML private FlowPane userAlbumGrid, autoAlbumGrid;
    @FXML private TextField newAlbumNameField;

    @FXML
    public void initialize() {
        loadAlbums();
    }

    private void loadAlbums() {
        userAlbumGrid.getChildren().clear();
        autoAlbumGrid.getChildren().clear();
        try {
            List<AlbumRecord> albums = DatabaseManager.getAlbumsForUser(Session.getCurrentUser().userId());
            for (AlbumRecord album : albums) {
                String cover = DatabaseManager.getRandomThumbnailPath(album.albumId());
                AlbumCard card = new AlbumCard(album, cover, 
                    () -> onOpenAlbum(album), 
                    () -> onDeleteAlbum(album),
                    () -> onTagAllPhotos(album)
                );
                (album.isUser() ? userAlbumGrid :  autoAlbumGrid).getChildren().add(card);
            }
        } catch (SQLException e) { e.printStackTrace(System.err); }
    }
    private void onTagAllPhotos(AlbumRecord album) {
        try {
            List<TagRecord> allTags = DatabaseManager.getAllTags();
            if (allTags.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "No tags available. Please create a tag first.");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }

            // Simple wrapper class to display the TagRecord's title nicely in the ChoiceDialog
            class TagOption {
                final TagRecord tag;
                TagOption(TagRecord tag) { this.tag = tag; }
                @Override public String toString() { return tag.title(); }
            }
            
            List<TagOption> options = allTags.stream().map(TagOption::new).toList();
            
            ChoiceDialog<TagOption> dialog = new ChoiceDialog<>(options.get(0), options);
            dialog.setTitle("Tag All Photos");
            dialog.setHeaderText("Apply a tag to all photos in '" + album.albumName() + "'");
            dialog.setContentText("Select tag:");

            dialog.showAndWait().ifPresent(selectedOption -> {
                try {
                    List<PhotoRecord> photos = DatabaseManager.getPhotosInAlbum(album.albumId());
                    for (PhotoRecord photo : photos) {
                        DatabaseManager.tagPhoto(photo.photoId(), selectedOption.tag.tagId());
                    }
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION, 
                        "Successfully applied tag to " + photos.size() + " photos!");
                    success.setHeaderText(null);
                    success.showAndWait();
                    
                } catch (SQLException e) {
                    e.printStackTrace(System.err);
                }
            });
            
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }
    private void onDeleteAlbum(AlbumRecord album) {
        try {
            DatabaseManager.deleteAlbum(album.albumId());
            loadAlbums(); // Refresh the grid
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }
    public void onOpenAlbum(AlbumRecord album) {
        Session.setCurrentAlbum(album);
        App.navigate("album_view");
    }

    @FXML
    private void onCreateAlbum() {
        String name = newAlbumNameField.getText().trim();
        if (name.isEmpty()) return;
        try {
            DatabaseManager.createAlbum(Session.getCurrentUser().userId(), name, "");
            newAlbumNameField.clear();
            loadAlbums();
        } catch (SQLException e) { e.printStackTrace(System.err); }
    }
}