package cm336.albumapp;

/**
 * Thin entry point used by the shade plugin manifest.
 * JavaFX apps packaged as fat jars need a non-JavaFX main class
 * to avoid classloader issues at startup.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
