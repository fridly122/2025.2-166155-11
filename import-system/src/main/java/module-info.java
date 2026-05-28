module itss.group11 {
    requires javafx.controls;
    requires javafx.fxml;

    opens itss.group11 to javafx.fxml;
    exports itss.group11;
}
