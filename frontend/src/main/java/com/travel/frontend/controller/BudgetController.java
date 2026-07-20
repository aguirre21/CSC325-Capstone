package com.travel.frontend.controller;

import com.travel.frontend.model.ExpenseItem;
import com.travel.frontend.model.FlightInfo;
import com.travel.frontend.model.HotelInfo;
import com.travel.frontend.model.TripSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class BudgetController {

    @FXML private VBox flightsBreakdown;
    @FXML private Label flightCostLabel;
    @FXML private VBox hotelsBreakdown;
    @FXML private Label hotelCostLabel;
    @FXML private TextField foodField;
    @FXML private TextField transportField;
    @FXML private TextField activitiesField;
    @FXML private Label totalLabel;
    @FXML private Label statusLabel;

    @FXML private TextField expenseDescField;
    @FXML private TextField expenseAmountField;
    @FXML private TableView<ExpenseItem> expensesTable;
    @FXML private TableColumn<ExpenseItem, String> colExpenseDesc;
    @FXML private TableColumn<ExpenseItem, String> colExpenseAmount;
    @FXML private TableColumn<ExpenseItem, Void> colExpenseRemove;
    @FXML private Label otherCostLabel;

    @FXML
    public void initialize() {
        TripSession s = TripSession.get();

        buildFlightsBreakdown();
        buildHotelsBreakdown();

        foodField.setText(s.getFoodBudget() > 0 ? String.valueOf(s.getFoodBudget()) : "");
        transportField.setText(s.getTransportBudget() > 0 ? String.valueOf(s.getTransportBudget()) : "");
        activitiesField.setText(s.getActivitiesBudget() > 0 ? String.valueOf(s.getActivitiesBudget()) : "");

        foodField.textProperty().addListener((obs, o, n) -> recalculate());
        transportField.textProperty().addListener((obs, o, n) -> recalculate());
        activitiesField.textProperty().addListener((obs, o, n) -> recalculate());

        colExpenseDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colExpenseAmount.setCellValueFactory(new PropertyValueFactory<>("amountText"));
        colExpenseRemove.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("✕");
            {
                removeBtn.setStyle("-fx-text-fill: #C62828; -fx-background-color: transparent; -fx-font-weight: bold; -fx-cursor: hand;");
                removeBtn.setOnAction(e -> {
                    TripSession.get().removeExpense(getTableView().getItems().get(getIndex()));
                    recalculate();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });
        expensesTable.setItems(s.getOtherExpenses());

        recalculate();
    }

    private void buildFlightsBreakdown() {
        flightsBreakdown.getChildren().clear();
        TripSession s = TripSession.get();
        if (s.getFlights().isEmpty()) {
            flightsBreakdown.getChildren().add(new Label("No flights added"));
        } else {
            for (FlightInfo f : s.getFlights()) {
                flightsBreakdown.getChildren().add(breakdownRow(
                        f.getAirline() + " — " + f.getRoute(), String.format("$%.2f", f.getPriceRaw()),
                        () -> { s.removeFlight(f); buildFlightsBreakdown(); recalculate(); }));
            }
        }
    }

    private void buildHotelsBreakdown() {
        hotelsBreakdown.getChildren().clear();
        TripSession s = TripSession.get();
        if (s.getHotels().isEmpty()) {
            hotelsBreakdown.getChildren().add(new Label("No hotels added"));
        } else {
            for (HotelInfo h : s.getHotels()) {
                hotelsBreakdown.getChildren().add(breakdownRow(
                        h.getName() + " (" + h.getNights() + " nights)", String.format("$%.2f", h.getTotalCost()),
                        () -> { s.removeHotel(h); buildHotelsBreakdown(); recalculate(); }));
            }
        }
    }

    private HBox breakdownRow(String label, String value, Runnable onRemove) {
        Label left = new Label(label);
        left.getStyleClass().add("summary-key");
        Label right = new Label(value);
        right.getStyleClass().add("summary-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button removeBtn = new Button("✕");
        removeBtn.setStyle("-fx-text-fill: #C62828; -fx-background-color: transparent; -fx-font-weight: bold; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> onRemove.run());

        return new HBox(8, left, spacer, right, removeBtn);
    }

    private double parse(TextField f) {
        try { return Double.parseDouble(f.getText().trim()); } catch (Exception e) { return 0; }
    }

    @FXML
    private void addExpense() {
        String desc = expenseDescField.getText().trim();
        if (desc.isBlank()) {
            setStatus("Please enter a description.", true);
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(expenseAmountField.getText().trim());
        } catch (NumberFormatException e) {
            setStatus("Please enter a valid amount.", true);
            return;
        }

        TripSession.get().addExpense(new ExpenseItem(desc, amount));
        expenseDescField.clear();
        expenseAmountField.clear();
        setStatus("Expense added.", false);
        recalculate();
    }

    private void recalculate() {
        TripSession s = TripSession.get();
        double flight = s.getTotalFlightCost();
        double hotel = s.getTotalHotelCost();
        double food = parse(foodField);
        double transport = parse(transportField);
        double activities = parse(activitiesField);
        double other = s.getTotalOtherExpenses();

        flightCostLabel.setText(String.format("$%.2f", flight));
        hotelCostLabel.setText(String.format("$%.2f", hotel));
        otherCostLabel.setText(String.format("$%.2f", other));

        double total = flight + hotel + food + transport + activities + other;
        totalLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    private void saveBudget() {
        TripSession s = TripSession.get();
        s.setFoodBudget(parse(foodField));
        s.setTransportBudget(parse(transportField));
        s.setActivitiesBudget(parse(activitiesField));

        setStatus("Budget saved!", false);
    }

    private void setStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill: #C62828;" : "-fx-text-fill: #2E7D32;");
    }
}
