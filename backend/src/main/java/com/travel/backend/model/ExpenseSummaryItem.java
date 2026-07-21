package com.travel.backend.model;

public class ExpenseSummaryItem {
    private String description;
    private double amount;

    public ExpenseSummaryItem() {}

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
