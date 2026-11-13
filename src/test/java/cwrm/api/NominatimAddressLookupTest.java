package cwrm.api;

import static cwrm.api.FakeHttpTransport.answering;
import static cwrm.api.FakeHttpTransport.failure;
import static cwrm.api.FakeHttpTransport.status;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cwrm.api.NominatimAddressLookup.LookupResult;
import cwrm.model.Address;
import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for {@link NominatimAddressLookup}. A fake transport is used, so no test touches the network. */
class NominatimAddressLookupTest {
    private static final String QUERY = "Menara Kuala Lumpur";
    private static final String DISPLAY_NAME = "Menara Kuala Lumpur, Jalan Puncak, Kuala Lumpur, Malaysia";
    private static final double LATITUDE = 3.1528;
    private static final double LONGITUDE = 101.7038;
    private static final String ONE_RESULT = "[{\"place_id\":1,\"lat\":\"3.1528\",\"lon\":\"101.7038\","
            + "\"display_name\":\"" + DISPLAY_NAME + "\",\"type\":\"tower\"}]";
    private static final String EMPTY_ARRAY = "[]";
    private static final String SECRET_DETAIL = "SecretInternalDetail";
    private static final Duration NO_GAP = Duration.ZERO;
    private static final Duration SHORT_GAP = Duration.ofMillis(200);
    private static final Duration LONG_GAP = Duration.ofMinutes(1);

    private static NominatimAddressLookup lookupWith(FakeHttpTransport transport) {
        return new NominatimAddressLookup(transport, NO_GAP);
    }

    @Test
    void lookup_status200WithResult_returnsAddress() {
        LookupResult result = lookupWith(answering(status(200, ONE_RESULT))).lookup(QUERY);

        assertAll(
                () -> assertTrue(result.hasAddress()),
                () -> assertEquals(new Address(DISPLAY_NAME, LATITUDE, LONGITUDE), result.address()),
                () -> assertEquals(NominatimAddressLookup.FOUND, result.message()));
    }

    @Test
    void lookup_status200EmptyArray_returnsNotFoundMessage() {
        LookupResult result = lookupWith(answering(status(200, EMPTY_ARRAY))).lookup(QUERY);

        assertFalse(result.hasAddress());
        assertEquals(NominatimAddressLookup.NO_MATCH, result.message());
    }

    @Test
    void lookup_status404_returnsFriendlyMessage() {
        LookupResult result = lookupWith(answering(status(404, ""))).lookup(QUERY);

        assertNull(result.address());
        assertEquals(NominatimAddressLookup.LOCATION_NOT_FOUND, result.message());
    }

    @Test
    void lookup_status500WithoutCache_returnsFriendlyMessage() {
        LookupResult result = lookupWith(answering(status(500, "Internal Server Error"))).lookup(QUERY);

        assertNull(result.address());
        assertEquals(NominatimAddressLookup.SERVICE_PROBLEM, result.message());
    }

    @Test
    void lookup_status500WithCache_returnsLastKnownAddress() {
        NominatimAddressLookup lookup = lookupWith(answering(status(200, ONE_RESULT), status(500, "")));
        Address found = lookup.lookup(QUERY).address();

        LookupResult result = lookup.lookup(QUERY);

        assertEquals(found, result.address());
        assertEquals(NominatimAddressLookup.SERVICE_PROBLEM + NominatimAddressLookup.LAST_KNOWN_NOTE,
                result.message());
    }

    @ParameterizedTest
    @ValueSource(ints = {403, 429, 503})
    void lookup_otherErrorStatus_returnsServiceProblemMessage(int errorStatus) {
        LookupResult result = lookupWith(answering(status(errorStatus, ""))).lookup(QUERY);

        assertEquals(NominatimAddressLookup.SERVICE_PROBLEM, result.message());
    }

    @Test
    void lookup_ioException_returnsLastKnownAddress() {
        NominatimAddressLookup lookup = lookupWith(
                answering(status(200, ONE_RESULT), failure(new IOException(SECRET_DETAIL))));
        Address found = lookup.lookup(QUERY).address();

        LookupResult result = lookup.lookup(QUERY);

        assertEquals(found, result.address());
        assertEquals(NominatimAddressLookup.UNAVAILABLE + NominatimAddressLookup.LAST_KNOWN_NOTE,
                result.message());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "this is not json",
        "{\"display_name\":\"An object, not an array\"}",
        "[{\"lat\":\"3.1\",\"lon\":\"101.7\"}]",
        "[{\"display_name\":\"Bad numbers\",\"lat\":\"north\",\"lon\":\"east\"}]"})
    void lookup_malformedJson_returnsFriendlyMessage(String body) {
        LookupResult result = lookupWith(answering(status(200, body))).lookup(QUERY);

        assertNull(result.address());
        assertEquals(NominatimAddressLookup.UNREADABLE, result.message());
    }

    @ParameterizedTest
    @MethodSource("failures")
    void lookup_anyFailure_messageNeverContainsExceptionText(HttpTransport failingAnswer) {
        String message = lookupWith(answering(failingAnswer)).lookup(QUERY).message();

        assertAll(
                () -> assertFalse(message.contains(SECRET_DETAIL)),
                () -> assertFalse(message.contains("Exception")),
                () -> assertFalse(message.contains("\tat ")));
    }

    static Stream<HttpTransport> failures() {
        return Stream.of(
                failure(new IOException(SECRET_DETAIL)),
                failure(new HttpTimeoutException(SECRET_DETAIL)),
                status(500, SECRET_DETAIL),
                status(404, SECRET_DETAIL),
                status(200, SECRET_DETAIL));
    }

    @Test
    void lookup_queryWithSpaces_isUrlEncodedInRequest() {
        FakeHttpTransport transport = answering(status(200, EMPTY_ARRAY));

        lookupWith(transport).lookup(QUERY);

        String url = transport.requestedUrls().get(0);
        assertTrue(url.startsWith("https://nominatim.openstreetmap.org/search?format=json&limit=1&q="));
        assertTrue(url.endsWith("q=Menara+Kuala+Lumpur"));
    }

    @Test
    void lookup_twoCallsInARow_waitsForMinimumGap() {
        FakeHttpTransport transport = answering(status(200, ONE_RESULT));
        NominatimAddressLookup lookup = new NominatimAddressLookup(transport, SHORT_GAP);

        lookup.lookup(QUERY);
        lookup.lookup(QUERY);

        List<Long> times = transport.requestTimesNanos();
        assertTrue(times.get(1) - times.get(0) >= SHORT_GAP.toNanos());
    }

    @Test
    void lookup_interruptedWhileWaiting_returnsLastKnownAndKeepsInterruptFlag() {
        NominatimAddressLookup lookup = new NominatimAddressLookup(answering(status(200, ONE_RESULT)), LONG_GAP);
        Address found = lookup.lookup(QUERY).address();

        Thread.currentThread().interrupt();
        LookupResult result = lookup.lookup(QUERY);
        boolean flagKept = Thread.interrupted();

        assertAll(
                () -> assertTrue(flagKept),
                () -> assertEquals(found, result.address()),
                () -> assertEquals(NominatimAddressLookup.UNAVAILABLE + NominatimAddressLookup.LAST_KNOWN_NOTE,
                        result.message()));
    }
}
