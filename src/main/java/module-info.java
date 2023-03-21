module club.keleya.fofagui {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.jsoup;
    requires commons.csv;


    opens club.keleya.controller to javafx.fxml;
    exports club.keleya.controller;
    opens club.keleya.data to javafx.fxml;
    exports club.keleya.data;
}