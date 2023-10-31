package application.api.timeoffs;

import application.api.Database;
import application.api.Slot;
import application.api.exceptions.tasks.TaskDoesNotExistException;
import application.api.exceptions.timeoffs.TimeOffAlreadyApprovedException;
import application.api.exceptions.timeoffs.TimeOffAlreadyExistsException;
import application.api.exceptions.users.InvalidUpdateFieldsException;
import application.api.exceptions.users.PermissionDeniedException;
import application.api.tasks.TaskNotification;
import application.api.users.CurrentUser;
import application.api.users.Employer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;


interface TimeoffsInterface{
    public void createTimeOff(String description, String startDate, String endDate, String employerId)
            throws SQLException, TimeOffAlreadyExistsException, TaskDoesNotExistException;
    public void approveTimeOff(String timeOffId) throws SQLException, TimeOffAlreadyApprovedException, TaskDoesNotExistException;

    public void declineTimeOff(String timeOffId) throws SQLException, TimeOffAlreadyApprovedException, TaskDoesNotExistException;

    public ResultSet selectTimeOff(String timeOffId);

    public void updateTimeOff(String timeOffId, HashMap<String, String> properties)
            throws SQLException, TimeOffAlreadyApprovedException, InvalidUpdateFieldsException, TimeOffAlreadyExistsException;

    public void deleteTimeOff(String timeOffId) throws SQLException, TimeOffAlreadyApprovedException;

    public ResultSet selectAllTimeOffs() throws SQLException, PermissionDeniedException;

    public ResultSet selectAllTimeOffsEmployee() throws SQLException, PermissionDeniedException;

    public ResultSet selectAllTimeOffsAdmin() throws SQLException, PermissionDeniedException;
}

public class Timeoffs implements TimeoffsInterface{
    private Database db = Database.getInstance();
    @Override
    public void createTimeOff(String description, String startDate, String endDate, String employerId)
            throws SQLException, TimeOffAlreadyExistsException, TaskDoesNotExistException {
        // creating time off by employee
        String employeeId = CurrentUser.getUserId();
        checkTimeOffDates(employeeId, startDate, endDate, null);
        String values = String.format(" ('%s', '%s', '%s', '%s') ", employeeId, description, startDate, endDate);
        ResultSet newTimeOff = db.insert("timeoffs", "(employee_id, description, start_day, end_day)", values);

//        if(newTimeOff.next()) {
//            TaskNotification notification = new TaskNotification();
//            notification.createTaskNotificationForTimeoffs(employeeId, employerId,
//                    newTimeOff.getInt("timeoff_id"),
//                    "An employee " + CurrentUser.getUserField("full_name") + " requests a time off during the time " +
//                            "between " + startDate + " and " + endDate);
//        }
    }

    public void approveTimeOff(String timeOffId) throws SQLException, TimeOffAlreadyApprovedException, TaskDoesNotExistException {
        // function where manager can approve time off requested by user
        ResultSet timeOff = selectTimeOff(timeOffId);
        Employer employer = new Employer();

        if (timeOff.next()) {
            if (timeOff.getString("is_approved") != null) {
                throw new TimeOffAlreadyApprovedException("Sorry, but the time off has already been approved or declined by the manager");
            }

            // getting employee who requested time off
            String userId = timeOff.getString("employee_id");
            ResultSet user = employer.selectEmployee(userId);

            if (user.next()) {
                double required_hours = user.getDouble("required_hours");

                // selecting and converting to LocalDate the beginning and the end of time off
                String startDate = timeOff.getString("start_day");
                String endDate = timeOff.getString("end_day");

                LocalDate localStart = LocalDate.parse(startDate);
                LocalDate localEnd = LocalDate.parse(endDate);

                while (localStart.isBefore(localEnd.plusDays(1))) {
                    // adding new slots for every missed day
                    if (localStart.getDayOfWeek().toString() != "SUNDAY") {
                        Slot.createSlotForTimeOff(required_hours / 5,
                                "Approved time off by the manager " + CurrentUser.getUserField("full_name"),
                                userId, localStart.toString());

                    }
                    localStart = localStart.plusDays(1);
                }
                TaskNotification notification = new TaskNotification();
                notification.createTaskNotificationForTimeoffs(CurrentUser.getUserId(), userId,
                        timeOff.getInt("timeoff_id"),
                        "Congratulations, a manager " + CurrentUser.getUserField("full_name") + " had approved your time off");
                db.update("timeoffs", "timeoff_id = " + timeOffId, "is_approved = true ");
            }
        }
    }

    @Override
    public ResultSet selectTimeOff(String timeOffId) {
        return db.get("*", "timeoffs", "timeoff_id = " + timeOffId);
    }

    @Override
    public void updateTimeOff(String timeOffId, HashMap<String, String> properties)
            throws SQLException, TimeOffAlreadyApprovedException, InvalidUpdateFieldsException, TimeOffAlreadyExistsException {
        // a function where user can update time off before it was declined/updated
        ResultSet timeOff = selectTimeOff(timeOffId);

        if (properties.values().size() == 0) return;

        String[] blackList = { "is_approved", "employer_id", "timeoff_id"};

        // limiting fields that can be updated
        for (String field : blackList) {
            if (properties.keySet().contains(field)){
                throw new InvalidUpdateFieldsException("You cannot update is_approved, employer_id, timeoff_id");
            }
        }

        if (timeOff.next()){
            if (timeOff.getString("is_approved") != null){
                throw new TimeOffAlreadyApprovedException("Sorry, but the time off has already been approved or declined by the manager");
            }
            else {
                // if it's not cancelled or approved, update timeoff
                String employee_id = timeOff.getString("employee_id");

                // getting dates from properties if they were passed
                // if not passed, getting existing dates from database
                String startDate = properties.get("start_day") != null ?
                        properties.get("start_day") : timeOff.getString("start_day");
                String endDate = properties.get("end_day") != null ?
                        properties.get("end_day") : timeOff.getString("end_day");

                checkTimeOffDates(employee_id, startDate, endDate, timeOffId);

                // updating notification for that time off
                String message = "An employee " + CurrentUser.getUserField("full_name") + " requests a time off during the time " +
                        "between " + startDate + " and " + endDate;
                db.update("notifications", "timeoff_id = " + timeOffId,
                        "notification_message= '" + message + "'");

                String query = "";
                for (String key : properties.keySet()) {
                    query += key + " = " + "'" + properties.get(key) + "',";
                }
                query = query.substring(0, query.length() - 1);
                db.update("timeoffs", "timeoff_id = " + timeOffId, query);
            }
        }
    }

    @Override
    public void deleteTimeOff(String timeOffId) throws SQLException, TimeOffAlreadyApprovedException {
        // function where user can delete their time off before it was approved/declined
        ResultSet timeOff = selectTimeOff(timeOffId);
        if (timeOff.next()) {
            if (timeOff.getString("is_approved") != null) {
                throw new TimeOffAlreadyApprovedException("Sorry, but the time off has already been approved or declined by the manager");
            }
            else{
                db.delete("notifications", "timeoff_id = " + timeOffId);
                db.delete("timeoffs", "timeoff_id = " + timeOffId);
            }
        }
    }

    @Override
    public void declineTimeOff(String timeOffId) throws SQLException, TimeOffAlreadyApprovedException, TaskDoesNotExistException {
        // declining time off if it's not approved/declined yet
        ResultSet timeOff = selectTimeOff(timeOffId);
        if (timeOff.next()) {
            if (timeOff.getString("is_approved") != null) {
                throw new TimeOffAlreadyApprovedException("Sorry, but the time off has already been approved or declined by the manager");
            }
            else {
                db.update("timeoffs", "timeoff_id = " + timeOffId, "is_approved = false");
                TaskNotification notification = new TaskNotification();
                notification.createTaskNotificationForTimeoffs(CurrentUser.getUserId(), timeOff.getString("employee_id"),
                        timeOff.getInt("timeoff_id"),
                        "Sorry, but A manager " + CurrentUser.getUserField("full_name") + " had declined your time off");
            }
        }
    }


    @Override
    public ResultSet selectAllTimeOffs() throws SQLException, PermissionDeniedException {
        // select all new timeoffs from the company
        if (CurrentUser.getUserField("status").equals("employer")) {
            Employer employer = new Employer();
            ResultSet resultSet = employer.selectAllUsers();
            String inQuery = "(";

            // getting all user_id of the manager
            while (resultSet.next()) {
                inQuery += "'" + resultSet.getString("user_id") + "',";
            }
            inQuery = inQuery.substring(0, inQuery.length() - 1) + ")";

            // if manager has no users
            if (inQuery.equals("()")) return null;

            return db.anotherGetQuery("SELECT * from timeoffs WHERE employee_id IN " + inQuery +
                    " AND is_approved IS NULL ORDER BY start_day ASC");
        }
        else throw new PermissionDeniedException("This function is only for an employer");
    }

    @Override
    public ResultSet selectAllTimeOffsAdmin() throws SQLException, PermissionDeniedException {
        if (CurrentUser.getUserField("status").equals("admin")) {
            return db.getNC("*", "timeoffs");
        }
        else throw new PermissionDeniedException("This function is only for an admin");
    }

    @Override
    public ResultSet selectAllTimeOffsEmployee() throws SQLException, PermissionDeniedException {

        if (CurrentUser.getUserField("status").equals("employee")) {
            return db.get("*", "timeoffs", "employee_id = " + CurrentUser.getUserId());
        }
        else throw new PermissionDeniedException("This function is only for an employee");
    }

    public void checkTimeOffDates(String employeeId, String startDate, String endDate, String timeOffID)
            throws SQLException, TimeOffAlreadyExistsException{
        // function to check if time off for this date already exists
        ResultSet resultSet;

        if (timeOffID == null) { // check for creation of time off
            resultSet = db.get("*", "timeoffs", " employee_id = " + employeeId
                    + " AND (is_approved is NULL OR is_approved = true ) ");
        }
        else { // check for update of time off
            resultSet = db.get("*", "timeoffs", " employee_id = " + employeeId
                    + " AND (is_approved is NULL OR is_approved = true ) AND timeoff_id != " + timeOffID);
        }
        LocalDate localStart = LocalDate.parse(startDate);
        LocalDate localEnd = LocalDate.parse(endDate);

        // check if inserted/updated time off is not within the date
        while (resultSet.next()){
            LocalDate start = LocalDate.parse(resultSet.getString("start_day"));
            LocalDate end = LocalDate.parse(resultSet.getString("end_day"));

            if (localStart.isAfter(start.minusDays(1)) && localStart.isBefore(end.plusDays(1))){
                throw new TimeOffAlreadyExistsException("You already have time off for this date");
            }
            if (localEnd.isAfter(start.minusDays(1)) && localEnd.isBefore(end.plusDays(1))){
                throw new TimeOffAlreadyExistsException("You already have time off for this date");
            }
        }
    }
}
