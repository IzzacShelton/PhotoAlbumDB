package cm336.albumapp.controller;

import cm336.albumapp.App;
import cm336.albumapp.Session;
import cm336.albumapp.db.DatabaseManager;
import cm336.albumapp.model.PhotoRecord;
import cm336.albumapp.model.TagRecord;
import java.sql.SQLException;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.util.StringConverter;

public class TagViewController {

    @FXML private ListView<TagRecord> tagList;
    @FXML private FlowPane tagPhotoGrid;
    @FXML private TextField newTagNameField;

    @FXML
    public void initialize() {
        tagList.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(TagRecord tag, boolean empty) {
                super.updateItem(tag, empty);
                setText(empty || tag == null ? null : tag.title());
            }
        });

        loadTags();

        tagList.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) loadPhotosForTag(newVal);
            }
        );
    }

    private void loadTags() {
        tagList.getItems().clear();
        try {
            tagList.getItems().addAll(DatabaseManager.getAllTags());
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    private void loadPhotosForTag(TagRecord tag) {
        tagPhotoGrid.getChildren().clear();
        try {
            List<PhotoRecord> photos = DatabaseManager.getPhotosByTag(tag.tagId());
            for (PhotoRecord photo : photos) {
                tagPhotoGrid.getChildren().add(new PhotoThumbnail(photo, () -> {
                    Session.setCurrentPhoto(photo);
                    App.navigate("photo_view");
                }));
            }
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    @FXML
    private void onCreateTag() {
        String name = newTagNameField.getText().trim();
        if (name.isEmpty()) return;
        try {
            DatabaseManager.createTag(name, 0xFFFFFF, null);
            newTagNameField.clear();
            loadTags();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    @FXML
    private void onDeleteTag() {
        TagRecord selected = tagList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete tag \"" + selected.title() + "\"? This cannot be undone.",
            ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    DatabaseManager.deleteTag(selected.tagId());
                    tagPhotoGrid.getChildren().clear();
                    loadTags();
                } catch (SQLException e) {
                    e.printStackTrace(System.err);
                }
            }
        });
    }
}
