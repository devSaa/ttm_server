package application.exception;

/**
 * Thrown when error occurred during
 * interactions between classes or with local file system.
 */
public class InternalServerErrorException extends Exception {
    public InternalServerErrorException() {
    }

    public InternalServerErrorException(String message) {
        super(message);
    }

    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
