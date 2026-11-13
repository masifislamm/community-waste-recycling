package cwrm.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * The real {@link HttpTransport}: sends requests with Java's built-in {@link HttpClient}.
 *
 * <p>Every request has an 8-second timeout and an identifying {@code User-Agent},
 * as the Nominatim usage policy requires.
 */
public class JdkHttpTransport implements HttpTransport {
    private static final Duration TIMEOUT = Duration.ofSeconds(8);
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String USER_AGENT = "CWRM-CSC61204/1.0 (Taylor's University student project)";
    private static final String INTERRUPTED = "The request was interrupted.";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    /** Creates a transport with its own {@link HttpClient}. */
    public JdkHttpTransport() {
        // The client is created in the field initialiser above.
    }

    @Override
    public HttpResult get(String url) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT)
                .header(USER_AGENT_HEADER, USER_AGENT)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new HttpResult(response.statusCode(), response.body());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IOException(INTERRUPTED, interrupted);
        }
    }
}
