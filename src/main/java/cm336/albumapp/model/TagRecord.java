package cm336.albumapp.model;

/**
 * Represents a row from the Tags table.
 */
public record TagRecord(
    int tagId,
    String title,
    int tagColor,   // stored as mediumint, treat as RGB int
    String tagType  // nullable
) {}
