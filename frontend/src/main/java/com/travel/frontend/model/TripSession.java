package com.travel.frontend.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TripSession {

    private static final TripSession INSTANCE = new TripSession();

    private String origin = "";
    private String destination = "";
    private String departureDate = "";
    private String returnDate = "";
    private int travelerCount = 1;
    private String notes = "";

    private final ObservableList<FlightInfo> flights = FXCollections.observableArrayList();
    private final ObservableList<HotelInfo> hotels = FXCollections.observableArrayList();
    private final ObservableList<ExpenseItem> otherExpenses = FXCollections.observableArrayList();

    private double foodBudget = 0.0;
    private double transportBudget = 0.0;
    private double activitiesBudget = 0.0;

    private TripSession() {}

    public static TripSession get() { return INSTANCE; }

    public double getTotalCost() {
        return getTotalFlightCost() + getTotalHotelCost()
                + foodBudget + transportBudget + activitiesBudget + getTotalOtherExpenses();
    }

    public double getTotalOtherExpenses() {
        return otherExpenses.stream().mapToDouble(ExpenseItem::getAmount).sum();
    }

    public ObservableList<ExpenseItem> getOtherExpenses() {
        return otherExpenses;
    }

    public void addExpense(ExpenseItem expense) {
        otherExpenses.add(expense);
    }

    public void removeExpense(ExpenseItem expense) {
        otherExpenses.remove(expense);
    }

    public double getTotalFlightCost() {
        return flights.stream().mapToDouble(FlightInfo::getPriceRaw).sum();
    }

    public double getTotalHotelCost() {
        return hotels.stream().mapToDouble(HotelInfo::getTotalCost).sum();
    }

    public ObservableList<FlightInfo> getFlights() {
        return flights;
    }

    public void addFlight(FlightInfo flight) {
        flights.add(flight);
    }

    public void removeFlight(FlightInfo flight) {
        flights.remove(flight);
    }

    public ObservableList<HotelInfo> getHotels() {
        return hotels;
    }

    public void addHotel(HotelInfo hotel) {
        hotels.add(hotel);
    }

    public void removeHotel(HotelInfo hotel) {
        hotels.remove(hotel);
    }

    public double getCostPerPerson() {
        return travelerCount > 0 ? getTotalCost() / travelerCount : getTotalCost();
    }

    // --- Getters and setters ---

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDepartureDate() { return departureDate; }
    public void setDepartureDate(String departureDate) { this.departureDate = departureDate; }

    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }

    public int getTravelerCount() { return travelerCount; }
    public void setTravelerCount(int travelerCount) { this.travelerCount = travelerCount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public double getFoodBudget() { return foodBudget; }
    public void setFoodBudget(double foodBudget) { this.foodBudget = foodBudget; }

    public double getTransportBudget() { return transportBudget; }
    public void setTransportBudget(double transportBudget) { this.transportBudget = transportBudget; }

    public double getActivitiesBudget() { return activitiesBudget; }
    public void setActivitiesBudget(double activitiesBudget) { this.activitiesBudget = activitiesBudget; }
}
