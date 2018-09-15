package exceptions;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public InvalidTokenException(String s) {
        super(s);
    }
}
