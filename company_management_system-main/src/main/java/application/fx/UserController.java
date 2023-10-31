package application.fx;

import application.api.Slot;
import application.api.tasks.Task;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.ResourceBundle;

public class UserController implements Initializable {
    public VBox items;
    private Stage stage;
    private Scene scene;

    public void toHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("UserMain.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void toLeaderboards(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("UserLeaderboards.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void toInfo(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("UserInfo.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void signOut(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("LogIn.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void addingNewSlot(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("Slots.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void seeAllTasks(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("AllTasksForUser.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            ResultSet resultSet = Slot.getAllSlots();
            Task tasks = new Task();

            while (resultSet.next()) {
                HBox item = FXMLLoader.load(
                        Objects.requireNonNull(getClass().getResource("SlotRecord.fxml"))
                );
                ObservableList<Node> list = item.getChildren();

                // correctly format all the data
                Label date = new Label(resultSet.getString("slot_date"));
                date.setMinWidth(100);
                date.setAlignment(Pos.CENTER);

                Label task;
                if (resultSet.getString("task_id") == null) {
                    task = new Label("no task");
                } else {
                    ResultSet taskByID = tasks.showSingleTask(resultSet.getInt("task_id"));
                    taskByID.next();
                    task = new Label(taskByID.getString("description"));
                }
                task.setMinWidth(200);
                task.setAlignment(Pos.CENTER);

                Label description;
                if (resultSet.getString("description") == null) {
                    description = new Label("no description");
                } else {
                    description = new Label(resultSet.getString("description"));
                }
                description.setMinWidth(456);
                description.setAlignment(Pos.CENTER);

                Label hours = new Label(resultSet.getString("hours"));
                hours.setMinWidth(100);
                hours.setAlignment(Pos.CENTER);

                list.set(0, date);
                list.set(1, task);
                list.set(2, description);
                list.set(3, hours);

                items.getChildren().add(item);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

