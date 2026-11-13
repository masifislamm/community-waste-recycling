package cwrm.api;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Test double for {@link HttpTransport}. It replays prepared answers in order
 * (the last one repeats) and records each request. It never touches the network.
 */
public final class FakeHttpTransport implements HttpTransport {
    private final Deque<HttpTransport> answers = new ArrayDeque<>();
    private final List<String> requestedUrls = new ArrayList<>();
    private final List<Long> requestTimesNanos = new ArrayList<>();

    private FakeHttpTransport(List<HttpTransport> answers) {
        this.answers.addAll(answers);
    }

    /** @return a fake that gives these answers, one per request */
    public static FakeHttpTransport answering(HttpTransport... answers) {
        return new FakeHttpTransport(List.of(answers));
    }

    /** @return an answer with this status code and body */
    public static HttpTransport status(int status, String body) {
        return url -> new HttpResult(status, body);
    }

    /** @return an answer that fails with this exception, as a network error would */
    public static HttpTransport failure(IOException error) {
        return url -> {
            throw error;
        };
    }

    @Override
    public HttpResult get(String url) throws IOException {
        requestedUrls.add(url);
        requestTimesNanos.add(System.nanoTime());
        HttpTransport answer = answers.size() > 1 ? answers.poll() : answers.peek();
        return answer.get(url);
    }

    /** @return every URL requested so far, in order */
    public List<String> requestedUrls() {
        return requestedUrls;
    }

    /** @return the {@link System#nanoTime()} of each request, in order */
    public List<Long> requestTimesNanos() {
        return requestTimesNanos;
    }
}
