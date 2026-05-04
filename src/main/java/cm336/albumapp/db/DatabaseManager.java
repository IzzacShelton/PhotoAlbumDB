package cm336.albumapp.db;

import cm336.albumapp.metadata.PhotoMetadata;
import cm336.albumapp.model.AlbumRecord;
import cm336.albumapp.model.PhotoRecord;
import cm336.albumapp.model.TagRecord;
import cm336.albumapp.model.UserRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseManager {
  
  private static final String URL = "jdbc:mysql://localhost:3306/PhotoApp";
  private static final String USER = "root";
  private static final String PASS = "root";
  
  private static Connection connection;
  
  private DatabaseManager() {
  }
  
  public static Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
      connection = DriverManager.getConnection(URL, USER, PASS);
    }
    return connection;
  }

  /**
   * Finds or creates User record by username, or silently create one on first
   * run.
   */
  public static UserRecord findOrCreateUser(String username) throws SQLException {
    String insIgnoreUser = "insert ignore into Users (Name) values (?);",
            selectUserRow = "select * from Users where Name = ? limit 1;";
    try ( // ugly syntax but Its required to close them automatically
            Connection con = getConnection(); 
            PreparedStatement ins = con.prepareStatement(insIgnoreUser); 
            PreparedStatement sel = con.prepareStatement(selectUserRow)) {
      
        // format username into queries
        ins.setString(1, username);
        sel.setString(1, username);

        // inserts the new user record if its not already there
        ins.execute();

        // submit query + select the row
        ResultSet rs = sel.executeQuery();
        rs.next();

        // map it to the record and return
        return mapUser(rs);
    }
  }

  /**
   * Returns all albums owned by the given user.
   */
  public static List<AlbumRecord> getAlbumsForUser(int userId) throws SQLException {
    String selectAlbums = "select * from Album where OwnerID = ?;";
    try (Connection con = getConnection(); 
         PreparedStatement sel = con.prepareStatement(selectAlbums);) {
      
      sel.setInt(1, userId);
      ResultSet rs = sel.executeQuery();

      // populate list with records
      List<AlbumRecord> albums = new ArrayList<>();
      while (rs.next()) {
        albums.add(mapAlbum(rs));
      }
      
      return albums;
    }
  }

  /**
   * Creates a new User album
   */
  public static AlbumRecord createAlbum(int ownerId, String name, String description) throws SQLException {
    
    throw new UnsupportedOperationException("createAlbum not yet implemented");
  }

  /**
   * Deletes an album. Must not allow deleting the Library/Year albums
   * (AlbumType='User')
   */
  public static void deleteAlbum(int albumId) throws SQLException {
    
    throw new UnsupportedOperationException("deleteAlbum not yet implemented");
  }

  /**
   * Returns a random photo filepath from an album for cover thumbnails. null if
   * the album is empty.
   */
  public static String getRandomThumbnailPath(int albumId) throws SQLException {
    String selectRandom = "select Filepath from Photo where AlbumID = ? order by rand() limit 1";
    try (
            Connection con = getConnection(); 
            PreparedStatement sel = con.prepareStatement(selectRandom);
        ) {
        
        sel.setInt(1, albumId);
        ResultSet rs = sel.executeQuery();
        rs.next();
        
        return rs.getString("Filepath");
    }
  }

  /**
   * Returns the number of photos in an album.
   */
  public static int getPhotoCount(int albumId) throws SQLException {
    String selGetCount = "select photo_count(?) as ct;";
    try (
            Connection con = getConnection(); 
            PreparedStatement sel = con.prepareStatement(selGetCount);
        ) {
        sel.setInt(1, albumId);
        ResultSet rs = sel.executeQuery();
        rs.next();
        
        return rs.getInt("ct");
    }
  }

  /**
   * Returns all photos in an album sorted by date taken, then date added.
   */
  public static List<PhotoRecord> getPhotosInAlbum(int albumId) throws SQLException {
    String selectPhotos = """
        select * 
        from Photo P 
            join (
                select AlbumID 
                from AlbumPhoto
                where AlbumID = ?
            ) as S 
        on P.AlbumID = S.AlbumID""";
    try (
            Connection con = getConnection(); 
            PreparedStatement sel = con.prepareStatement(selectPhotos);
        ) {
        sel.setInt(1, albumId);
        ResultSet rs = sel.executeQuery();
        
        List<PhotoRecord> photos = new ArrayList<>();
        while (rs.next()) {
            photos.add(mapPhoto(rs));
        }
        return photos;
    }
  }

  /**
   * Returns all photos in an album that have a specific tag applied.
   */
  public static List<PhotoRecord> getPhotosInAlbumByTag(int albumId, int tagId)
         throws SQLException {
    
    throw new UnsupportedOperationException("getPhotosInAlbumByTag not yet implemented");
  }

  /**
   * Imports a photo into the database from an extracted PhotoMetadata record.
   */
  public static PhotoRecord importPhoto(PhotoMetadata meta, int userId) throws SQLException {
    // TODO
    throw new UnsupportedOperationException("importPhoto not yet implemented");
  }

  /**
   * Associates an existing photo with an album. Does nothing if AlbumPhoto
   * record already exists (INSERT IGNORE)
   */
  public static void addPhotoToAlbum(int albumId, int photoId) throws SQLException {
    // TODO
    throw new UnsupportedOperationException("addPhotoToAlbum not yet implemented");
  }

  /**
   * Removes a photo from a User album without deleting the Photo row itself.
   */
  public static void removePhotoFromAlbum(int albumId, int photoId) throws SQLException {
    // TODO
    throw new UnsupportedOperationException("removePhotoFromAlbum not yet implemented");
  }

  /**
   * Returns all tags ordered alphabetically.
   */
  public static List<TagRecord> getAllTags() throws SQLException {
    // TODO
    throw new UnsupportedOperationException("getAllTags not yet implemented");
  }

  /**
   * Creates a new custom tag. Color is stored as an RGB int (e.g. 0xFFFFFF).
   * TagType can be null.
   */
  public static TagRecord createTag(String title, int color, String tagType)
          throws SQLException {
    // TODO
    throw new UnsupportedOperationException("createTag not yet implemented");
  }

  /**
   * Deletes a tag. PhotoTag rows referencing it must be cleaned up first —
   * either via ON DELETE CASCADE on PhotoTag.TagID or an explicit delete.
   */
  public static void deleteTag(int tagId) throws SQLException {
    // TODO
    throw new UnsupportedOperationException("deleteTag not yet implemented");
  }

  /**
   * Applies a tag to a photo. Does nothing if already applied (INSERT IGNORE)
   */
  public static void tagPhoto(int photoId, int tagId) throws SQLException {
    // TODO
    throw new UnsupportedOperationException("tagPhoto not yet implemented");
  }

  /**
   * Removes a tag from a photo.
   */
  public static void untagPhoto(int photoId, int tagId) throws SQLException {
    // TODO
    throw new UnsupportedOperationException("untagPhoto not yet implemented");
  }

  /**
   * Returns all photos that have a given tag.
   */
  public static List<PhotoRecord> getPhotosByTag(int tagId) throws SQLException {
    // TODO
    throw new UnsupportedOperationException("getPhotosByTag not yet implemented");
  }

  // ResultSet Record mappers to translate raw SQL rows into record types
  static UserRecord mapUser(ResultSet rs) throws SQLException {
    return new UserRecord(
            rs.getInt("UserId"),
            rs.getString("Name"),
            rs.getObject("DateJoined", LocalDateTime.class)
    );
  }
  
  static AlbumRecord mapAlbum(ResultSet rs) throws SQLException {
    return new AlbumRecord(
            rs.getInt("AlbumID"),
            rs.getInt("OwnerID"),
            rs.getString("AlbumName"),
            rs.getString("AlbumDescription"),
            rs.getString("AlbumType"),
            rs.getObject("CreatedAt", LocalDateTime.class),
            rs.getObject("AlbumUpdated", LocalDateTime.class)
    );
  }
  
  static PhotoRecord mapPhoto(ResultSet rs) throws SQLException {
    return new PhotoRecord(
            rs.getInt("PhotoID"),
            rs.getObject("CameraID", Integer.class),
            rs.getString("Filepath"),
            rs.getLong("FileSize"),
            rs.getObject("Latitude", Double.class),
            rs.getObject("Longitude", Double.class),
            rs.getInt("ImageWidth"),
            rs.getInt("ImageHeight"),
            rs.getObject("DateTimeTaken", LocalDateTime.class),
            rs.getObject("DateTimeAdded", LocalDateTime.class)
    );
  }
  
  static TagRecord mapTag(ResultSet rs) throws SQLException {
    return new TagRecord(
            rs.getInt("TagID"),
            rs.getString("Title"),
            rs.getInt("TagColor"),
            rs.getString("TagType")
    );
  }
}
