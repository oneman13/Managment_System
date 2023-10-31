module application.fx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;


    opens application.fx to javafx.fxml;
    exports application.fx;
}