package application.api.exceptions.users;

public class InvalidPasswordException extends Exception {
    public InvalidPasswordException(){
        super();
    }

    public InvalidPasswordException(String message){
        super(message);
    }

}
