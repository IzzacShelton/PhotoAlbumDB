package cm336.albumapp.controller;

import cm336.albumapp.Session;
import cm336.albumapp.db.DatabaseManager;
import cm336.albumapp.model.PhotoRecord;
import cm336.albumapp.model.TagRecord;
import java.sql.SQLException;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

public class TagViewController {

    @FXML private ListView<TagRecord> tagList;
    @FXML private FlowPane tagPhotoGrid;
    @FXML private TextField newTagNameField;
    @FXML private ColorPicker tagColorPicker;
    
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
                if (newVal != null) {
                    loadPhotosForTag(newVal);
                    int rgb = newVal.tagColor();
                    tagColorPicker.setValue(Color.rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
                    tagColorPicker.setDisable(false);
                } else {
                    tagColorPicker.setDisable(true);
                }
            }
        );

        TagRecord sessionTag = Session.getCurrentTag();
        if (sessionTag != null) {
            for (TagRecord t : tagList.getItems()) {
                if (t.tagId() == sessionTag.tagId()) {
                    tagList.getSelectionModel().select(t);
                    break;
                }
            }
            Session.setCurrentTag(null); 
        }

        newTagNameField.setOnAction((e) -> {
          onCreateTag();
        });
    }

    @FXML
    private void onColorChanged() {
        TagRecord selected = tagList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Color c = tagColorPicker.getValue();
        int rgb = ((int)(c.getRed() * 255) << 16) | 
                  ((int)(c.getGreen() * 255) << 8) | 
                  ((int)(c.getBlue() * 255));

        try {
            DatabaseManager.updateTagColor(selected.tagId(), rgb);
            loadTags();
            for (TagRecord t : tagList.getItems()) {
                if (t.tagId() == selected.tagId()) {
                    tagList.getSelectionModel().select(t);
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
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
                PhotoThumbnail thumb = new PhotoThumbnail(photo, e -> {
                    try {
                        DatabaseManager.untagPhoto(photo.photoId(), tag.tagId());
                        tagPhotoGrid.getChildren().removeIf(
                            n -> (n instanceof PhotoThumbnail) && ((PhotoThumbnail)n).photoId == photo.photoId()
                        );
                    } catch (SQLException ex) {
                        ex.printStackTrace(System.err);
                    }
                });

                tagPhotoGrid.getChildren().add(thumb);
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
