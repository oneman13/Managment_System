package application.api.exceptions.tasks;

public class EmployeeDoesNotExistException extends Exception{

    public EmployeeDoesNotExistException() { super(); }

    public EmployeeDoesNotExistException(String message) { super(message); }
}
