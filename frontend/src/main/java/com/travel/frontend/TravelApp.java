package com.travel.frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class TravelApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL fxml = getClass().getResource("/com/travel/frontend/fxml/Main.fxml");
        FXMLLoader loader = new FXMLLoader(fxml);
        Scene scene = new Scene(loader.load(), 1100, 700);
        scene.getStylesheets().add(
                getClass().getResource("/com/travel/frontend/css/styles.css").toExternalForm());
        stage.setTitle("Travel Itinerary Planner");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
