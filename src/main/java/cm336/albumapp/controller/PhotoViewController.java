package cm336.albumapp.controller;

import cm336.albumapp.App;
import cm336.albumapp.Session;
import cm336.albumapp.db.DatabaseManager;
import cm336.albumapp.metadata.MetaRow;
import cm336.albumapp.model.PhotoRecord;
import cm336.albumapp.model.TagRecord;
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
import javafx.scene.control.Accordion;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

public class PhotoViewController {
    @FXML private Accordion sideAccordion;
    @FXML private ImageView photoDisplay;
    @FXML private StackPane imageContainer;
    @FXML private TitledPane photoInfo;
    @FXML private FlowPane tagFlowPane;
    @FXML private Label photoLabel;
    @FXML private TreeTableView<MetaRow> metadataTree;
    @FXML private TreeTableColumn<MetaRow, String> metaNameColumn;
    @FXML private TreeTableColumn<MetaRow, String> metaValueColumn;
    
    private List<PhotoRecord> albumPhotos;
    private int currentIndex;
    
    private ContextMenu imageContextMenu = new ContextMenu();

    @FXML
    public void initialize() {
        sideAccordion.setExpandedPane(photoInfo);
        metaNameColumn.setCellValueFactory(p ->
            new SimpleStringProperty(p.getValue().getValue().name()));
        metaValueColumn.setCellValueFactory(p ->
            new SimpleStringProperty(p.getValue().getValue().value()));
        metadataTree.setShowRoot(false);

        photoDisplay.fitWidthProperty().bind(imageContainer.widthProperty());
        photoDisplay.fitHeightProperty().bind(imageContainer.heightProperty());

        imageContainer.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                imageContextMenu.hide();
            }
        });
        
        imageContainer.setOnContextMenuRequested(e -> {
            imageContextMenu.show(imageContainer, e.getScreenX(), e.getScreenY());
            e.consume(); 
        });

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
        photoLabel.setText(file.getName() + "  " + photo.imageWidth() + "×" + photo.imageHeight());
        populateMetadata(file);
        populateTags(photo);
    }

    private void populateTags(PhotoRecord photo) {
        tagFlowPane.getChildren().clear();
        try {
            List<TagRecord> allTags = DatabaseManager.getAllTags();
            List<TagRecord> photoTags = DatabaseManager.getTagsForPhoto(photo.photoId());
            
            for (TagRecord tag : photoTags) {
                Label tagLabel = new Label(tag.title());

                int rgb = tag.tagColor();
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                double luma = 0.299 * r + 0.587 * g + 0.114 * b;
                
                String textColor = (luma > 128) ? "black" : "white";
                String hexColor = String.format("#%06X", (0xFFFFFF & rgb));
                
                tagLabel.setStyle(
                    "-fx-background-color: " + hexColor + ";" +
                    "-fx-text-fill: " + textColor + ";" + 
                    "-fx-padding: 4 10 4 10;" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: #bbb; -fx-border-radius: 12;" +
                    "-fx-font-weight: bold;" +
                    "-fx-cursor: hand;"
                );
                
                ContextMenu removeMenu = new ContextMenu();
                MenuItem removeItem = new MenuItem("Remove Tag");
                removeItem.setOnAction(e -> {
                    try {
                        DatabaseManager.untagPhoto(photo.photoId(), tag.tagId());
                        populateTags(photo); 
                    } catch (SQLException ex) { ex.printStackTrace(System.err); }
                });
                removeMenu.getItems().add(removeItem);
                
                tagLabel.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        removeMenu.hide();
                        Session.setCurrentTag(tag);
                        App.navigate("tag_view");
                    } else if (e.getButton() == MouseButton.SECONDARY) {
                        removeMenu.show(tagLabel, e.getScreenX(), e.getScreenY());
                    }
                });
                
                tagFlowPane.getChildren().add(tagLabel);
            }
            
            MenuButton addTagBtn = new MenuButton("+");
            addTagBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-border-color: #888; -fx-border-radius: 12;" +
                "-fx-text-fill: #888;" +
                "-fx-padding: 2 8 2 8;" +
                "-fx-cursor: hand;"
            );
            
            for (TagRecord t : allTags) {
                if (photoTags.stream().noneMatch(pt -> pt.tagId() == t.tagId())) {
                    MenuItem item = new MenuItem(t.title());
                    item.setOnAction(e -> {
                        try {
                            DatabaseManager.tagPhoto(photo.photoId(), t.tagId());
                            populateTags(photo); 
                        } catch (SQLException ex) { ex.printStackTrace(System.err); }
                    });
                    addTagBtn.getItems().add(item);
                }
            }
            
            if (!addTagBtn.getItems().isEmpty()) {
                addTagBtn.getItems().add(new SeparatorMenuItem());
            }
            
            MenuItem createNewItem = new MenuItem("Create New Tag...");
            createNewItem.setOnAction(e -> promptCreateNewTag(photo));
            addTagBtn.getItems().add(createNewItem);
            
            tagFlowPane.getChildren().add(addTagBtn);
            
            updateImageContextMenu(photo, allTags, photoTags);
            
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    private void updateImageContextMenu(PhotoRecord photo, List<TagRecord> allTags, List<TagRecord> photoTags) {
        imageContextMenu.getItems().clear();
        
        Menu addTagMenu = new Menu("Add Tag");
        
        for (TagRecord t : allTags) {
            if (photoTags.stream().noneMatch(pt -> pt.tagId() == t.tagId())) {
                MenuItem item = new MenuItem(t.title());
                item.setOnAction(e -> {
                    try {
                        DatabaseManager.tagPhoto(photo.photoId(), t.tagId());
                        populateTags(photo);
                    } catch (SQLException ex) { ex.printStackTrace(System.err); }
                });
                addTagMenu.getItems().add(item);
            }
        }
        
        if (!addTagMenu.getItems().isEmpty()) {
            addTagMenu.getItems().add(new SeparatorMenuItem());
        }
        
        MenuItem createNewItem = new MenuItem("Create New Tag...");
        createNewItem.setOnAction(e -> promptCreateNewTag(photo));
        addTagMenu.getItems().add(createNewItem);
        
        imageContextMenu.getItems().add(addTagMenu);
    }

    private void promptCreateNewTag(PhotoRecord photo) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Tag");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter tag name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                try {
                    TagRecord newTag = DatabaseManager.createTag(name.trim(), 0xFFFFFF, null);
                    DatabaseManager.tagPhoto(photo.photoId(), newTag.tagId());
                    populateTags(photo); 
                } catch (SQLException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        });
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
        imageContextMenu.hide();
        if (albumPhotos == null || currentIndex <= 0) return;
        currentIndex--;
        PhotoRecord prev = albumPhotos.get(currentIndex);
        Session.setCurrentPhoto(prev);
        loadPhoto(prev);
    }

    @FXML
    private void onNext() {
        imageContextMenu.hide();
        if (albumPhotos == null || currentIndex >= albumPhotos.size() - 1) return;
        currentIndex++;
        PhotoRecord next = albumPhotos.get(currentIndex);
        Session.setCurrentPhoto(next);
        loadPhoto(next);
    }

    @FXML 
    private void onBack() { 
        imageContextMenu.hide(); 
        App.navigate("album_view"); 
    }

    private int findIndex(PhotoRecord photo) {
        if (albumPhotos == null) return 0;
        for (int i = 0; i < albumPhotos.size(); i++) {
            if (albumPhotos.get(i).photoId() == photo.photoId()) return i;
        }
        return 0;
    }
}