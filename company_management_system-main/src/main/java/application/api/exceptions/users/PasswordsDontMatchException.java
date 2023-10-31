package application.api.exceptions.users;

public class PasswordsDontMatchException extends Exception {
    public PasswordsDontMatchException(){
        super();
    }

    public PasswordsDontMatchException(String message){
        super(message);
    }
}
