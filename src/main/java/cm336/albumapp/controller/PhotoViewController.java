package cm336.albumapp.controller;

import cm336.albumapp.App;
import cm336.albumapp.Session;
import cm336.albumapp.db.DatabaseManager;
import cm336.albumapp.metadata.MetaRow;
import cm336.albumapp.model.PhotoRecord;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PhotoViewController {

    @FXML private ImageView photoDisplay;
    @FXML private Label photoInfoLabel;
    @FXML private TreeTableView<MetaRow> metadataTree;
    @FXML private TreeTableColumn<MetaRow, String> metaNameColumn;
    @FXML private TreeTableColumn<MetaRow, String> metaValueColumn;

    private List<PhotoRecord> albumPhotos;
    private int currentIndex;

    @FXML
    public void initialize() {
        metaNameColumn.setCellValueFactory(p ->
            new SimpleStringProperty(p.getValue().getValue().name()));
        metaValueColumn.setCellValueFactory(p ->
            new SimpleStringProperty(p.getValue().getValue().value()));
        metadataTree.setShowRoot(false);

        loadPhoto(Session.getCurrentPhoto());

        try {
            albumPhotos = DatabaseManager.getPhotosInAlbum(Session.getCurrentAlbum().albumId());
            currentIndex = findIndex(Session.getCurrentPhoto());
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    private void loadPhoto(PhotoRecord photo) {
        File file = new File(photo.filepath());
        photoDisplay.setImage(new Image(file.toURI().toString()));
        photoInfoLabel.setText(file.getName() + "  " + photo.imageWidth() + "×" + photo.imageHeight());
        populateMetadata(file);
    }

    private void populateMetadata(File file) {
        TreeItem<MetaRow> root = new TreeItem<>(new MetaRow("", ""));
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            for (Directory dir : metadata.getDirectories()) {
                TreeItem<MetaRow> dirItem = new TreeItem<>(new MetaRow(dir.getName(), ""));
                dirItem.setExpanded(true);
                for (Tag tag : dir.getTags()) {
                    String desc = tag.getDescription();
                    dirItem.getChildren().add(
                        new TreeItem<>(new MetaRow(tag.getTagName(), desc != null ? desc : ""))
                    );
                }
                root.getChildren().add(dirItem);
            }
        } catch (IOException | ImageProcessingException e) {
            e.printStackTrace(System.err);
        }
        metadataTree.setRoot(root);
    }

    @FXML
    private void onPrevious() {
        if (albumPhotos == null || currentIndex <= 0) return;
        currentIndex--;
        PhotoRecord prev = albumPhotos.get(currentIndex);
        Session.setCurrentPhoto(prev);
        loadPhoto(prev);
    }

    @FXML
    private void onNext() {
        if (albumPhotos == null || currentIndex >= albumPhotos.size() - 1) return;
        currentIndex++;
        PhotoRecord next = albumPhotos.get(currentIndex);
        Session.setCurrentPhoto(next);
        loadPhoto(next);
    }

    @FXML private void onBack() { App.navigate("album_view"); }

    private int findIndex(PhotoRecord photo) {
        if (albumPhotos == null) return 0;
        for (int i = 0; i < albumPhotos.size(); i++) {
            if (albumPhotos.get(i).photoId() == photo.photoId()) return i;
        }
        return 0;
    }
}
