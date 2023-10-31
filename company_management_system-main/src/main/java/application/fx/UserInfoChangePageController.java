package application.fx;

import application.api.exceptions.users.InvalidEmailException;
import application.api.exceptions.users.InvalidUpdateFieldsException;
import application.api.users.CurrentUser;
import application.api.users.Employee;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;

public class UserInfoChangePageController extends UserController implements Initializable {
    public TextField fullNameField;
    public TextField emailField;
    public Label mainLabel;
    private Stage stage;
    private Scene scene;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            fullNameField.setText(CurrentUser.getUserField("full_name"));
            emailField.setText(CurrentUser.getUserField("email"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void submitChanges(ActionEvent event) {
        Employee employee = new Employee();
        HashMap<String, String> values = new HashMap<>();
        values.put("full_name", fullNameField.getText());
        values.put("email", emailField.getText());
        try {
            employee.UpdateMe(values);
            Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("UserInfo.fxml")));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }catch (InvalidUpdateFieldsException | SQLException | IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidEmailException e) {
            mainLabel.setText("Email must be valid");
            mainLabel.setTextFill(Color.RED);
        }
    }

    public void cancelChanging(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("UserInfo.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void changePassword(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("ChangePassword.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
