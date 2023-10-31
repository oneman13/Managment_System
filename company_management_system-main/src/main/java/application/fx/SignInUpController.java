package application.fx;

import application.api.exceptions.users.InvalidEmailException;
import application.api.exceptions.users.InvalidPasswordException;
import application.api.exceptions.users.PasswordsDontMatchException;
import application.api.users.Authorization;
import application.api.users.CurrentUser;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class SignInUpController extends Controller implements Initializable {
    public TextField emailField;
    public PasswordField passField;
    public TextField fullName;
    public TextField organization;
    public Label layout;
    public PasswordField rePassField;
    private Stage stage;
    private Scene scene;

    public void switchToSignIn(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("LogIn.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToSignUp(ActionEvent event) throws IOException{
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class.getResource("SignUp.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void signIn(ActionEvent event) throws IOException {
        Authorization authorization = new Authorization();
        try {
            if (authorization.authorize(emailField.getText(), passField.getText())) {
                Parent root;
                if (Objects.equals(CurrentUser.getUserField("status"), "employer")) {
                    root = FXMLLoader.load(Objects.requireNonNull(
                            AppMain.class.getResource("ManagerDashboard.fxml"))
                    );
                } else {
                    root = FXMLLoader.load(Objects.requireNonNull(
                            AppMain.class.getResource("UserInfo.fxml"))
                    );
                }
                stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } else {
                layout.setText("Incorrect email or password");
                layout.setTextFill(Color.RED);
            }
        } catch (SQLException e) {
            System.out.println("Something went wrong");
        }
    }

    public void signUp(ActionEvent event) {
        Authorization authorization = new Authorization();
        try {
            if (authorization.register(
                    emailField.getText(),
                    passField.getText(),
                    rePassField.getText(),
                    fullName.getText(),
                    organization.getText()
            )) {
                Parent root = FXMLLoader.load(Objects.requireNonNull(
                        AppMain.class.getResource("ManagerDashboard.fxml"))
                );
                stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } else {
                System.out.println("Something is wrong");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvalidPasswordException e) {
            layout.setText("Password must be at least 8 characters!");
            layout.setTextFill(Color.RED);
        } catch (PasswordsDontMatchException e) {
            layout.setText("Passwords must match!");
            layout.setTextFill(Color.RED);
        } catch (InvalidEmailException e) {
            layout.setText("Email must be valid!");
            layout.setTextFill(Color.RED);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
