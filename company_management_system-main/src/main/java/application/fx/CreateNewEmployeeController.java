package application.fx;

import application.api.Database;
import application.api.exceptions.users.InvalidEmailException;
import application.api.users.CurrentUser;
import application.api.users.Employer;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.ResourceBundle;

public class CreateNewEmployeeController extends Controller implements Initializable {
    public Label labelAddingNewEmployee;
    public TextField fullName;
    public TextField emailField;
    public TextField salaryField;
    public TextField requiredHours;
    public TextField occupationField;
    public DatePicker dateOfBirth;
    private Stage stage;
    private Scene scene;
    private final Database db = Database.getInstance();

    public void createEmployee(ActionEvent event) {
        if (dateOfBirth.getValue() == null) {
            labelAddingNewEmployee.setText("Date of Birth cannot be blank!");
            labelAddingNewEmployee.setTextFill(Color.RED);
        } else {
            try {
                Employer employer = new Employer();
                employer.createUser(
                        fullName.getText(),
                        emailField.getText(),
                        CurrentUser.getUserField("company"),
                        Double.parseDouble(salaryField.getText()),
                        Double.parseDouble(requiredHours.getText()),
                        occupationField.getText(),
                        dateOfBirth.getValue().toString()
                );
                // after creating we should check
                // if users created or not
                String where = String.format(
                        "full_name = '%s' AND date_of_birth = '%s' AND email = '%s'",
                        fullName.getText(), dateOfBirth.getValue().toString(), emailField.getText()
                );
                ResultSet employee = db.get("*", "users", where);

                // if created then success
                if (employee.next()) {
                    Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class
                            .getResource("ManagerDashboard.fxml"))
                    );
                    stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                } else {
                    labelAddingNewEmployee.setText("Something went wrong");
                    labelAddingNewEmployee.setTextFill(Color.RED);
                }
            } catch (InvalidEmailException e) {
                labelAddingNewEmployee.setText("Email must be valid");
                labelAddingNewEmployee.setTextFill(Color.RED);
            } catch (NumberFormatException e) {
                labelAddingNewEmployee.setText("Please enter numbers in the correct format!");
                labelAddingNewEmployee.setTextFill(Color.RED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void cancelCreatingNewEmployee(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(AppMain.class
                .getResource("ManagerDashboard.fxml"))
        );
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
