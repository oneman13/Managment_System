package application.fx;

import application.api.tasks.Task;
import application.api.users.CurrentUser;
import application.api.users.Employer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.ResourceBundle;

public class ManagerDashboardController extends Controller implements Initializable {
    public ComboBox<String> employeesCombobox;
    public Label orgName;
    public ImageView notification;
    private Stage stage;
    private Scene scene;
    private HashMap<Integer, String> idNamePair;
    public VBox items;
    private HashMap <String, String> countToTaskID = new HashMap<>();


    public void addingNewTask(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("NewTask.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void enteringNewEmployee(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("AddingNewEmployee.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Task task = new Task();
        try {
            ResultSet resultSet = task.getAllTasksForEmployer();

            int count;
            for (count = 1; resultSet.next(); count++) {
                HBox item = FXMLLoader.load(
                        Objects.requireNonNull(getClass().getResource("TaskForManager.fxml"))
                );

                ObservableList<Node> list = item.getChildren();

                Label numberOfTask = new Label(Integer.toString(count));
                numberOfTask.setMinWidth(83);
                numberOfTask.setAlignment(Pos.CENTER);

                CheckBox checked = new CheckBox();
                checked.setMinWidth(17);

                Label taskType = new Label(resultSet.getString("task_type"));
                taskType.setMinWidth(150);
                taskType.setAlignment(Pos.CENTER);

                Label description = new Label(resultSet.getString("description"));
                description.setMinWidth(260);
                description.setAlignment(Pos.CENTER);

                Label assignee = new Label(resultSet.getString("full_name"));
                assignee.setMinWidth(150);
                assignee.setAlignment(Pos.CENTER);

                Label status = new Label(resultSet.getString("status"));
                status.setMinWidth(103);
                status.setAlignment(Pos.CENTER);

                Label deadline = new Label(resultSet.getString("deadline").substring(0, 10));
                deadline.setMinWidth(103);
                deadline.setAlignment(Pos.CENTER);

                list.set(0, numberOfTask);
                list.set(1, checked);
                list.set(2, taskType);
                list.set(3, description);
                list.set(4, assignee);
                list.set(5, status);
                list.set(6, deadline);

                items.getChildren().add(item);

                countToTaskID.put(Integer.toString(count), resultSet.getString("task_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            employeesCombobox.setItems(FXCollections.observableList(usersNames));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            orgName.setText(CurrentUser.getUserField("company"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getId(String name) {
        String[] strings = name.split("\\.");
        int id = Integer.parseInt(strings[0]);
        return idNamePair.get(id);
    }

    public void selectEmployee(ActionEvent event) throws IOException {
        Parent root;
        UserInfoForManagerController.id = getId(employeesCombobox.getValue().substring(0,1));
        root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("UserInfoForManager.fxml")));

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void allTimeoffs(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("TimeoffsForManager.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void approveTask(ActionEvent event) throws Exception{
        ObservableList<Node> allTasks = items.getChildren();
        Task task = new Task();

        for (Node taskNode : allTasks) {
            HBox taskRecord = (HBox) taskNode;
            ObservableList<Node> taskAttributes = taskRecord.getChildren();

            Label number = (Label) taskAttributes.get(0);
            CheckBox checkBox = (CheckBox) taskAttributes.get(1);
            if (checkBox.isSelected()) {
                task.updateTaskStatus(
                        Integer.parseInt(countToTaskID.get(number.getText())),
                        "completed"
                );
            }
        }
    }

    public void deleteTask(ActionEvent event) throws Exception{
        ObservableList<Node> allTasks = items.getChildren();
        Task task = new Task();

        for (Node taskNode : allTasks) {
            HBox taskRecord = (HBox) taskNode;
            ObservableList<Node> taskAttributes = taskRecord.getChildren();

            Label number = (Label) taskAttributes.get(0);
            CheckBox checkBox = (CheckBox) taskAttributes.get(1);
            if (checkBox.isSelected()) {
                task.deleteTask(Integer.parseInt(countToTaskID.get(number.getText())));
            }
        }
    }
}
