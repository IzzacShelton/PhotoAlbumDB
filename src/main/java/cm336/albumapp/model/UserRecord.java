package cm336.albumapp.model;

import java.time.LocalDateTime;

/**
 * Represents a row from the Users table.
 */
public record UserRecord(
    int userId,
    String name,
    LocalDateTime dateJoined
) {}
