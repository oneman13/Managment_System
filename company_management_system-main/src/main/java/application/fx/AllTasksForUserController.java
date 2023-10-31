package application.fx;

import application.api.tasks.Task;
import application.api.users.CurrentUser;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;

public class AllTasksForUserController extends UserController implements Initializable {
    public VBox items;
    private HashMap <String, String> countToTaskID = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Task task = new Task();
        try {
            ResultSet resultSet = task.getAllNotCompletedTasksForUser(
                    Integer.parseInt(Objects.requireNonNull(CurrentUser.getUserId()))
            );
            int count;
            for (count = 1; resultSet.next(); count++) {
                HBox item = FXMLLoader.load(
                        Objects.requireNonNull(getClass().getResource("UpdateTasksForUser.fxml"))
                );

                ObservableList<Node> list = item.getChildren();

                Label numberOfTask = new Label(Integer.toString(count));
                numberOfTask.setMinWidth(83);
                numberOfTask.setAlignment(Pos.CENTER);

                CheckBox checked = new CheckBox();
                checked.setMinWidth(17);

                Label taskType = new Label(resultSet.getString("task_type"));
                taskType.setMinWidth(200);
                taskType.setAlignment(Pos.CENTER);

                Label description = new Label(resultSet.getString("description"));
                description.setMinWidth(355);
                description.setAlignment(Pos.CENTER);

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
                list.set(4, status);
                list.set(5, deadline);

                items.getChildren().add(item);

                countToTaskID.put(Integer.toString(count), resultSet.getString("task_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void submitForReview(ActionEvent event) throws Exception {
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
                        "in review"
                );
            }
            seeAllTasks(event);
        }
    }
}
