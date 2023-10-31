package application.api.users;

import application.StandardUserPhoto;
import application.api.Database;
import application.api.exceptions.users.InvalidEmailException;
import application.api.exceptions.users.InvalidUpdateFieldsException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

interface EmployerInterface extends AbstractUserInterface{
    void createUser(String fullName, String email, String company, double salary, double required_hours,
                           String occupation, String dateOfBirth)
            throws InvalidEmailException, SQLException;
    ResultSet selectAllUsers() throws SQLException;
    ResultSet selectAllUsers(String sortField, String order) throws SQLException;

    ResultSet selectAllUsers(String filterQuery) throws SQLException;

    ResultSet selectAllUsers(String filterQuery, String sortField, String order) throws SQLException;

    ResultSet selectEmployee(String userId) throws SQLException;

    void updateEmployee(String userId, HashMap<String, String> properties)
            throws SQLException, InvalidEmailException, InvalidUpdateFieldsException;

    void deleteEmployee(String userId) throws SQLException;

}

public class Employer extends AbstractUser implements EmployerInterface  {
    private Database db = Database.getInstance();
    @Override
    public void createUser(String fullName, String email, String company, double salary, double required_hours,
                           String occupation, String dateOfBirth)
    throws SQLException, InvalidEmailException {

        Authorization authorization = new Authorization();

        //  email validation
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        boolean isValidEmail = pat.matcher(email).matches();

        if (!isValidEmail) throw new InvalidEmailException("Email must be valid");

        // by default password is date of birth
        String password = dateOfBirth.replaceAll("-", "");
        password = authorization.hash(password);
        String values = String.format(" ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                fullName, email, company, salary, required_hours, occupation,
                dateOfBirth, password, "employee");
        db.insert("users", " (full_name, email, company, salary, required_hours, occupation, date_of_birth, password, status) ", values);

    }

    @Override
    public ResultSet selectAllUsers() throws SQLException {
        // function that returns all users from the company
        String userId = CurrentUser.getUserId();

        ResultSet resultSet = db.get("company", "users", "user_id = " + userId);

        if (resultSet.next()){
            resultSet = db.get("*", "users", "company = '" +
                    resultSet.getString("company") + "'");
            return resultSet;
        }
        return null;
    }

    @Override
    public ResultSet selectAllUsers(String sortField, String order) throws SQLException {
        // sortField - a column by which users will be sorted; example: 'salary', 'required_hours'
        // order - order of sort, DESC or ASC

        String userId = CurrentUser.getUserId();

        ResultSet resultSet = db.get("company", "users", "user_id = " + userId);
        String orderQuery = "ORDER BY " + sortField + " " + order;
        if (resultSet.next()){
            resultSet = db.get("*", "users", "company = '" +
                    resultSet.getString("company") + "' " + orderQuery);
            return resultSet;
        }
        return null;
    }

    @Override
    public ResultSet selectAllUsers(String filterQuery) throws SQLException {
        // function that returns users filtered by some fields
        String userId = CurrentUser.getUserId();

        ResultSet resultSet = db.get("company", "users", "user_id = " + userId);

        if (resultSet.next()){
            resultSet = db.get("*", "users", "company = '" +
                    resultSet.getString("company") + "' AND " + filterQuery);
            return resultSet;
        }
        return null;
    }

    @Override
    public ResultSet selectAllUsers(String filterQuery, String sortField, String order) throws SQLException {
        // function that returns filtered and sorted list of users
        String userId = CurrentUser.getUserId();

        ResultSet resultSet = db.get("company", "users", "user_id = " + userId);
        String orderQuery = "ORDER BY " + sortField + " " + order;
        if (resultSet.next()){
            resultSet = db.get("*", "users", "company = '" +
                    resultSet.getString("company") + "' AND " + filterQuery + " " + orderQuery);
            return resultSet;
        }
        return null;
    }

    @Override
    public ResultSet selectEmployee(String userId) throws SQLException {
        // function to get one user from the company
        String managerId = CurrentUser.getUserId();
        ResultSet resultSet = db.get("company", "users", "user_id = " + managerId);

        if (resultSet.next()) {
            return db.get("*", "users",
                    "user_id = " + userId + " AND company = '" + resultSet.getString("company") + "'");
        }
        return null;
    }

    @Override
    public void updateEmployee(String userId, HashMap<String, String> properties)
            throws SQLException, InvalidEmailException, InvalidUpdateFieldsException {
        // a function where manager can update an employee

        if (properties.values().size() == 0) return;

        String[] blackList = { "worked_hours", "user_id", "password", "company", "photo"};

        // limiting fields that can be updated
        for (String field : blackList) {
            if (properties.keySet().contains(field)){
                throw new InvalidUpdateFieldsException("You cannot update worked_hours, user_id, password, company or photo");
            }
        }

        ResultSet employee = selectEmployee(userId);
        if (employee!= null && employee.next()){
            if (employee.getString("status")!= "employer") { // only employees can be updated
                // if email is updated, it should be verified
                if (properties.get("email") != null) {
                    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                            "[a-zA-Z0-9_+&*-]+)*@" +
                            "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                            "A-Z]{2,7}$";

                    Pattern pat = Pattern.compile(emailRegex);
                    boolean isValidEmail = pat.matcher(properties.get("email")).matches();

                    if (!isValidEmail) throw new InvalidEmailException("Email must be valid");
                }

                // updating status
                String status =properties.get("status");
                if (status != null){
                    status = status.toLowerCase();
                    ArrayList<String> statusOptionsList = new ArrayList<>();
                    String[] statusOptions = {"employee", "employer"};
                    statusOptionsList.addAll(List.of(statusOptions));
                    if (!statusOptionsList.contains(status))
                        throw new InvalidUpdateFieldsException("Status can only be: employee, employer, admin");
                }

                String query = "";
                for (String key : properties.keySet()) {
                    query += key + " = " + "'" + properties.get(key) + "',";
                }
                query = query.substring(0, query.length() - 1);
                db.update("users", "user_id = " + userId, query);
            }
        }
    }

    @Override
    public void deleteEmployee(String userId) throws SQLException {
        // function to delete an employee from users table and all other tables with this user_id
        ResultSet employee = selectEmployee(userId);

        if (employee!= null && employee.next()){
            if (employee.getString("status")!= "employer") { // only employees can be deleted
                // all tables related to user ust also be deleted
                db.delete("users_notifications", "user_id = " + userId);
                db.delete("users_tasks", "user_id = " + userId);
                db.delete("notifications", "sender_id = " + userId);
                db.delete("timeoffs", "employee_id = " + userId);
                db.delete("slots", "user_id = " + userId);
                db.delete("tasks", "creator_id = " + userId );
                db.delete("users", "user_id = " + userId);
            }
        }
    }
}
