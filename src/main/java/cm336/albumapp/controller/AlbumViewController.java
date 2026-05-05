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
import java.io.File;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;

public class AlbumViewController {
    @FXML private StackPane gridContainer;
    @FXML private Label emptyPlaceholder, albumInfo;
    @FXML private Label albumTitleLabel;
    @FXML private FlowPane photoGrid;
    @FXML private ComboBox<TagRecord> tagFilterBox;

    private AlbumRecord currentAlbum;

    @FXML
    public void initialize() {
        currentAlbum = Session.getCurrentAlbum();
        albumTitleLabel.setText(currentAlbum.albumName());
        try {
            albumInfo.setText(String.format("%d photos", DatabaseManager.getPhotoCount(currentAlbum.albumId())));
        } catch (SQLException ex) {
            System.err.println("Failed to get the count!");
        }
        
        
        loadTagFilter();
        loadPhotos();
        setupImportContextMenu();
    }
    private void setupImportContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem importFiles = new MenuItem("Import File(s)...");
        importFiles.setOnAction(e -> {
            Window window = photoGrid.getScene().getWindow();
            List<File> files = PhotoImporter.pickFiles(window, "Select Photos to Import");
            if (files != null && !files.isEmpty()) {
                for (File f : files) {
                    PhotoImporter.importFileToAlbum(f, currentAlbum.albumId());
                }
                loadPhotos(); 
            }
        });
        
        MenuItem importFolder = new MenuItem("Import Folder...");
        
        MenuItem tagAll = new MenuItem("Tag All Photos...");
        tagAll.setOnAction(e -> TagActionUtil.promptTagAllPhotos(
            currentAlbum.albumId(), 
            currentAlbum.albumName(), 
            this::loadPhotos
        ));
        
        importFolder.setOnAction(e -> {
            Window window = photoGrid.getScene().getWindow();
            File dir = PhotoImporter.pickDirectory(window, "Select Folder to Import");
            if (dir != null) {
                List<File> files = PhotoImporter.listImages(dir);
                for (File f : files) {
                    PhotoImporter.importFileToAlbum(f, currentAlbum.albumId());
                }
                loadPhotos(); 
            }
        });
        
        contextMenu.getItems().addAll(importFiles, importFolder, new SeparatorMenuItem(), tagAll);
        
        gridContainer.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(gridContainer, e.getScreenX(), e.getScreenY());
            } 
            else if (e.getButton() == MouseButton.PRIMARY && photoGrid.getChildren().isEmpty()) {
                contextMenu.show(gridContainer, e.getScreenX(), e.getScreenY());
            } 
            else if (e.getButton() == MouseButton.PRIMARY) {
                contextMenu.hide();
            }
        });
    }
    
    private void loadPhotos() {
        photoGrid.getChildren().clear();
        TagRecord selectedTag = tagFilterBox.getValue();
        try {
            List<PhotoRecord> photos = (selectedTag == null)
                ? DatabaseManager.getPhotosInAlbum(currentAlbum.albumId())
                : DatabaseManager.getPhotosInAlbumByTag(currentAlbum.albumId(), selectedTag.tagId());
                
            for (PhotoRecord photo : photos) {
                PhotoThumbnail thumb = new PhotoThumbnail(photo, e -> {
                    try {
                        DatabaseManager.removePhotoFromAlbum(currentAlbum.albumId(), photo.photoId());
                        photoGrid.getChildren().removeIf(
                            n -> (n instanceof PhotoThumbnail) && ((PhotoThumbnail)n).photoId == photo.photoId()
                        );
                    } catch (SQLException ex) {
                        System.err.println("failed to remove photo from album");
                    }
                });
                photoGrid.getChildren().add(thumb);
            }
        } catch (SQLException e) { e.printStackTrace(System.err); }
        emptyPlaceholder.setVisible(photoGrid.getChildren().isEmpty());
    }

    @FXML private void onFilterChanged() { loadPhotos(); }

    @FXML private void onBack() { App.navigate("album_gallery"); }
    
    private void loadTagFilter() {
        tagFilterBox.setConverter(new StringConverter<>() {
            @Override public String toString(TagRecord t) { return t == null ? "All" : t.title(); }
            @Override public TagRecord fromString(String s) { return null; }
        });
        tagFilterBox.getItems().clear();
        tagFilterBox.getItems().add(null);
        try { tagFilterBox.getItems().addAll(DatabaseManager.getAllTags()); } catch (SQLException e) { e.printStackTrace(System.err); }
        tagFilterBox.getSelectionModel().selectFirst();
    }
}