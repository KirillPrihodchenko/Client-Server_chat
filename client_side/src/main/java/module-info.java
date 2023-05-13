module com.ui.client_ui {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.ui.client_ui to javafx.fxml;
    exports com.ui.client_ui;
}