package cm336.albumapp.controller;

import cm336.albumapp.App;
import cm336.albumapp.Session;
import cm336.albumapp.db.DatabaseManager;
import cm336.albumapp.model.AlbumRecord;
import java.sql.SQLException;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

public class AlbumGalleryController {

    @FXML private FlowPane albumGrid;
    @FXML private TextField newAlbumNameField;

    @FXML
    public void initialize() {
        loadAlbums();
    }

    private void loadAlbums() {
        albumGrid.getChildren().clear();
        try {
            int userId = Session.getCurrentUser().userId();
            List<AlbumRecord> albums = DatabaseManager.getAlbumsForUser(userId);
            for (AlbumRecord album : albums) {
                String cover = DatabaseManager.getRandomThumbnailPath(album.albumId());
                albumGrid.getChildren().add(new AlbumCard(album, cover, () -> onOpenAlbum(album)));
            }
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
            int userId = Session.getCurrentUser().userId();
            DatabaseManager.createAlbum(userId, name, "");
            newAlbumNameField.clear();
            loadAlbums();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }
}
