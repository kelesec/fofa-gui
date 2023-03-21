package club.keleya.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 600);
        stage.setTitle("Fofa-Gui v1.0 by kele");
        stage.setScene(scene);
        stage.show();
    }

//    public static void main(String[] args) {
//        launch();
//    }
}
