module work.art {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires javafx.graphics;
    requires jdk.jdi;


    opens work.art to javafx.fxml;
    exports work.art;
}