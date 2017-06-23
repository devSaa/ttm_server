package application.exception;

/**
 * Thrown when a caller attempts to get the value of
 * a non-existent application property.
 */
public class PropertyNotFoundException extends Exception {
    public PropertyNotFoundException(String message) {
        super(message);
    }
}
