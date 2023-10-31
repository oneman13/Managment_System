package application.api.exceptions.timeoffs;

public class TimeOffAlreadyExistsException extends Exception{
    public TimeOffAlreadyExistsException(){
        super();
    }

    public TimeOffAlreadyExistsException(String message){
        super(message);
    }
}
