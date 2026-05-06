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
    String selectAlbums = "select * from Album where OwnerID = ? order by AlbumType, AlbumName desc;";
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
    String 
        insAlbum = """
            insert into Album (OwnerID, AlbumName, AlbumDescription, AlbumType) 
              values (?, ?, ?, 'User');""",
        selAlbum = """
            select * from Album
            where AlbumID = last_insert_id();""";
    try (Connection con = getConnection();
         PreparedStatement ins = con.prepareStatement(insAlbum);
         PreparedStatement sel = con.prepareStatement(selAlbum)) {
        
        // set insert params;
        ins.setInt(1, ownerId);
        ins.setString(2, name);
        ins.setString(3, description);
        ins.execute();
        
        ///
        ResultSet rs = sel.executeQuery();
        rs.next();
        return mapAlbum(rs);
    } 
  }

  /**
   * Deletes an album. Must not allow deleting the Library/Year albums
   * (AlbumType='User')
   */
  public static void deleteAlbum(int albumId) throws SQLException {
    String delAlbum = """
        delete from Album
        where AlbumID = ? and AlbumType = 'User';
        """;
    try (Connection con = getConnection();
         PreparedStatement del = con.prepareStatement(delAlbum)) {
        del.setInt(1, albumId);
        del.execute();
    } 
  }

  /**
   * Returns a random photo filepath from an album for cover thumbnails. null if
   * the album is empty.
   */
  public static String getRandomThumbnailPath(int albumId) throws SQLException {
    String selThumb = """
        select Filepath 
        from Photo P join  Album_Photo AP 
          on P.PhotoID = AP.PhotoID
          where AlbumID = ?
        order by rand()
        limit 1;
    """;
    try (Connection con = getConnection();
         PreparedStatement sel = con.prepareStatement(selThumb)) {
        sel.setInt(1, albumId);
        ResultSet r = sel.executeQuery();
        
        return r.next() ? r.getString("Filepath") : null;
    } 
  }

  /**
   * Returns the number of photos in an album.
   */
  public static int getPhotoCount(int albumId) throws SQLException {
    String selCount = "select fn_PhotoCount(?) as ct;";
            
    try (Connection con = getConnection();
         PreparedStatement sel = con.prepareStatement(selCount)) {
        sel.setInt(1, albumId);
        ResultSet r = sel.executeQuery();
        r.next();
        
        return r.getInt("ct");
    } 
  }

  /**
   * Returns all photos in an album sorted by date taken, then date added.
   */
  public static List<PhotoRecord> getPhotosInAlbum(int albumId) throws SQLException {
    String selPhotos = """
        select P.* 
        from Photo P join  Album_Photo AP 
          on P.PhotoID = AP.PhotoID
          where AlbumID = ?
        order by P.DateTimeTaken, P.DateTimeAdded;
    """;
    try (Connection con = getConnection();
         PreparedStatement sel = con.prepareStatement(selPhotos)) {
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
  public static List<PhotoRecord> getPhotosInAlbumByTag(int albumId, int tagId) throws SQLException {
    String selPhoByTag = """
        select P.*
        from 
          Photo P join (
            select PT.PhotoID, PT.TagID, AP.AlbumID
            from Album_Photo AP join Photo_Tag PT
            on AP.PhotoID = PT.PhotoID
          ) J on J.PhotoID = P.PhotoID
        where J.AlbumID = ?
          and J.TagID = ?;
    """;
    try (Connection con = getConnection();
         PreparedStatement sel = con.prepareStatement(selPhoByTag)) {
        sel.setInt(1, albumId);
        sel.setInt(2, tagId);
        ResultSet rs = sel.executeQuery();
        
        List<PhotoRecord> photos = new ArrayList<>();
        while (rs.next()) {
            photos.add(mapPhoto(rs));
        }
        
        return photos;
    }
  }

  /**
   * Imports a photo into the database from an extracted PhotoMetadata record.
   */
  public static PhotoRecord importPhoto(PhotoMetadata meta, int userId) throws SQLException {
    String callIns = "call sp_InsertPhoto(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", // oh the setters...
           selPhoto= "select * from Photo where Filepath = ?;";
    
    try (Connection con = getConnection();
         PreparedStatement ins = con.prepareStatement(callIns);
         PreparedStatement sel = con.prepareStatement(selPhoto)) {
        // sp_InsertPhoto(int, String, long, Double, Double, int, int, LocalDateTime, String, String, String); 
        ins.setInt(1, userId);                             // in u_UserID int,
        ins.setString(2, meta.filepath());                 // in p_Filepath varchar(256),
        ins.setLong(3, meta.fileSize());                   // in p_FileSize bigint,
        ins.setObject(4, meta.latitude(), Types.DECIMAL);  // in p_Latitude decimal(8,6),
        ins.setObject(5, meta.longitude(), Types.DECIMAL); // in p_Longitude decimal(9,6),
        ins.setInt(6, meta.imageWidth());                  // in p_ImageWidth int unsigned,
        ins.setInt(7, meta.imageHeight());                 // in p_ImageHeight int unsigned,
        ins.setObject(8, meta.dateTimeTaken());            // in p_DateTimeTaken datetime,
        ins.setString(9, meta.cameraBrand());              // in c_Brand varchar(100),
        ins.setString(10, meta.cameraModel());             // in c_Model varchar(100),
        ins.setString(11, meta.cameraSerial());            // in c_SerialNumber varchar(100)
        ins.execute();
        
        // select photo by its filepath
        sel.setString(1, meta.filepath());
        ResultSet rs = sel.executeQuery();
        rs.next();
        
        return mapPhoto(rs);
    } 
  }

  /**
   * Associates an existing photo with an album. Does nothing if AlbumPhoto
   * record already exists (INSERT IGNORE)
   */
  public static void addPhotoToAlbum(int albumId, int photoId) throws SQLException {
    String insPhoto = """
        insert ignore into Album_Photo (AlbumID, PhotoID)
          values (?, ?);
    """;
    try (Connection con = getConnection();
         PreparedStatement ins = con.prepareStatement(insPhoto)) {
        ins.setInt(1, albumId);
        ins.setInt(2, photoId);
        
        // run it
        ins.execute();
    } 
  }

  /**
   * Removes a photo from a User album without deleting the Photo row itself.
   */
  public static void removePhotoFromAlbum(int albumId, int photoId) throws SQLException {
    String callRemove = "call sp_RemovePhotoFromAlbum(?, ?);";
    try (Connection con = getConnection();
         PreparedStatement call = con.prepareStatement(callRemove)) {
        call.setInt(1, albumId);
        call.setInt(2, photoId);
        
        ResultSet rs = call.executeQuery();
        System.out.println(rs);
        PreparedStatement s = con.prepareStatement("select * from Album_Photo where AlbumID = ? and PhotoID = ?;");
        s.setInt(1, albumId);
        s.setInt(2, photoId);
        ResultSet test = s.executeQuery();
    } 
  }

  /**
   * Returns all tags ordered alphabetically.
   */
  public static List<TagRecord> getAllTags() throws SQLException {
    String selTags = """
        select * 
        from Tags 
        order by Title;
    """;
    try (Connection con = getConnection();
         PreparedStatement sel = con.prepareStatement(selTags)) {
        ResultSet rs = sel.executeQuery();
        
        List<TagRecord> tags = new ArrayList<>();
        while (rs.next()) {
            tags.add(mapTag(rs));
        }
        
        return tags;
    } 
  }

  /**
   * Creates a new custom tag. Color is stored as an RGB int (e.g. 0xFFFFFF).
   * TagType can be null.
   */
  public static TagRecord createTag(String title, int color, String tagType) throws SQLException {
    String 
        insTag = """
            insert into Tags (Title, TagColor, TagType)
              values (?, ?, ?);""",
        selTag = """
            select * from Tags
            where TagID = last_insert_id();""";
    try (Connection con = getConnection();
         PreparedStatement ins = con.prepareStatement(insTag);
         PreparedStatement sel = con.prepareStatement(selTag)) {
        
        // set insert params;
        ins.setString(1, title);
        ins.setInt(2, color);
        ins.setString(3, tagType);
        ins.execute();
        
        ResultSet rs = sel.executeQuery();
        rs.next();
        return mapTag(rs);
    } 
  }

  /**
   * Deletes a tag. PhotoTag rows referencing it must be cleaned up first —
   * either via ON DELETE CASCADE on PhotoTag.TagID or an explicit delete.
   */
  public static void deleteTag(int tagId) throws SQLException {
    String delTag = """
        delete from Tags 
        where TagID = ?;
    """;
    try (Connection con = getConnection();
         PreparedStatement del = con.prepareStatement(delTag)) {
        del.setInt(1, tagId);
        del.execute();
    } 
  }

  /**
   * Applies a tag to a photo. Does nothing if already applied (INSERT IGNORE)
   */
  public static void tagPhoto(int photoId, int tagId) throws SQLException {
    String insPhotoTag = """
        insert ignore into Photo_Tag (PhotoID, TagID)
          values (?, ?);
    """;
    try (Connection con = getConnection();
         PreparedStatement ins = con.prepareStatement(insPhotoTag)) {
        ins.setInt(1, photoId);
        ins.setInt(2, tagId);
        
        ins.execute();
    } 
  }
  /**
  * Returns all tags applied to a specific photo.
  */
  public static List<TagRecord> getTagsForPhoto(int photoId) throws SQLException {
    String selTags = """
        select T.* from Tags T join Photo_Tag PT
          on T.TagID = PT.TagID
        where PT.PhotoID = ?
        order by T.Title;
    """;
    try (Connection con = getConnection();
         PreparedStatement sel = con.prepareStatement(selTags)) {
        sel.setInt(1, photoId);
        ResultSet rs = sel.executeQuery();
        
        List<TagRecord> tags = new ArrayList<>();
        while (rs.next()) {
            tags.add(mapTag(rs));
        }
        return tags;
    }
  }
  
  /**
   * Removes a tag from a photo.
   */
  public static void untagPhoto(int photoId, int tagId) throws SQLException {
    String delPhotoTag = """
        delete from Photo_Tag 
          where PhotoID = ?
            and TagID = ?;
    """;
    try (Connection con = getConnection();
         PreparedStatement del = con.prepareStatement(delPhotoTag)) {
        del.setInt(1, photoId);
        del.setInt(2, tagId);
        
        del.execute();
    } 
  }
  
  /**
  * Updates the color of an existing tag.
  */
  public static void updateTagColor(int tagId, int color) throws SQLException {
      String updColor = "update Tags set TagColor = ? where TagID = ?;";
      try (Connection con = getConnection();
           PreparedStatement upd = con.prepareStatement(updColor)) {
          upd.setInt(1, color);
          upd.setInt(2, tagId);
          upd.execute();
      }
  }
  
  /**
   * Returns all photos that have a given tag.
   */
  public static List<PhotoRecord> getPhotosByTag(int tagId) throws SQLException {
    String selPhotos = """
        select P.* 
        from Photo P join Photo_Tag PT
          on P.PhotoID = PT.PhotoID
          where TagID = ?;
    """;
    try (Connection con = getConnection();
         PreparedStatement sel = con.prepareStatement(selPhotos)) {
        sel.setInt(1, tagId);
        
        ResultSet rs = sel.executeQuery();
        
        List<PhotoRecord> photos = new ArrayList<>();
        while (rs.next()) {
            photos.add(mapPhoto(rs));
        }
        return photos;
    } 
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
