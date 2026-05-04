package cm336.albumapp.model;

import java.time.LocalDateTime;

public record AlbumRecord(
    int albumId,
    int ownerId,
    String albumName,
    String albumDescription,
    String albumType,       // ENUM: 'Library', 'User', 'Auto'
    LocalDateTime createdAt,
    LocalDateTime albumUpdated
) {
    public boolean isLibrary() { return "Library".equals(albumType); }
    public boolean isAuto()    { return "Auto".equals(albumType); }
    public boolean isUser()    { return "User".equals(albumType); }
}
