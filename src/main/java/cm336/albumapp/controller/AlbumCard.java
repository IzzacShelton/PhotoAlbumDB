package cm336.albumapp.controller;

import cm336.albumapp.model.AlbumRecord;
import java.io.File;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class AlbumCard extends VBox {

    private static final int CARD_SIZE = 150;

    public AlbumCard(AlbumRecord album, String coverImagePath, Runnable onClick) {
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
        setOnMouseClicked(e -> onClick.run());
        setStyle("-fx-cursor: hand; -fx-padding: 8;");
    }
}
