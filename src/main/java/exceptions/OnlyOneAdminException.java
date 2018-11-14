package exceptions;

public class OnlyOneAdminException extends RuntimeException{

    public OnlyOneAdminException(String s) {
        super(s);
    }

    public OnlyOneAdminException(String message, Throwable exception) {
        super(message, exception);
    }
}
