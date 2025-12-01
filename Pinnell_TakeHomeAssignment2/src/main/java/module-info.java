module ca.georgiancollege.pinnell_takehomeassignment2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires java.xml;
    requires java.desktop;
    requires javafx.base;

    opens ca.georgiancollege.pinnell_takehomeassignment2 to javafx.fxml;
    exports ca.georgiancollege.pinnell_takehomeassignment2;
}