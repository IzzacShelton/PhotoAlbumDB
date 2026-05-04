package cm336.albumapp;

import cm336.albumapp.model.AlbumRecord;
import cm336.albumapp.model.PhotoRecord;
import cm336.albumapp.model.TagRecord;
import cm336.albumapp.model.UserRecord;

/**
 * Holds application-wide state for the current session.
 * This is the single source of truth for which user is logged in
 * and what the user is currently looking at. Controllers read from
 * here after navigation rather than passing data through method calls.
 *
 * Nothing here persists between runs — it is all reconstructed from
 * the database on startup or on user action.
 */
public final class Session {

    /** The user derived from the OS login name on first run. */
    private static UserRecord currentUser;

    /** The album currently open in AlbumView. */
    private static AlbumRecord currentAlbum;

    /** The photo currently open in PhotoView. */
    private static PhotoRecord currentPhoto;

    /** The tag currently selected in TagView. */
    private static TagRecord currentTag;

    private Session() {}

    public static UserRecord getCurrentUser() { return currentUser; }
    public static void setCurrentUser(UserRecord user) { currentUser = user; }

    public static AlbumRecord getCurrentAlbum() { return currentAlbum; }
    public static void setCurrentAlbum(AlbumRecord album) { currentAlbum = album; }

    public static PhotoRecord getCurrentPhoto() { return currentPhoto; }
    public static void setCurrentPhoto(PhotoRecord photo) { currentPhoto = photo; }

    public static TagRecord getCurrentTag() { return currentTag; }
    public static void setCurrentTag(TagRecord tag) { currentTag = tag; }
}
