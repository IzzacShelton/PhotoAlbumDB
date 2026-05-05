package cm336.albumapp;

import atlantafx.base.theme.CupertinoDark;
import cm336.albumapp.controller.AppController;
import cm336.albumapp.db.DatabaseManager;
import cm336.albumapp.model.UserRecord;
import java.io.IOException;
import java.io.File;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.awt.Desktop;

/**
 * JavaFX application class. Responsible for:
 *   - Setting the global AtlantaFX theme
 *   - Loading the persistent shell (app.fxml)
 *   - Resolving or creating the current user from the OS username
 *   - Providing the static navigate() method used by all controllers
 */
public class App extends Application {

    private static AppController appController;

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

        String osUsername = System.getProperty("user.name");
        UserRecord user = DatabaseManager.findOrCreateUser(osUsername);
        Session.setCurrentUser(user);

        FXMLLoader loader = new FXMLLoader(App.class.getResource("app.fxml"));
        Parent shell = loader.load();
        appController = loader.getController();

        Scene scene = new Scene(shell, 1100, 720);
        stage.setTitle("PhotoAlbum");
        stage.setScene(scene);
        stage.show();

        // Start on the album gallery.
        navigate("album_gallery");
    }

    /**
     * Navigates to a view by loading its FXML into the shell content area.
     * The FXML file must exist at resources/cm336/albumapp/{name}.fxml.
     * Set any Session state before calling this so the incoming controller
     * can read it in its initialize() method.
     *
     * @param name the FXML filename without extension
     */
    public static void navigate(String name) {
        if (appController == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(name + ".fxml"));
            Parent view = loader.load();
            appController.setContent(view);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + name, e);
        }
    }

    public static void setTheme(String stylesheet) {
        Application.setUserAgentStylesheet(stylesheet);
    }

    public static void main(String[] args) {
        launch();
    }
    public static void showInExplorer(String filepath) {
        try {
            File file = new File(filepath);
            if (file.exists()) {
                Desktop.getDesktop().browseFileDirectory(file);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
