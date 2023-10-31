package application.fx;

import application.api.Slot;
import application.api.tasks.Task;
import application.api.users.CurrentUser;
import application.api.users.Employer;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class UserInfoForManagerController extends  Controller implements Initializable {
    public VBox tasksForUser;
    public Label orgName;
    public Label fullName;
    public Label email;
    public Label dateOfBirth;
    public Label requiredHours;
    public Label workedHours;
    public Label occupation;
    public ImageView avatar;
    public Label salary;
    public static String id;
    public VBox allSlots;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            orgName.setText(CurrentUser.getUserField("company"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            ResultSet user = (new Employer()).selectEmployee(id);
            user.next();
            fullName.setText(user.getString("full_name"));
            email.setText(user.getString("email"));
            dateOfBirth.setText(user.getString("date_of_birth"));
            workedHours.setText(user.getString("worked_hours"));
            requiredHours.setText(user.getString("required_hours"));
            occupation.setText(user.getString("occupation"));
            String photo = user.getString("photo");
            avatar.setImage(new Image(photo == null ? "photo.jpg" : photo));
            salary.setText(user.getString("salary"));

            ResultSet resultSet = (new Task()).getAllTasksForUser(
                    Integer.parseInt(id)
            );

            while (resultSet.next()) {
                HBox item = FXMLLoader.load(
                        Objects.requireNonNull(getClass().getResource("TasksForUserPage.fxml"))
                );
                ObservableList<Node> list = item.getChildren();

                // correctly format all the data

                Label deadline = (Label) list.get(0);
                deadline.setText(resultSet.getString("deadline").substring(0, 10));
                Label taskType = (Label) list.get(1);
                taskType.setText(resultSet.getString("status"));
                Label description = (Label) list.get(2);
                description.setText(resultSet.getString("description"));

                list.set(0, deadline);
                list.set(1, taskType);
                list.set(2, description);

                tasksForUser.getChildren().add(item);
            }

            ResultSet allSlotsOfUser = Slot.getAllSlots(user.getInt("user_id"));
            Task task = new Task();
            while (allSlotsOfUser.next()) {
                HBox item = FXMLLoader.load(
                        Objects.requireNonNull(getClass().getResource("SlotsRecordForManager.fxml"))
                );
                ObservableList<Node> list = item.getChildren();

                Label date = (Label) list.get(0);
                date.setText(allSlotsOfUser.getString("slot_date"));

                Label taskLabel = (Label) list.get(1);

                ResultSet singeTask = task.showSingleTask(allSlotsOfUser.getInt("task_id"));
                singeTask.next();
                taskLabel.setText(singeTask.getString("task_type"));
                Label description = (Label) list.get(2);
                description.setText(allSlotsOfUser.getString("description"));
                Label hours = (Label) list.get(3);
                hours.setText(allSlotsOfUser.getString("hours"));

                allSlots.getChildren().add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
