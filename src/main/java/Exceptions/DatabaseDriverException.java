package Exceptions;

public class DatabaseDriverException extends RuntimeException {

    public DatabaseDriverException(String s) {
        super(s);
    }

    public DatabaseDriverException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
