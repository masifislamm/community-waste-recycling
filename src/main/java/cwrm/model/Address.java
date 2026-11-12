package cwrm.model;

/**
 * A pickup address, usually returned by the Nominatim address lookup.
 *
 * @param displayName full address text shown to the user
 * @param latitude    latitude in decimal degrees
 * @param longitude   longitude in decimal degrees
 */
public record Address(String displayName, double latitude, double longitude) {
}
