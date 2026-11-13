package cwrm.api;

import java.io.IOException;

/**
 * Sends an HTTP GET request and returns the response.
 *
 * <p>{@link NominatimAddressLookup} depends on this interface instead of on
 * {@code HttpClient} directly. The real app uses {@link JdkHttpTransport}; unit tests
 * pass in a fake, so no test ever touches the network. A caching or self-hosted
 * geocoder could also be plugged in here later without changing the lookup code.
 */
public interface HttpTransport {

    /**
     * Sends a GET request.
     *
     * @param url the full URL to request
     * @return the status code and body of the response
     * @throws IOException if the server cannot be reached or the request times out
     */
    HttpResult get(String url) throws IOException;
}
