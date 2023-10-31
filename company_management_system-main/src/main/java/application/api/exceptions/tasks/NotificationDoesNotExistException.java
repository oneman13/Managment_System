package application.api.exceptions.tasks;

public class NotificationDoesNotExistException extends Exception {
    public NotificationDoesNotExistException() { super(); }

    public NotificationDoesNotExistException(String message) {super(message);}
}
