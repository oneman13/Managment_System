package application.api.exceptions.slots;

public class CannotCreateSlotForFutureException extends Exception{
    public CannotCreateSlotForFutureException() {
        super();
    }
    public CannotCreateSlotForFutureException(String message) {
        super(message);
    }
}
