package application.api.tasks;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.DateFormat;
import java.util.Date;

import application.api.Database;

// importing exceptions
import application.api.exceptions.tasks.EmployeeDoesNotExistException;
import application.api.exceptions.tasks.InvalidInputFieldsException;
import application.api.exceptions.tasks.TaskDoesNotExistException;
import application.api.users.CurrentUser;

// Task interface with all needed methods
interface TaskInterface {
    void createTask(int creatorID, int assigneeID, String description, String deadline, String task_type)
            throws EmployeeDoesNotExistException, InvalidInputFieldsException, SQLException, TaskDoesNotExistException;
    void deleteTask(int task_id)
            throws TaskDoesNotExistException, SQLException;
    void updateTask(int task_id, String deadline, String task_type, String status, String completion_date)
            throws TaskDoesNotExistException, EmployeeDoesNotExistException, InvalidInputFieldsException, SQLException, ParseException;
    ResultSet showSingleTask(int task_id)
            throws TaskDoesNotExistException, SQLException;
    ResultSet getAllTasksForUser(int user_id)
            throws EmployeeDoesNotExistException, SQLException;

    ResultSet getAllNotCompletedTasksForUser(int user_id)
            throws EmployeeDoesNotExistException, SQLException;

    ResultSet getAllTasksForEmployer() throws SQLException;

    ResultSet getAllTasks()
        throws SQLException;

    void updateTaskStatus(int task_id, String status)
            throws TaskDoesNotExistException, InvalidInputFieldsException, SQLException, ParseException;
}


public class Task implements TaskInterface{
    // instance of Database cass which is used to make simpler queries to DB
    Database db = Database.getInstance();
    // main table with tasks
    String tasksTable = "tasks";

    @Override
    public void updateTaskStatus(int task_id, String status) throws TaskDoesNotExistException,
            InvalidInputFieldsException, SQLException, ParseException {

        ResultSet task = this.showSingleTask(task_id);

        // checks weather the task exists or not
        if (!taskExists(task_id)) {
            throw new TaskDoesNotExistException("There is no task with such id");
        }

        // Checks if entered status is correct
        if (!isStatusCorrect(status)) {
            throw new InvalidInputFieldsException("Wrong task status!");
        }

        // If query has returned task -> then process update
        if (task.next()) {

            String deadline = task.getString("deadline");

            // automatically sets completion date when status changes
            if (Objects.equals(status, "in review") || (Objects.equals(status, "completed") && task.getString("completion_date") == null)) {
                String completion_date;

                Date date = Calendar.getInstance().getTime();
                DateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
                String stringCurrentDate = currentDate.format(date);
                completion_date = stringCurrentDate;

                // parse string date to date object
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                // sets status  'done late' if task is overdue
                if (sdf.parse(deadline).before(sdf.parse(completion_date))) {
                    status = "done late";
                }

                db.update(tasksTable, "task_id = " + task_id,
                        "status = '" + status +"', completion_date = '" + completion_date +"'");
            }

            // update status from review to completed
            else if (Objects.equals(status, "completed") && task.getString("completion_date") != null){
                db.update(tasksTable, "task_id = " + task_id,
                        "status = '" + status +"'");
            }

            // update status to 'to do' or 'in progress'
            else {
                db.update(tasksTable, "task_id = " + task_id,
                        "status = '" + status +"', completion_date = null");
            }
        }
    }


    // checks whether task with given id exists or not
    public boolean taskExists(int task_id) throws SQLException {
        ResultSet task = db.get("*", tasksTable, "task_id = " + task_id);

        return task.next();
    }

    // checks if inputted status is valid
    public boolean isStatusCorrect(String status){
        ArrayList<String> taskStatusOptions = new ArrayList<>();
        String[] taskStatuses = {"to do", "completed", "in review", "in progress", "done late"};
        taskStatusOptions.addAll(List.of(taskStatuses));

        return taskStatusOptions.contains(status);
    }

    // check whether user with given id exists or not
    public boolean userExists(int user_id) throws SQLException {
        ResultSet user = db.get("*", "users", "user_id = " + user_id);

        return user.next();
    }


    @Override
    public void createTask(int creatorID, int assigneeID, String description, String deadline, String task_type)
            throws EmployeeDoesNotExistException, SQLException, TaskDoesNotExistException {

        String initialStatus = "to do";

        if(!userExists(assigneeID)){
            throw new EmployeeDoesNotExistException("Employee with given id does not exist!");
        }

         String values = String.format(" (%o, '%s', '%s', '%s', '%s')", creatorID, deadline, description, task_type, initialStatus);
         ResultSet generatedTask = db.insert(tasksTable, "(creator_id, deadline, description, task_type, status)", values);

         if (generatedTask.next()) {
             long task_id = generatedTask.getLong(1);

             // adds values of user and task to the table in order to implement many-to-many relationship
             db.insert("users_tasks", "(user_id, task_id)", "(" +assigneeID + ", " + task_id +")");

             // creating notification about new task
             TaskNotification notification = new TaskNotification();
             notification.createTaskNotification(creatorID, assigneeID, (int) task_id, "New task have been assigned!");

         }
    }


    @Override
    public void deleteTask(int task_id)
            throws TaskDoesNotExistException, SQLException {

        if(!taskExists(task_id)){
            throw new TaskDoesNotExistException("There is no task with such id");
        }

        // deletes records about these tasks from slots
        db.delete("slots", "task_id = " + task_id);

        // deletes records from the table of many-to-many relationship
        db.delete("users_tasks", "task_id = " + task_id);

        // deletes all notifications about existed task
        db.delete("notifications", "task_id = " + task_id);

        db.delete(tasksTable, "task_id = " + task_id);

    }


    @Override
    public void updateTask(int task_id, String deadline, String task_type, String status, String completion_date)
            throws TaskDoesNotExistException, InvalidInputFieldsException, SQLException, ParseException {

        if(!taskExists(task_id)){
            throw new TaskDoesNotExistException("There is no task with such id");
        }

        if (!isStatusCorrect(status)){
            throw new InvalidInputFieldsException("Wrong task status!");
        }

        // automatically sets completion date when status changes
        if (status == "in review"){
            Date date = Calendar.getInstance().getTime();
            DateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String stringCurrentDate = currentDate.format(date);
            completion_date = stringCurrentDate;
        }

        // parse string date to date object
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        // sets status  'done late' if task is overdue
        if(sdf.parse(deadline).before(sdf.parse(completion_date))){
            status = "done late";
        }

        String values = String.format("deadline = '%s', task_type = '%s', status = '%s', completion_date = '%s'",
                deadline, task_type, status, completion_date);

        db.update(tasksTable, "task_id = " + task_id,
                values);
    }


    @Override
    public ResultSet showSingleTask(int task_id)
            throws TaskDoesNotExistException, SQLException {

        ResultSet task = db.get("*", tasksTable, "task_id = " + task_id);

        if(!taskExists(task_id)){
            throw new TaskDoesNotExistException("There is no task with such id");
        }

        return task;
    }

    @Override
    public ResultSet getAllTasksForUser(int user_id) throws SQLException, EmployeeDoesNotExistException {

        if (! userExists(user_id)){
            throw new EmployeeDoesNotExistException("This user does not exist!");
        }

        // query to get all task of specific user
        String query = "SELECT task_id, description, creator_id, deadline, task_type, status, completion_date\n" +
                "FROM tasks\n" +
                "\tINNER JOIN users_tasks USING(task_id)\n" +
                "WHERE user_id = " + user_id;

        ResultSet tasks = db.anotherGetQuery(query);
        return tasks;
    }

    @Override
    public ResultSet getAllNotCompletedTasksForUser(int user_id) throws SQLException, EmployeeDoesNotExistException {

        if(!userExists(user_id)){
            throw new EmployeeDoesNotExistException("This user does not exist!");
        }

        // gets all tasks which are not completed
        String query = "SELECT task_id, description, creator_id, deadline, task_type, status, completion_date\n" +
                "FROM tasks\n" +
                "\tINNER JOIN users_tasks USING(task_id)\n" +
                "WHERE status != 'completed' AND user_id = "+ user_id;

        ResultSet tasks = db.anotherGetQuery(query);
        return tasks;
    }

    @Override
    public ResultSet getAllTasks() throws SQLException {

        ResultSet tasks = db.getNC("*", tasksTable);

        return tasks;
    }

    @Override
    public ResultSet getAllTasksForEmployer() throws SQLException {
        return db.anotherGetQuery("SELECT users.full_name, tasks.* from users INNER JOIN users_tasks USING(user_id)" +
                " INNER JOIN tasks USING(task_id) WHERE creator_id = " + CurrentUser.getUserId());
    }
}
