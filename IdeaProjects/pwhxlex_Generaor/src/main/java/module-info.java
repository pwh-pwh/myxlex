module com.coderpwh.pwhxlex_generaor {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.coderpwh.pwhxlex_generaor to javafx.fxml;
    exports com.coderpwh.pwhxlex_generaor;
}