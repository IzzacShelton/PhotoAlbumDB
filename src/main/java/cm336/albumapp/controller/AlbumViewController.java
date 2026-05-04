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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.util.StringConverter;

public class AlbumViewController {

    @FXML private Label albumTitleLabel;
    @FXML private FlowPane photoGrid;
    @FXML private ComboBox<TagRecord> tagFilterBox;

    private AlbumRecord currentAlbum;

    @FXML
    public void initialize() {
        currentAlbum = Session.getCurrentAlbum();
        albumTitleLabel.setText(currentAlbum.albumName());
        loadTagFilter();
        loadPhotos();
    }

    private void loadTagFilter() {
        tagFilterBox.setConverter(new StringConverter<>() {
            @Override public String toString(TagRecord tag) {
                return tag == null ? "All" : tag.title();
            }
            @Override public TagRecord fromString(String s) { return null; }
        });
        tagFilterBox.getItems().clear();
        tagFilterBox.getItems().add(null);
        try {
            tagFilterBox.getItems().addAll(DatabaseManager.getAllTags());
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
        tagFilterBox.getSelectionModel().selectFirst();
    }

    private void loadPhotos() {
        photoGrid.getChildren().clear();
        TagRecord selectedTag = tagFilterBox.getValue();
        try {
            List<PhotoRecord> photos = (selectedTag == null)
                ? DatabaseManager.getPhotosInAlbum(currentAlbum.albumId())
                : DatabaseManager.getPhotosInAlbumByTag(currentAlbum.albumId(), selectedTag.tagId());
            for (PhotoRecord photo : photos) {
                photoGrid.getChildren().add(new PhotoThumbnail(photo, () -> onOpenPhoto(photo)));
            }
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    @FXML private void onFilterChanged() { loadPhotos(); }

    public void onOpenPhoto(PhotoRecord photo) {
        Session.setCurrentPhoto(photo);
        App.navigate("photo_view");
    }

    @FXML private void onBack() { App.navigate("album_gallery"); }
}
