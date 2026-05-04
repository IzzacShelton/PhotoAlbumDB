package cm336.albumapp.metadata;

/**
 * Simple pair used as the item type for the metadata TreeTableView in PhotoView.
 * Represents either a directory header (value is empty) or a single tag row.
 */
public record MetaRow(String name, String value) {}
