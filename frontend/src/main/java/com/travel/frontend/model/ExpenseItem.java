package com.travel.frontend.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ExpenseItem {

    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty amountText = new SimpleStringProperty();
    private final double amount;

    public ExpenseItem(String description, double amount) {
        this.description.set(description);
        this.amount = amount;
        this.amountText.set(String.format("$%.2f", amount));
    }

    public StringProperty descriptionProperty() { return description; }
    public StringProperty amountTextProperty() { return amountText; }

    public String getDescription() { return description.get(); }
    public double getAmount() { return amount; }
}
