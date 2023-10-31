package application.fx;

import application.api.Database;
import application.api.exceptions.tasks.TaskDoesNotExistException;
import application.api.exceptions.timeoffs.TimeOffAlreadyApprovedException;
import application.api.tasks.Task;
import application.api.timeoffs.Timeoffs;
import application.api.users.CurrentUser;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;

public class TimeoffsForManagerController extends Controller implements Initializable {
    public VBox timeoffs;
    public Label orgName;
    private final Database db = Database.getInstance();
    private HashMap<String, String> countToTaskID = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Timeoffs timeoffs = new Timeoffs();
        try {
            ResultSet resultSet = timeoffs.selectAllTimeOffs();
            orgName.setText(CurrentUser.getUserField("company"));

            int count;
            for (count = 1; resultSet.next(); count++) {
                HBox item = FXMLLoader.load(
                        Objects.requireNonNull(getClass().getResource("TimeoffRecord.fxml"))
                );

                ObservableList<Node> list = item.getChildren();
                ResultSet person = db.get("full_name", "users",
                        "user_id = " + resultSet.getString("employee_id"));
                person.next();

                Label numberOfTask = new Label(Integer.toString(count));
                numberOfTask.setMinWidth(83);
                numberOfTask.setAlignment(Pos.CENTER);

                Label fullName = new Label(person.getString("full_name"));
                fullName.setMinWidth(180);
                fullName.setAlignment(Pos.CENTER);

                Label startDay = new Label(resultSet.getString("start_day"));
                startDay.setMinWidth(170);
                startDay.setAlignment(Pos.CENTER);

                Label endDay = new Label(resultSet.getString("end_day"));
                endDay.setMinWidth(170);
                endDay.setAlignment(Pos.CENTER);

                Label description = new Label(resultSet.getString("description"));
                description.setMinWidth(240);
                description.setAlignment(Pos.CENTER);

                list.set(0, numberOfTask);
                list.set(2, fullName);
                list.set(3, startDay);
                list.set(4, endDay);
                list.set(5, description);

                this.timeoffs.getChildren().add(item);

                countToTaskID.put(Integer.toString(count), resultSet.getString("timeoff_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void declineTimeoff(ActionEvent event) throws Exception{
        ObservableList<Node> allTasks = timeoffs.getChildren();
        Timeoffs timeoffs = new Timeoffs();

        for (Node taskNode : allTasks) {
            HBox taskRecord = (HBox) taskNode;
            ObservableList<Node> taskAttributes = taskRecord.getChildren();

            Label number = (Label) taskAttributes.get(0);
            CheckBox checkBox = (CheckBox) taskAttributes.get(1);
            if (checkBox.isSelected()) {
                timeoffs.declineTimeOff(countToTaskID.get(number.getText()));
            }
        }
    }

    public void acceptTimeoff(ActionEvent event) throws Exception {
        ObservableList<Node> allTasks = timeoffs.getChildren();
        Timeoffs timeoffs = new Timeoffs();

        for (Node taskNode : allTasks) {
            HBox taskRecord = (HBox) taskNode;
            ObservableList<Node> taskAttributes = taskRecord.getChildren();

            Label number = (Label) taskAttributes.get(0);
            CheckBox checkBox = (CheckBox) taskAttributes.get(1);
            if (checkBox.isSelected()) {
                timeoffs.approveTimeOff(countToTaskID.get(number.getText()));
            }
        }
    }
}
