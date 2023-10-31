package application.api.exceptions.timeoffs;

public class TimeOffAlreadyApprovedException extends Exception {
    public TimeOffAlreadyApprovedException(){
        super();
    }

    public TimeOffAlreadyApprovedException(String message){
        super(message);
    }
}
