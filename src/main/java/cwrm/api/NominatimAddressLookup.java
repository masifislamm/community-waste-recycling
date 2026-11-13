package cwrm.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cwrm.model.Address;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Turns an address typed by a resident into an {@link Address} with coordinates,
 * using the OpenStreetMap Nominatim search API.
 *
 * <p>The JSON response is read with Jackson's {@link ObjectMapper}. Every outcome,
 * including errors, becomes a {@link LookupResult} with a friendly message, so no
 * exception text or stack trace ever reaches the user. When the service fails, the
 * last address found is returned instead (graceful degradation). Requests are spaced
 * at least one second apart, as the Nominatim usage policy requires.
 */
public class NominatimAddressLookup {
    private static final String SEARCH_URL = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=";
    private static final Duration MINIMUM_GAP = Duration.ofSeconds(1);
    private static final int FIRST_SUCCESS_STATUS = 200;
    private static final int LAST_SUCCESS_STATUS = 299;
    private static final int NOT_FOUND_STATUS = 404;

    static final String FOUND = "Address found.";
    static final String NO_MATCH = "We could not find that address. Please check it and try again.";
    static final String LOCATION_NOT_FOUND = "Address service could not find that location.";
    static final String SERVICE_PROBLEM = "The address service is having problems. Please try again shortly.";
    static final String UNAVAILABLE = "Address service unavailable.";
    static final String UNREADABLE = "Address service returned data we could not read.";
    static final String LAST_KNOWN_NOTE = " Showing the last address found.";

    private final HttpTransport transport;
    private final ObjectMapper mapper = new ObjectMapper();
    private final long minimumGapNanos;
    private long lastRequestNanos;
    private Address lastKnown;

    /**
     * Creates a lookup that sends at most one request per second.
     *
     * @param transport sends the HTTP requests; tests pass in a fake
     */
    public NominatimAddressLookup(HttpTransport transport) {
        this(transport, MINIMUM_GAP);
    }

    /** Lets tests use a shorter gap between requests so they run quickly. */
    NominatimAddressLookup(HttpTransport transport, Duration minimumGap) {
        this.transport = transport;
        this.minimumGapNanos = minimumGap.toNanos();
        this.lastRequestNanos = System.nanoTime() - minimumGapNanos;
    }

    /**
     * Searches for an address.
     *
     * @param query the address as typed by the user, for example {@code Menara KL}
     * @return the address found (or the last known one after a failure) and a message for the user
     */
    public synchronized LookupResult lookup(String query) {
        try {
            waitForTurn();
            return handle(transport.get(SEARCH_URL + URLEncoder.encode(query, StandardCharsets.UTF_8)));
        } catch (JsonProcessingException unreadable) {
            return fallback(UNREADABLE);
        } catch (IOException unreachable) {
            return fallback(UNAVAILABLE);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return fallback(UNAVAILABLE);
        }
    }

    /** Sleeps if the previous request was sent less than the minimum gap ago. */
    private void waitForTurn() throws InterruptedException {
        long waitNanos = lastRequestNanos + minimumGapNanos - System.nanoTime();
        if (waitNanos > 0) {
            Thread.sleep(Duration.ofNanos(waitNanos));
        }
        lastRequestNanos = System.nanoTime();
    }

    private LookupResult handle(HttpResult response) throws JsonProcessingException {
        int status = response.status();
        if (status >= FIRST_SUCCESS_STATUS && status <= LAST_SUCCESS_STATUS) {
            return readFirstPlace(response.body());
        }
        if (status == NOT_FOUND_STATUS) {
            return new LookupResult(null, LOCATION_NOT_FOUND);
        }
        return fallback(SERVICE_PROBLEM);
    }

    private LookupResult readFirstPlace(String body) throws JsonProcessingException {
        NominatimPlace[] places = mapper.readValue(body, NominatimPlace[].class);
        if (places == null || places.length == 0) {
            return new LookupResult(null, NO_MATCH);
        }
        lastKnown = places[0].toAddress();
        return new LookupResult(lastKnown, FOUND);
    }

    /** Uses the last address found, if there is one, after the service has failed. */
    private LookupResult fallback(String problem) {
        if (lastKnown == null) {
            return new LookupResult(null, problem);
        }
        return new LookupResult(lastKnown, problem + LAST_KNOWN_NOTE);
    }

    /**
     * What a lookup produced.
     *
     * @param address the address found, the last known address after a failure, or {@code null}
     * @param message a friendly message to show the user
     */
    public record LookupResult(Address address, String message) {

        /** @return {@code true} if an address can be shown, either new or the last known one */
        public boolean hasAddress() {
            return address != null;
        }

        /** @return {@code true} only if the service has just found this address */
        public boolean found() {
            return FOUND.equals(message);
        }
    }

    /**
     * One search result as Nominatim sends it. Jackson fills it from the JSON;
     * other fields in the response are ignored, and a missing field counts as unreadable data.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record NominatimPlace(
            @JsonProperty(value = "display_name", required = true) String displayName,
            @JsonProperty(value = "lat", required = true) double latitude,
            @JsonProperty(value = "lon", required = true) double longitude) {

        Address toAddress() {
            return new Address(displayName, latitude, longitude);
        }
    }
}
