package application.api.exceptions.slots;

public class OneDayLimitException extends Exception{
    OneDayLimitException() {super();}

    public OneDayLimitException(String message) {super(message);}
}
