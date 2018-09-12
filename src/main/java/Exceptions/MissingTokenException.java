package Exceptions;

public class MissingTokenException extends RuntimeException {

    public MissingTokenException(String s) {
        super(s);
    }

    public MissingTokenException(String message, Throwable exception) {
        super(message, exception);
    }
}
