package application.fx;

import application.api.Slot;
import application.api.tasks.Task;
import application.api.users.CurrentUser;
import application.api.users.Employer;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class UserInfoController extends UserController implements Initializable {
    public VBox tasksForUser;
    public Label orgName;
    private Stage stage;
    private Scene scene;
    public Label fullName;
    public Label email;
    public Label dateOfBirth;
    public Label requiredHours;
    public Label workedHours;
    public Label occupation;
    public ImageView avatar;
    public Label salary;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            orgName.setText(CurrentUser.getUserField("company"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            ResultSet user = (new Employer()).selectEmployee(CurrentUser.getUserId());
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
                    Integer.parseInt(Objects.requireNonNull(CurrentUser.getUserId()))
            );

            while (resultSet.next()) {
                HBox item = FXMLLoader.load(
                        Objects.requireNonNull(getClass().getResource("TasksForUserPage.fxml"))
                );
                ObservableList<Node> list = item.getChildren();

                // correctly format all the data

                Label deadline = (Label) list.get(0);
                deadline.setText(resultSet.getString("deadline").substring(0,10));
                Label taskType = (Label) list.get(1);
                taskType.setText(resultSet.getString("status"));
                Label description = (Label) list.get(2);
                description.setText(resultSet.getString("description"));

                list.set(0, deadline);
                list.set(1, taskType);
                list.set(2, description);

                tasksForUser.getChildren().add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void goToUpdatePage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("UserInfoChangePage.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
