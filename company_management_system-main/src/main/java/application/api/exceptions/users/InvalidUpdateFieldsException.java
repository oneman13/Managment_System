package application.api.exceptions.users;

public class InvalidUpdateFieldsException extends Exception {
    public InvalidUpdateFieldsException(){
        super();
    }

    public InvalidUpdateFieldsException(String message){
        super(message);
    }
}
