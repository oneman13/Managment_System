package application.fx;

import application.api.Database;
import application.api.Slot;
import application.api.exceptions.slots.CannotCreateSlotForFutureException;
import application.api.exceptions.slots.OneDayLimitException;
import application.api.exceptions.tasks.EmployeeDoesNotExistException;
import application.api.exceptions.tasks.TaskDoesNotExistException;
import application.api.exceptions.timeoffs.TimeOffAlreadyApprovedException;
import application.api.exceptions.timeoffs.TimeOffAlreadyExistsException;
import application.api.tasks.Task;
import application.api.timeoffs.Timeoffs;
import application.api.users.CurrentUser;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import org.postgresql.util.PSQLException;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.ResourceBundle;

public class SlotsController extends UserController implements Initializable {
    public ComboBox<String> tasksCombobox;
    public TextField hoursInSlot;
    public TextArea descriptionToSlot;
    public DatePicker slotDate;
    public DatePicker startDate;
    public DatePicker endDate;
    public Label slotDateLabel;
    public Label slotMainLabel;
    public TextArea timeoffDescription;
    public Label timeoffLabel;
    private HashMap<Integer, String> idNamePair;
    private Database db = Database.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Task task = new Task();
            LinkedList<String> tasks = new LinkedList<>();
            idNamePair = new HashMap<>();

            ResultSet tasksResultSet = task.getAllTasksForUser(
                    Integer.parseInt(Objects.requireNonNull(CurrentUser.getUserId()))
            );

            int count = 1;
            while (tasksResultSet.next()) {
                tasks.add(count + ". " + tasksResultSet.getString("task_type"));
                idNamePair.put(count, tasksResultSet.getString("task_id"));
                count++;
            }

            tasksCombobox.setItems(FXCollections.observableList(tasks));
        } catch (SQLException | EmployeeDoesNotExistException e) {
            throw new RuntimeException(e);
        }
    }

    public void createSlot(ActionEvent event) throws SQLException, ParseException, IOException {
        try {
            if (tasksCombobox.getValue() == null) {
                Slot.createSlotWithoutTask(
                        Double.parseDouble(hoursInSlot.getText()),
                        descriptionToSlot.getText(),
                        slotDate.getValue().toString()
                );
            }
            else {
                Slot.createSlot(
                        Integer.parseInt(getTaskId(tasksCombobox.getValue())),
                        Double.parseDouble(hoursInSlot.getText()),
                        descriptionToSlot.getText(),
                        slotDate.getValue().toString()
                );
            }

            toHome(event);

        } catch (CannotCreateSlotForFutureException e) {
            slotMainLabel.setText("You are trying to set slot for future");
            slotMainLabel.setTextFill(Color.RED);
        } catch (NumberFormatException e) {
            slotMainLabel.setText("Set correct number for hours!");
            slotMainLabel.setTextFill(Color.RED);
        } catch (NullPointerException e) {
            slotMainLabel.setText("Fill all fields!");
            slotMainLabel.setTextFill(Color.RED);
        } catch (OneDayLimitException | PSQLException | RuntimeException e) {
            slotMainLabel.setText("You cannot work more than 16 and less than 0 hours a day");
            slotMainLabel.setTextFill(Color.RED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTaskId(String name) {
        String[] strings = name.split("\\.");
        int id = Integer.parseInt(strings[0]);
        return idNamePair.get(id);
    }

    public void cancelCreatingSlot(ActionEvent event) throws IOException {
        toHome(event);
    }

    public void createTimeoff(ActionEvent event) {
        if (startDate.getValue().toString() == null || endDate.getValue().toString() == null) {
            timeoffLabel.setText("Set start and end date properly!");
            timeoffLabel.setTextFill(Color.RED);
        } else {
            try {
                ResultSet employer = db.get("full_name","users",
                        "company = '" + CurrentUser.getUserField("company") +
                                "' AND status = 'employer' LIMIT 1");
                employer.next();
                Timeoffs timeoffs = new Timeoffs();
                timeoffs.createTimeOff(
                        timeoffDescription.getText(),
                        startDate.getValue().toString(),
                        endDate.getValue().toString(),
                        employer.getString("full_name")
                );

                toHome(event);

            } catch (TimeOffAlreadyExistsException e ) {
                timeoffLabel.setText("Time off at these days already exists!");
                System.out.println("!!!");
                timeoffLabel.setTextFill(Color.RED);
            } catch (TaskDoesNotExistException | SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
