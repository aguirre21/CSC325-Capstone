package com.travel.backend.model;

import java.util.ArrayList;
import java.util.List;

public class TripSummaryRequest {
    private String origin;
    private String destination;
    private String departureDate;
    private String returnDate;
    private int travelerCount;
    private String notes;

    private List<FlightSummaryItem> flights = new ArrayList<>();
    private List<HotelSummaryItem> hotels = new ArrayList<>();

    private double foodBudget;
    private double transportBudget;
    private double activitiesBudget;
    private List<ExpenseSummaryItem> otherExpenses = new ArrayList<>();

    public TripSummaryRequest() {}

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

    public List<FlightSummaryItem> getFlights() { return flights; }
    public void setFlights(List<FlightSummaryItem> flights) { this.flights = flights; }

    public List<HotelSummaryItem> getHotels() { return hotels; }
    public void setHotels(List<HotelSummaryItem> hotels) { this.hotels = hotels; }

    public double getFoodBudget() { return foodBudget; }
    public void setFoodBudget(double foodBudget) { this.foodBudget = foodBudget; }

    public double getTransportBudget() { return transportBudget; }
    public void setTransportBudget(double transportBudget) { this.transportBudget = transportBudget; }

    public double getActivitiesBudget() { return activitiesBudget; }
    public void setActivitiesBudget(double activitiesBudget) { this.activitiesBudget = activitiesBudget; }

    public List<ExpenseSummaryItem> getOtherExpenses() { return otherExpenses; }
    public void setOtherExpenses(List<ExpenseSummaryItem> otherExpenses) { this.otherExpenses = otherExpenses; }
}
