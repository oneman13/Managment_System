package application.api.users;

import application.api.Database;
import application.api.exceptions.users.InvalidEmailException;
import application.api.exceptions.users.InvalidPasswordException;
import application.api.exceptions.users.InvalidUpdateFieldsException;
import application.api.exceptions.users.PasswordsDontMatchException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

interface AdminInterface {
    void create(String fullname, String email, String company, String password, String passwordConfirm)
            throws SQLException, InvalidPasswordException, PasswordsDontMatchException, InvalidEmailException;
    ResultSet selectOne(String userId);
    ResultSet selectAll();
    ResultSet selectAll(String filterQuery);
    ResultSet selectAll(String sortBy, String order);
    ResultSet selectAll(String filterQuery, String sortBy, String order);
    void delete(String userId) throws SQLException;
    void update(String userId, HashMap<String, String> properties)
            throws InvalidEmailException, InvalidPasswordException, SQLException, InvalidUpdateFieldsException;
}

public class Admin implements AdminInterface {
    private Database db;
    public Admin(){
        db = Database.getInstance();
    }

    @Override
    public ResultSet selectAll(String filterQuery) {
        return db.get("*", "users", filterQuery);
    }

    @Override
    public ResultSet selectAll() {
        return db.getNC("*", "users");
    }

    @Override
    public ResultSet selectAll(String sortBy, String order) {
        String orderQuery = "ORDER BY "+ sortBy + " " + order;
        return db.get("*", "users", "user_id > 0 " + orderQuery);
    }

    @Override
    public ResultSet selectAll(String filterQuery, String sortBy, String order) {
        String orderQuery = "ORDER BY "+ sortBy + " " + order;
        return db.get("*", "users", filterQuery + " " + orderQuery);
    }

    @Override
    public void update(String userId, HashMap<String, String> properties)
            throws SQLException, InvalidPasswordException, InvalidEmailException, InvalidUpdateFieldsException {
        // for admin it's possible to update all fields

        if (properties.values().size() == 0) return;

        ResultSet user = selectOne(userId);

        if (user.next()) {
            // updating email
            if (properties.get("email") != null) {
                String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                        "[a-zA-Z0-9_+&*-]+)*@" +
                        "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                        "A-Z]{2,7}$";

                Pattern pat = Pattern.compile(emailRegex);
                boolean isValidEmail = pat.matcher(properties.get("email")).matches();

                if (!isValidEmail) throw new InvalidEmailException("Email must be valid");
            }

            if (properties.get("password") != null) {
                if (properties.get("password").length() < 8)
                    throw new InvalidPasswordException("Password must be at least 8 characters");

                String password = properties.get("password");
                Authorization auth = new Authorization();
                // replacing old password with new one
                properties.replace("password", password, auth.hash(password));

            }
            String status =properties.get("status");
            if (status != null){
                status = status.toLowerCase();
                ArrayList<String> statusOptionsList = new ArrayList<>();
                String[] statusOptions = {"employee", "employer", "admin"};
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

    @Override
    public void delete(String userId) throws SQLException {
        // function to delete any user from table users
        // it also deletes rows with user_id from other tables
        ResultSet user = selectOne(userId);
        if (user.next()) {
            db.delete("users_notifications", "user_id = " + userId);
            db.delete("users_tasks", "user_id = " + userId);
            db.delete("notifications", "sender_id = " + userId);
            db.delete("timeoffs", "employee_id = " + userId);
            db.delete("slots", "user_id = " + userId);
            db.delete("tasks", "creator_id = " + userId );
            db.delete("users", "user_id = " + userId);
        }
    }

    @Override
    public void create(String fullname, String email, String company, String password, String passwordConfirm)
    throws SQLException, InvalidPasswordException, PasswordsDontMatchException, InvalidEmailException {
        // function that creates a new user
        Authorization auth = new Authorization();
        auth.register(email, password, passwordConfirm, fullname, company);
    }

    @Override
    public ResultSet selectOne(String userId) {
        return db.get("*", "users", "user_id = " + userId);
    }
}
