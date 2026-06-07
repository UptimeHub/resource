package uz.uptimehub.resourceapp.exception;

public class ElasticsearchUnavailableException extends RuntimeException {

    public ElasticsearchUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
