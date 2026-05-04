package cm336.albumapp.controller;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import cm336.albumapp.App;
import cm336.albumapp.Session;
import cm336.albumapp.db.DatabaseManager;
import cm336.albumapp.metadata.MetadataExtractor;
import cm336.albumapp.metadata.PhotoMetadata;
import com.drew.imaging.ImageProcessingException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class AppController {

    private static final List<String> IMAGE_EXTENSIONS =
        Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".webp");

    @FXML private StackPane contentArea;

    public void setContent(Parent view) {
        contentArea.getChildren().setAll(view);
    }

    @FXML
    private void onImportPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Import Photo");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files",
                "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.tiff", "*.webp")
        );
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fc.showOpenDialog(contentArea.getScene().getWindow());
        if (file != null) {
            importFile(file);
        }
    }

    @FXML
    private void onImportFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Import Folder");
        dc.setInitialDirectory(new File(System.getProperty("user.home")));
        File dir = dc.showDialog(contentArea.getScene().getWindow());
        if (dir == null) return;

        File[] files = dir.listFiles(f ->
            IMAGE_EXTENSIONS.stream().anyMatch(ext ->
                f.getName().toLowerCase().endsWith(ext)));

        if (files != null) {
            for (File f : files) importFile(f);
        }

        App.navigate("album_gallery");
    }

    private void importFile(File file) {
        try {
            PhotoMetadata meta = MetadataExtractor.extract(file);
            DatabaseManager.importPhoto(meta, Session.getCurrentUser().userId());
        } catch (IOException | ImageProcessingException | SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    @FXML private void onGoHome()    { App.navigate("album_gallery"); }
    @FXML private void onGoTags()    { App.navigate("tag_view"); }
    @FXML private void onThemeDark() { App.setTheme(new CupertinoDark().getUserAgentStylesheet()); }
    @FXML private void onThemeLight(){ App.setTheme(new CupertinoLight().getUserAgentStylesheet()); }
}
