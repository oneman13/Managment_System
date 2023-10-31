package application.api.users;

import application.StandardUserPhoto;
import application.api.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

interface UsersStatsInterface {
    public void calculateHours(String userId) throws SQLException;
    public void calculateHoursAll() throws SQLException;

    public ResultSet selectTopCincoUsers(String sortField) throws SQLException;

    public ResultSet selectUsersSortedByWorkedHours() throws SQLException;

    void updateUserPhotos() throws SQLException;
}

public class UsersStats implements UsersStatsInterface {
    private Database db = Database.getInstance();
    @Override
    public void calculateHours(String userId) throws SQLException {


        // getting today's date
        java.sql.Date today = new java.sql.Date(Calendar.getInstance().getTime().getTime());

        // getting last week's date
        Calendar lastWeeksDate = Calendar.getInstance();
        lastWeeksDate.add(Calendar.DATE, -7);
        java.sql.Date lastWeek = new java.sql.Date(lastWeeksDate.getTime().getTime());

        // adding all worked hours within last 7 days
        ResultSet resultSet = db.get("SUM(hours) AS total_hours", "slots", "user_id = " + userId
                + " AND slot_date > '" + lastWeek + "' AND slot_date <= '" + today +"'");

        // updating worked_hours in users table
        if (resultSet.next()){
            db.update("users", "user_id = " + userId,
                    "worked_hours = " + resultSet.getString("total_hours") );
        }
    }

    @Override
    public void calculateHoursAll() throws SQLException {
        // automatically calculating hours for all users
        ResultSet resultSet = db.getNC("user_id", "users");

        while (resultSet.next()){
            String userId = resultSet.getString("user_id");
            calculateHours(userId);
        }
    }

    @Override
    public ResultSet selectTopCincoUsers(String sortField) throws SQLException {
        // function which selects top 5 users based on some sort field
        String orderQuery = String.format(" ORDER BY %s DESC ", sortField);

        switch (sortField){
            case "worked_hours" :
                return db.get("*", "users", "worked_hours > 0 AND company = '" +
                        CurrentUser.getUserField("company") + "'"  + orderQuery);
            case "tasks":
                // getting today's date
                java.sql.Date today = new java.sql.Date(Calendar.getInstance().getTime().getTime());

                // getting last week's date
                Calendar lastWeeksDate = Calendar.getInstance();
                lastWeeksDate.add(Calendar.DATE, -7);
                java.sql.Date lastWeek = new java.sql.Date(lastWeeksDate.getTime().getTime());

                return db.anotherGetQuery("SELECT users.*, COUNT(users_tasks.*)" +
                        " AS tasks FROM users " +
                        "INNER JOIN users_tasks USING (user_id) " +
                        "INNER JOIN tasks USING (task_id) " +
                        " WHERE tasks.status = 'completed' AND company = '" + CurrentUser.getUserField("company") +
                        "' AND completion_date <='" + today + "' AND completion_date > '" + lastWeek + "'" +
                        " GROUP BY user_id" + orderQuery);

            case "timeoffs":
                // counting all slots for time offs
                return db.anotherGetQuery("SELECT users.*, COUNT(slots.*) as timeoffs " +
                        "FROM users INNER JOIN slots USING(user_id) " +
                        " WHERE description LIKE '%time off%' AND company = '" + CurrentUser.getUserField("company") +
                        "' GROUP BY user_id" +
                        " ORDER BY timeoffs DESC LIMIT 5");
            default:
                return null;
        }
    }

    @Override
    public ResultSet selectUsersSortedByWorkedHours() throws SQLException {
        return db.get("*, (worked_hours - required_hours) AS hours", "users",
                "company = '" + CurrentUser.getUserField("company") + "' ORDER BY hours DESC LIMIT 5" );
    }

    @Override
    public void updateUserPhotos() throws SQLException {
        db.update("users", "photo IS NULL", "photo = '" + StandardUserPhoto.path + "'");
    }
}
