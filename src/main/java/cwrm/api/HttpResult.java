package cwrm.api;

/**
 * The parts of an HTTP response that the address lookup needs.
 *
 * @param status the HTTP status code, for example 200 or 404
 * @param body   the response body as text
 */
public record HttpResult(int status, String body) {
}
