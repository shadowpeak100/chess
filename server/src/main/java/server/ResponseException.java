package server;

public class ResponseException extends Exception {
    public int statusCode;

    public ResponseException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
