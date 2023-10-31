package application.api.users;

import application.DataForDatabase;
import application.api.Database;

import java.sql.*;
import java.util.Properties;

public class CurrentUser {
    private static Connection connection;
    static { // static initializing block is used because CurrentUser has no constructors,
        // but "connection" needs to be initialized
        try {
            Class.forName("org.postgresql.Driver");
            Properties authorization = new Properties();

            String password = DataForDatabase.password;
            String username = DataForDatabase.user;
            String url = DataForDatabase.url;

            authorization.put("user", username);
            authorization.put("password", password);

            connection = DriverManager.getConnection(url, authorization);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String getUserField(String field) throws SQLException {
        // function that returns status of the user
        Statement statement = connection.createStatement();
        String userId = getUserId();

        if (userId == null) return null; // if there's no user, it should return null
        else {
            Database db = Database.getInstance();
            ResultSet resultSet = db.get(field, "users", "user_id = " + userId);
            if (resultSet.next()) {
                return resultSet.getString(field);
            }
            else return null;
        }

    }

    public static String getUserId() throws SQLException {
        // function that returns id of current user
        String selectQuery = "SELECT current_user_id FROM current_users";

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(selectQuery);
        if (resultSet.next()) {
            return resultSet.getString("current_user_id");
        }
        else return null;
    }

    public static ResultSet getUser() throws SQLException {
        // function that return the User table from current user
        String selectQuery = "SELECT current_user_id FROM current_users";

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(selectQuery);
        return resultSet;
    }
}