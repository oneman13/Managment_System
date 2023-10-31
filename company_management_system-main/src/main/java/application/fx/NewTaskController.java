package application.fx;

import application.api.exceptions.tasks.EmployeeDoesNotExistException;
import application.api.exceptions.tasks.TaskDoesNotExistException;
import application.api.tasks.Task;
import application.api.users.CurrentUser;
import application.api.users.Employer;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class NewTaskController extends Controller implements Initializable {
    public ComboBox<String> employeeComboBox;
    public TextArea descriptionTextArea;
    public TextField taskTypeTextField;
    public DatePicker deadLine;
    public Label newTaskLabel;
    private Stage stage;
    private Scene scene;
    private HashMap<Integer, String> idNamePair;

    public void cancelCreatingNewTask(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("ManagerDashboard.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void createNewTask(ActionEvent event) {
        if (employeeComboBox.getValue() == null) {
            newTaskLabel.setText("There should be the receiver!");
            newTaskLabel.setTextFill(Color.RED);
        } else if (taskTypeTextField.getText().isBlank()) {
            newTaskLabel.setText("Task type should not be empty!");
            newTaskLabel.setTextFill(Color.RED);
        } else if (deadLine.getValue() == null) {
            newTaskLabel.setText("Deadline cannot be blank!");
            newTaskLabel.setTextFill(Color.RED);
        } else if (!(deadLine.getValue() == null) &&
                deadLine.getValue().isBefore(LocalDate.now())) {
            newTaskLabel.setText("Deadline cannot be in the past!");
            newTaskLabel.setTextFill(Color.RED);
        } else {
            Task task = new Task();
            try {
                task.createTask(
                        Integer.parseInt(Objects.requireNonNull(CurrentUser.getUserId())),
                        Integer.parseInt(getId(employeeComboBox.getValue())),
                        descriptionTextArea.getText(),
                        deadLine.getValue().toString(),
                        taskTypeTextField.getText()
                );

                toHome(event);

            } catch (EmployeeDoesNotExistException | SQLException | TaskDoesNotExistException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getId(String name) {
        String[] strings = name.split("\\.");
        int id = Integer.parseInt(strings[0]);
        return idNamePair.get(id);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Employer employer = new Employer();
            ResultSet resultSet = employer.selectAllUsers();
            LinkedList<String> usersNames = new LinkedList<>();

            idNamePair = new HashMap<>();
            int count = 1;
            while (resultSet.next()) {
                if (resultSet.getString("status").equals("employee")) {
                    usersNames.add(count + ". " + resultSet.getString("full_name"));
                    idNamePair.put(count, resultSet.getString("user_id"));
                    count++;
                }

            }
            employeeComboBox.setItems(FXCollections.observableList(usersNames));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
