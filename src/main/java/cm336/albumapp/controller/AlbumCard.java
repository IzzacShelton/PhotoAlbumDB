package cm336.albumapp.controller;

import cm336.albumapp.model.AlbumRecord;
import java.io.File;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class AlbumCard extends VBox {

    private static final int CARD_SIZE = 128;

    public AlbumCard(AlbumRecord album, String coverImagePath, Runnable onClick, Runnable onDelete, Runnable onTagAll) {
        super(8);
        setAlignment(Pos.CENTER);
        setPrefWidth(CARD_SIZE);
        setMaxWidth(CARD_SIZE);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(CARD_SIZE);
        imageView.setFitHeight(CARD_SIZE);
        imageView.setPreserveRatio(true);

        if (coverImagePath != null) {
            File file = new File(coverImagePath);
            if (file.exists()) {
                imageView.setImage(new Image(file.toURI().toString(),
                    CARD_SIZE, CARD_SIZE, true, true, true));
            }
        }

        Label nameLabel = new Label(album.albumName());
        nameLabel.setMaxWidth(CARD_SIZE);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        getChildren().addAll(imageView, nameLabel);
        
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete Album");
        deleteItem.setOnAction(e -> onDelete.run());

        MenuItem tagAllItem = new MenuItem("Tag All Photos...");
        tagAllItem.setOnAction(e -> onTagAll.run());
        
        contextMenu.getItems().addAll(deleteItem, tagAllItem);

        // Update the click handler to support both buttons
        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                onClick.run();
            } else if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(this, e.getScreenX(), e.getScreenY());
            }
        });
    
        setStyle("-fx-cursor: hand; -fx-padding: 8;");
    }
}