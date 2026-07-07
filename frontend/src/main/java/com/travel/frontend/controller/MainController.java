package com.travel.frontend.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.net.URL;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Button btnFlights;
    @FXML private Button btnHotel;
    @FXML private Button btnBudget;
    @FXML private Button btnSummary;

    @FXML
    public void initialize() {
        loadView("FlightSearch");
        setActive(btnFlights);
    }

    @FXML private void showFlights() { loadView("FlightSearch"); setActive(btnFlights); }
    @FXML private void showHotel() { loadView("Hotel"); setActive(btnHotel); }
    @FXML private void showBudget() { loadView("Budget"); setActive(btnBudget); }
    @FXML private void showSummary() { loadView("Summary"); setActive(btnSummary); }

    private void loadView(String name) {
        try {
            URL fxml = getClass().getResource("/com/travel/frontend/fxml/" + name + ".fxml");
            Node view = FXMLLoader.load(fxml);
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActive(Button active) {
        for (Button b : new Button[]{btnFlights, btnHotel, btnBudget, btnSummary}) {
            b.getStyleClass().remove("nav-active");
        }
        active.getStyleClass().add("nav-active");
    }
}
