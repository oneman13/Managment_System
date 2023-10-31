package application.api.users;

import application.api.Database;
import application.api.exceptions.users.InvalidEmailException;
import application.api.exceptions.users.InvalidUpdateFieldsException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.Pattern;

interface EmployeeInterface extends AbstractUserInterface{
    public void UpdateMe(HashMap<String, String> properties)
            throws InvalidEmailException, SQLException, InvalidUpdateFieldsException;
}
public class Employee extends AbstractUser implements EmployeeInterface {
    private Database db = Database.getInstance();
    @Override
    public void UpdateMe(HashMap<String, String> properties)
            throws InvalidEmailException, SQLException, InvalidUpdateFieldsException {
        // function to update users' data

        if (properties.values().size() == 0) return;


        String userId = CurrentUser.getUserId();

        String[] blackList = {"status", "worked_hours", "required_hours", "user_id",
                "occupation", "salary", "password", "company"};

        // limiting fields that can be updated
        for (String field : blackList) {
            if (properties.keySet().contains(field)){
                throw new InvalidUpdateFieldsException("You can update only email and full name");
            }
        }

        if (properties.get("email")!= null) {
            // email validation
            String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                    "[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                    "A-Z]{2,7}$";

            Pattern pat = Pattern.compile(emailRegex);
            boolean isValidEmail = pat.matcher(properties.get("email")).matches();

            if (!isValidEmail) throw new InvalidEmailException("Email must be valid");
        }

        // making correct format for query UPDATE users
        String query = "";
        for (String key : properties.keySet()){
            query += key + " = " +  "'" +properties.get(key) + "',";
        }
        query = query.substring(0, query.length()-1);
        db.update("users", "user_id = " + userId, query);
    }
}
