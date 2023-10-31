package application.api.users;

import application.DataForDatabase;
import application.StandardUserPhoto;
import application.api.Database;
import application.api.exceptions.users.InvalidEmailException;
import application.api.exceptions.users.InvalidPasswordException;
import application.api.exceptions.users.PasswordsDontMatchException;

import java.security.MessageDigest;
import java.sql.*;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

interface AuthorizationInterface{
    public boolean register(String email, String password, String passwordConfirm, String fullName, String company)
            throws SQLException, InvalidEmailException, InvalidPasswordException, PasswordsDontMatchException;
    public boolean authorize(String email, String password) throws SQLException;
    public String hash(String password);
}

public class Authorization implements AuthorizationInterface {
    private Connection connection;

    public Authorization(){ // constructor for connection to database
        try {
            Class.forName("org.postgresql.Driver");
            Properties authorization = new Properties();

            String password = DataForDatabase.password;
            String username = DataForDatabase.user;
            String url = DataForDatabase.url;

            authorization.put("user", username);
            authorization.put("password", password);

            this.connection = DriverManager.getConnection(url, authorization);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean register(String email, String password, String passwordConfirm, String fullName, String company)
            throws SQLException, PasswordsDontMatchException, InvalidPasswordException, InvalidEmailException {
        Statement statement = connection.createStatement();

        // email validator
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        boolean isValidEmail = pat.matcher(email).matches();

        if (!isValidEmail) throw new InvalidEmailException("Email must be valid");

        // password validation
        if (!Objects.equals(password, passwordConfirm)) throw new PasswordsDontMatchException("Passwords must match");

        if (password.length() < 8) throw new InvalidPasswordException("Password must be at least 8 characters");

        String generatedPassword = this.hash(password);

        String values = String.format(" ('%s', '%s', '%s', '%s', '%s', '%s')",
                email, generatedPassword, fullName, "employer", company, StandardUserPhoto.path);
        String insertQuery = "INSERT INTO users (email, password, full_name, status, company, photo) VALUES " + values;
        statement.executeUpdate(insertQuery);

        // if registration was successful, then user should be logged in
        return authorize(email, password);
    }

    @Override
    public boolean authorize(String email, String password) throws SQLException {

        Statement statement = connection.createStatement();
        String hash = this.hash(password);

        String values = String.format(" email = '%s' AND password = '%s'", email, hash);
        String selectQuery = "SELECT * FROM users WHERE " + values;

        ResultSet result = statement.executeQuery(selectQuery);
        if (!result.next()) return false; // if no result matched the select query
        else {
            // updating current user
            Database db = Database.getInstance();
            String currentUserId = CurrentUser.getUserId();

            if (currentUserId != null) { // if current_users table is empty
                db.update("current_users", "current_user_id = " + currentUserId ,
                        "current_user_id = " + result.getString("user_id"));
            }
            else {
                db.insert("current_users", "(current_user_id)",
                        "('" + result.getString("user_id") + "')");
            }
            // if authorization is successful, then user should log in
            return true;
        }

    }

    public String hash(String password){
        try { // function to hash password and return hash

            String generatedPassword = null;

            // Hashing Algorithm
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());

            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            generatedPassword = sb.toString();
            return generatedPassword;
        }
        catch (Exception e){
            System.out.println(e);
            return null;
        }
    }
}