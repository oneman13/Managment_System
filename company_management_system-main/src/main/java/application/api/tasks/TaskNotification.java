package application.api.tasks;

import application.api.Database;

import application.api.exceptions.tasks.EmployeeDoesNotExistException;
import application.api.exceptions.tasks.NotificationDoesNotExistException;
import application.api.exceptions.tasks.TaskDoesNotExistException;

import java.sql.ResultSet;
import java.sql.SQLException;

// Notification interface with all needed methods
interface TaskNotificationInterface{
    public void createTaskNotification(int sender_id, int receiver_id, int task_id, String notificationMessage)
            throws SQLException, TaskDoesNotExistException;

    public void createTaskNotificationForTimeoffs(String sender_id, String receiver_id, int timeoff_id, String notificationMessage)
            throws SQLException, TaskDoesNotExistException;

    public void deleteTaskNotification(int notification_id)
            throws SQLException, NotificationDoesNotExistException;

    public ResultSet getAllTaskNotifications()
            throws SQLException;

    public void updateTaskNotification(int notification_id)
            throws SQLException, NotificationDoesNotExistException;


    public ResultSet getNotification(int notification_id)
            throws SQLException, NotificationDoesNotExistException;

    public ResultSet getAllNotificationsForUser(int user_id)
            throws SQLException, EmployeeDoesNotExistException;

    public ResultSet getAllNotSeenNotifications(int user_id)
            throws SQLException, EmployeeDoesNotExistException;
}


// notification class that implements method with all needed tasks
public class TaskNotification implements TaskNotificationInterface{

    Database db = Database.getInstance();

    String notificationsTable = "notifications";

    // checks whether notification exists or not
    public boolean notificationExists(int notification_id) throws SQLException{
        ResultSet notification = db.get("*", notificationsTable, "notification_id = " + notification_id);

        return notification.next();
    }

    // check whether user with given id exists or not
    public boolean userExists(int user_id) throws SQLException {
        ResultSet user = db.get("*", "users", "user_id = " + user_id);

        return user.next();
    }

    @Override
    public void createTaskNotification(int sender_id, int receiver_id, int task_id, String notificationMessage)
            throws SQLException, TaskDoesNotExistException {

        ResultSet generatedNotification = db.insert(notificationsTable, "(sender_id, task_id, notification_message)",
                "(" + sender_id + ", " + task_id + ", '" + notificationMessage + "')");

        if (generatedNotification.next()) {
            long notification_id = generatedNotification.getLong(1);

            // adds notification to user using many-to-many relational table
            db.insert("users_notifications", "(user_id, notification_id)", "("+receiver_id + ", " + notification_id+")");
        }
    }

    @Override
    public void createTaskNotificationForTimeoffs(String sender_id, String receiver_id, int timeoff_id, String notificationMessage)
            throws SQLException, TaskDoesNotExistException {
        String values = String.format(" ('%s', '%s', '%s')", sender_id, timeoff_id, notificationMessage);
        ResultSet generatedNotification = db.insert(notificationsTable, "(sender_id, timeoff_id, notification_message)",
                values);

        if (generatedNotification.next()) {
            long notification_id = generatedNotification.getLong(1);


            // adds notification to user using many-to-many relational table
            db.insert("users_notifications", "(user_id, notification_id)", "(" + receiver_id + ", " + notification_id + ")");
        }
    }

    @Override
    public void deleteTaskNotification(int notification_id)
            throws SQLException, NotificationDoesNotExistException {

        if(!notificationExists(notification_id)){
            throw new NotificationDoesNotExistException("Notification with given id does not exist!");
        }

        db.delete(notificationsTable, "notification_id = " + notification_id);

        // delete records of notification from many-to-many relational table
        db.delete("users_notifications","notification_id = " + notification_id);

    }

    @Override
    public ResultSet getAllTaskNotifications() throws SQLException {

        ResultSet notifications = db.getNC("*", notificationsTable);

        return notifications;
    }

    @Override
    public void updateTaskNotification(int notification_id)
            throws SQLException, NotificationDoesNotExistException {

        if(!notificationExists(notification_id)){
            throw new NotificationDoesNotExistException("Notification with given id does not exist!");
        }

        // set notification status to seen
        db.update(notificationsTable, "notification_id = " + notification_id, "is_seen = True");

    }

    @Override
    public ResultSet getNotification(int notification_id)
            throws SQLException, NotificationDoesNotExistException {

        if(!notificationExists(notification_id)){
            throw new NotificationDoesNotExistException("Notification with given id does not exist!");
        }

        ResultSet taskNotification = db.get("*", notificationsTable, "notification_id = " + notification_id);


        return taskNotification;
    }

    @Override
    public ResultSet getAllNotificationsForUser(int user_id)
            throws SQLException, EmployeeDoesNotExistException {

        // checks if employee exists
        if (!userExists(user_id)){
            throw new EmployeeDoesNotExistException("Employee does not exist!");
        }

        // query to select all notifications for particular user
        String query = "SELECT notification_id, task_id, sender_id, notification_message\n" +
                "FROM notifications\n" +
                "\tINNER JOIN users_notifications USING(notification_id)\n" +
                "WHERE user_id = " + user_id;

        ResultSet taskNotifications = db.anotherGetQuery(query);

        return taskNotifications;
    }

    @Override
    public ResultSet getAllNotSeenNotifications(int user_id)
            throws SQLException, EmployeeDoesNotExistException {

        // checks if employee exists
        if(!userExists(user_id)){
            throw new EmployeeDoesNotExistException("Employee does not exist!");
        }

        // query to select notifications for particular user, which were not seen
        String query = "SELECT notification_id, task_id, sender_id, notification_message\n" +
                "FROM notifications\n" +
                "\tINNER JOIN users_notifications USING(notification_id)\n" +
                "WHERE is_seen = false AND user_id = " + user_id;

        ResultSet taskNotifications = db.anotherGetQuery(query);

        return taskNotifications;
    }
}
