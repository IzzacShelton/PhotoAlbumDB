package cm336.albumapp.controller;

import cm336.albumapp.model.PhotoRecord;
import java.io.File;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class PhotoThumbnail extends VBox {

    private static final int THUMB_SIZE = 120;

    public PhotoThumbnail(PhotoRecord photo, Runnable onClick) {
        super(6);
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
        setOnMouseClicked(e -> onClick.run());
        setStyle("-fx-cursor: hand; -fx-padding: 4;");
    }
}
