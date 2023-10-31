package application.api.exceptions.tasks;

public class InvalidInputFieldsException extends Exception{
    public InvalidInputFieldsException() { super(); }

    public InvalidInputFieldsException(String message) { super(message); }
}
