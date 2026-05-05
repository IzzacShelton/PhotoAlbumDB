package cm336.albumapp.controller;

import cm336.albumapp.App;
import cm336.albumapp.Session;
import cm336.albumapp.model.PhotoRecord;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

public class PhotoThumbnail extends VBox {
    public final int photoId;
    private static final int THUMB_SIZE = 120;

    public PhotoThumbnail(PhotoRecord photo, EventHandler<ActionEvent> onDelete) {
        super(6);
        this.photoId = photo.photoId();
        
        setAlignment(Pos.CENTER);
        setPrefWidth(THUMB_SIZE);
        setMaxWidth(THUMB_SIZE);
        getStyleClass().add("photo-thumbnail");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(THUMB_SIZE);
        imageView.setFitHeight(THUMB_SIZE);
        imageView.setPreserveRatio(true);

        File file = new File(photo.filepath());
        if (file.exists()) {
            imageView.setImage(new Image(file.toURI().toString(),
                THUMB_SIZE, THUMB_SIZE, true, true, true));
        }

        Label nameLabel = new Label(file.getName());
        nameLabel.setMaxWidth(THUMB_SIZE);
        nameLabel.setStyle("-fx-font-size: 11px;");

        getChildren().addAll(imageView, nameLabel);
        
        ContextMenu contextMenu = new ContextMenu();

        MenuItem deleteItem = new MenuItem("Remove Photo");
        deleteItem.setOnAction(onDelete);

        contextMenu.getItems().addAll(deleteItem);
        setOnMouseClicked(
            e -> {
                if (contextMenu.isShowing())
                    contextMenu.hide();
                
                switch (e.getButton()){
                    case MouseButton.PRIMARY -> {
                        Session.setCurrentPhoto(photo); 
                        App.navigate("photo_view");
                    } case MouseButton.SECONDARY -> {
                        contextMenu.show(this, e.getScreenX(), e.getScreenY());
                    }
                }
            }
        );
        
        setStyle("-fx-cursor: hand; -fx-padding: 4;");
    }
}