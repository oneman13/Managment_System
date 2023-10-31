package application.api.exceptions.users;

public class PermissionDeniedException  extends  Exception{
    public PermissionDeniedException(String message){
        super(message);
    }

    public PermissionDeniedException(){
        super();
    }
}
