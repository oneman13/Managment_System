package application.api;

import application.api.exceptions.slots.CannotCreateSlotForFutureException;
import application.api.exceptions.slots.OneDayLimitException;
import application.api.exceptions.tasks.InvalidInputFieldsException;
import application.api.exceptions.tasks.TaskDoesNotExistException;
import application.api.tasks.Task;
import application.api.users.CurrentUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// Task interface with all needed methods
interface SlotInterface {
    static void createSlotWithoutTask(double hours, String description)
            throws SQLException {}
    static void createSlotForTimeOff(double hours, String description, String timeOffId, String date)
            throws SQLException{}
    static void createSlot(int taskID, double hours, String description)
            throws SQLException, OneDayLimitException{}
    static void updateSlotHours(int slot_id, double hours) {}

    static void updateSlotDescription(int slot_id, String description) {}

    static void updateSlotBothArgs(int slot_id, double hours, String description) {}

    static void deleteSlot(int slot_id) {}

    static ResultSet getAllSlots() throws SQLException {return null;}

    static ResultSet getOneSlot(int slot_id) {return null;}

    static void updateSlotDate(int slot_id, String slot_date) {}

    static ResultSet checkOneDayLimit(String user_id, String date) {return null;}

    static ResultSet selectCalendarSlots(String user_id) {return null;}
}

// class slots that implements  slot interface
public class Slot implements SlotInterface{
    private static final Database db = Database.getInstance();

    // if date that is given in parameters of functions > today -> exception
    // else
    private static boolean isNotInFuture(String date)
            throws ParseException, CannotCreateSlotForFutureException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date today = format.parse(String.valueOf(LocalDate.now()));
        Date date1 = format.parse(date);

        // drops warning if time of slot is set for the future
        if (today.compareTo(date1) < 0) {
            throw new CannotCreateSlotForFutureException("You are trying to set slot for future");
        }
        else return today.compareTo(date1) >= 0;
    }

    public static void createSlotWithoutTask(double hours, String description, String date)
            throws SQLException, CannotCreateSlotForFutureException, ParseException {
        String columns = "(hours, description, user_id, slot_date)";
        ResultSet resultSet = checkOneDayLimit(CurrentUser.getUserId(), date);

        //check if the slot is not in the future, and the sum of hours is not > 24
        if (isNotInFuture(date) && resultSet.next() && resultSet.getInt("sum") < 24) {
            String values = String.format(
                    "(%.2f, '%s', %s, '%s')",
                    hours, description, CurrentUser.getUserId(), date
            );
            db.insert("slots", columns, values);
        }
    }

    public static void createSlotForTimeOff(double hours, String description, String timeOffId, String date)
            throws SQLException {
        String columns = "(hours, description, user_id, slot_date)";
        String hoursStr = String.format(Locale.US, "%.2f", hours); // converting double to right format
        String values = String.format(
                "(%s, '%s', %s, '%s')",
                hoursStr, description, timeOffId, date
        );
        db.insert("slots", columns, values);
    }

    public static void createSlot(int taskID, double hours, String description, String date)
            throws SQLException, OneDayLimitException, CannotCreateSlotForFutureException, ParseException,
            TaskDoesNotExistException, InvalidInputFieldsException {
        String columns = "(task_id, hours, description, user_id, slot_date)";

        ResultSet resultSet = checkOneDayLimit(CurrentUser.getUserId(), date);

        //check if the slot is not in the future, and the sum of hours is not > 16
        if (isNotInFuture(date) && resultSet.next() &&
                resultSet.getString("sum") != null && resultSet.getInt("sum") <= 16) {
            throw new OneDayLimitException("you cannot work more than 16 hours a day");
        }
        Task task = new Task();
        task.updateTaskStatus(taskID, "in progress");

        String values = String.format(
                "(%d, %.2f, '%s', %s, '%s')",
                taskID, hours, description, CurrentUser.getUserId(), date
        );
        db.insert("slots", columns, values);
    }

    public static void updateSlotHours(int slot_id, double hours){
        String values = String.format("hours = %.2f", hours);
        db.update("slots", "slot_id = " + slot_id, values);
    }

    public static void updateSlotDescription(int slot_id, String description){
        String values = String.format("description = '%s'", description);
        db.update("slots", "slot_id = " + slot_id, values);
    }

    public static void updateSlotBothArgs(int slot_id, double hours, String description) {
        String values = String.format("hours = %.2f, description = '%s'", hours, description);
        db.update("slots", "slot_id = " + slot_id, values);
    }

    public static void deleteSlot(int slot_id){
        db.delete("slots", "slot_id = " + slot_id);
    }

    public static ResultSet getAllSlots()
            throws SQLException {
        return db.get("*", "slots",
                "user_id = '" + CurrentUser.getUserId() + "'");
    }

    public static ResultSet getOneSlot(int slot_id){
        return db.get("*", "slots",
                "slot_id = '" + slot_id + "'");
    }

    public static void updateSlotDate(int slot_id, String slot_date)
        throws CannotCreateSlotForFutureException, ParseException{

        if (isNotInFuture(slot_date)) {
            String values = String.format("slot_date = '%s'", slot_date);
            db.update("slots", "slot_id = " + slot_id, values);
        }
    }

    // function to check if the sum of all the slots' time
    // is not more than 24 hours
    public static ResultSet checkOneDayLimit(String user_id, String date) {
        return db.get(" SUM(hours) ", "slots",
                "user_id = " + user_id + " AND slot_date = '" + date + "';");
    }

    public static ResultSet selectCalendarSlots(String user_id) throws SQLException, ParseException {
        //selecting whether worker has worked enough for current month
        ResultSet resultSet = db.get("*", "users", "user_id = " + user_id);
        if (!resultSet.next()) return null;

        // getting first and last date of the month
        Calendar startOfTheMonth = Calendar.getInstance();
        startOfTheMonth.setTime(new Date());
        startOfTheMonth.set(Calendar.DAY_OF_MONTH, startOfTheMonth.getActualMinimum(Calendar.DAY_OF_MONTH));

        Calendar endOfTheMonth = Calendar.getInstance();
        endOfTheMonth.setTime(new Date());
        endOfTheMonth.set(Calendar.DAY_OF_MONTH, endOfTheMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

        // implementing SQL query
        return db.anotherGetQuery("SELECT users.required_hours, SUM(slots.hours) as total," +
                " slots.slot_date, users.required_hours/5 AS required, " +
                " CAST(SUM(slots.hours) >= users.required_hours / 5 AS BOOLEAN) AS is_worked, " +
                "CASE WHEN slots.description LIKE '%time off%' THEN true ELSE false END AS is_timeoff " +
                "FROM users INNER JOIN slots USING(user_id) " + "WHERE users.user_id = " + user_id +
                " AND slots.slot_date >= '" + startOfTheMonth.getTime() +
                "' AND slots.slot_date <= '" + endOfTheMonth.getTime() +
                "' GROUP BY (slots.slot_date, user_id, is_timeoff)");
    }

    public static ResultSet getAllSlots(int id)
            throws SQLException {
        return db.get("*", "slots",
                "user_id = '" + id + "'");
    }

}
