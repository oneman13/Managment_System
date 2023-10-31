package application.fx;

import application.api.users.UsersStats;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;
import java.util.ResourceBundle;

public class LeaderBoardsController extends Controller implements Initializable {
    public ComboBox<String> sortByComboBox;
    public Label label1;
    public Label label2;
    public VBox records;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UsersStats usersStats = new UsersStats();
        try {
                ArrayList<String> typesOfSorting = new ArrayList<>();
                typesOfSorting.add("worked hours");
                typesOfSorting.add("tasks");
                typesOfSorting.add("timeoffs");
                sortByComboBox.setItems(FXCollections.observableList(typesOfSorting));
                sortByComboBox.setPromptText("sort by ...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeSorting(ActionEvent event) {
        String sortingType = sortByComboBox.getValue();
        UsersStats usersStats = new UsersStats();

        switch (sortingType) {
            case "worked hours":
                try {
                    usersStats.calculateHoursAll();
                    ResultSet resultSet = usersStats.selectTopCincoUsers("worked_hours");

                    for (int counter = 0; resultSet.next(); counter++) {
                        HBox item = FXMLLoader.load(
                                Objects.requireNonNull(getClass().getResource("RecordForLeaderBoards.fxml"))
                        );

                        ObservableList<Node> list = item.getChildren();

                        // correctly format all the data
                        Label fullName = new Label(resultSet.getString("full_name"));
                        fullName.setMinWidth(390);
                        fullName.setAlignment(Pos.CENTER);
                        Label workedHours = new Label(resultSet.getString("worked_hours"));
                        workedHours.setMinWidth(120);
                        workedHours.setAlignment(Pos.CENTER);
                        Label requiredHours = new Label(resultSet.getString("required_hours"));
                        requiredHours.setMinWidth(120);
                        requiredHours.setAlignment(Pos.CENTER);

                        // photo.jpg is default photo
                        String photo = resultSet.getString("photo");
                        ImageView imageView = new ImageView(photo == null ? "photo.jpg" : photo);
                        imageView.setFitHeight(120);
                        imageView.setFitWidth(84);

                        // set all values into our list
                        list.set(0, fullName);
                        list.set(1, workedHours);
                        list.set(2, requiredHours);
                        list.set(3, imageView);

                        // set photo background color
                        if (counter == 0) item.setStyle("-fx-background-color: #FFD700"); // golden color
                        else if (counter == 1) item.setStyle("-fx-background-color: #C0C0C0"); // silver
                        else if (counter == 2) item.setStyle("-fx-background-color: #CD7F32"); // bronze

                        records.getChildren().removeAll();
                        records.getChildren().add(item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "tasks":
                try {
                    usersStats.calculateHoursAll();
                    ResultSet resultSet = usersStats.selectTopCincoUsers("tasks");

                    for (int counter = 0; resultSet.next(); counter++) {
                        HBox item = FXMLLoader.load(
                                Objects.requireNonNull(getClass().getResource("RecordForLeaderBoards.fxml"))
                        );

                        ObservableList<Node> list = item.getChildren();

                        Label fullName = new Label(resultSet.getString("full_name"));
                        fullName.setMinWidth(390);
                        fullName.setAlignment(Pos.CENTER);
                        Label workedHours = new Label(resultSet.getString("tasks"));
                        workedHours.setMinWidth(120);
                        workedHours.setAlignment(Pos.CENTER);
                        Label some = new Label("");
                        some.setMinWidth(120);

                        // photo.jpg is default photo
                        String photo = resultSet.getString("photo");
                        ImageView imageView = new ImageView(photo == null ? "photo.jpg" : photo);
                        imageView.setFitHeight(120);
                        imageView.setFitWidth(84);

                        // set all values into our list
                        list.set(0, fullName);
                        list.set(1, workedHours);
                        list.set(2, some);
                        list.set(3, imageView);

                        // set photo background color
                        if (counter == 0) item.setStyle("-fx-background-color: #FFD700"); // golden color
                        else if (counter == 1) item.setStyle("-fx-background-color: #C0C0C0"); // silver
                        else if (counter == 2) item.setStyle("-fx-background-color: #CD7F32"); // bronze

                        label1.setText("Number of tasks");
                        label2.setText(" ");
                        records.getChildren().removeAll();
                        records.getChildren().add(item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "timeoffs":
                try {
                    usersStats.calculateHoursAll();
                    ResultSet resultSet = usersStats.selectTopCincoUsers("timeoffs");

                    for (int counter = 0; resultSet.next(); counter++) {
                        HBox item = FXMLLoader.load(
                                Objects.requireNonNull(getClass().getResource("RecordForLeaderBoards.fxml"))
                        );

                        ObservableList<Node> list = item.getChildren();

                        Label fullName = new Label(resultSet.getString("full_name"));
                        fullName.setMinWidth(390);
                        fullName.setAlignment(Pos.CENTER);
                        Label workedHours = new Label(resultSet.getString("timeoffs"));
                        workedHours.setMinWidth(120);
                        workedHours.setAlignment(Pos.CENTER);
                        Label some = new Label("");
                        some.setMinWidth(120);

                        // photo.jpg is default photo
                        String photo = resultSet.getString("photo");
                        ImageView imageView = new ImageView(photo == null ? "photo.jpg" : photo);
                        imageView.setFitHeight(120);
                        imageView.setFitWidth(84);

                        // set all values into our list
                        list.set(0, fullName);
                        list.set(1, workedHours);
                        list.set(2, some);
                        list.set(3, imageView);

                        // set photo background color
                        if (counter == 0) item.setStyle("-fx-background-color: #FFD700"); // golden color
                        else if (counter == 1) item.setStyle("-fx-background-color: #C0C0C0"); // silver
                        else if (counter == 2) item.setStyle("-fx-background-color: #CD7F32"); // bronze

                        label1.setText("# of days");
                        label2.setText(" ");
                        records.getChildren().removeAll();
                        records.getChildren().add(item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
