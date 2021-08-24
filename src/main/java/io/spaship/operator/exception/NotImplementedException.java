package io.spaship.operator.exception;

public class NotImplementedException extends RuntimeException{
    public NotImplementedException() {
        super("this feature is not implemented yet");
    }
    public NotImplementedException(String details) {
        super(details);
    }
}
