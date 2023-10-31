package application.fx;

import application.api.exceptions.users.IncorrectPasswordException;
import application.api.exceptions.users.InvalidPasswordException;
import application.api.exceptions.users.PasswordsDontMatchException;
import application.api.users.Employee;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class ChangePasswordController extends UserController{
    public TextField oldPassField;
    private Stage stage;
    private Scene scene;
    public Label mainLabel;
    public TextField passField;
    public TextField rePassField;

    public void submitChanges(ActionEvent event) {
        try {
            Employee employee = new Employee();
            employee.changePassword(
                    oldPassField.getText(),
                    passField.getText(),
                    rePassField.getText()
            );
            toHome(event);
        } catch (PasswordsDontMatchException e) {
            mainLabel.setText("Passwords do not match");
            mainLabel.setTextFill(Color.RED);
        } catch (IncorrectPasswordException e) {
            mainLabel.setText("Incorrect password!");
            mainLabel.setTextFill(Color.RED);
        } catch (InvalidPasswordException e) {
            mainLabel.setText("Invalid password");
            mainLabel.setTextFill(Color.RED);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void cancelChanging(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("UserMain.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
