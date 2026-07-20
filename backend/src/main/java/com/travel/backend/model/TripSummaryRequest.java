package com.travel.backend.model;

import java.util.ArrayList;
import java.util.List;

public class TripSummaryRequest {
    private String tripName;
    private String destination;
    private String startDate;
    private String endDate;
    private List<FlightSummaryItem> flights = new ArrayList<>();
    private List<HotelSummaryItem> hotels = new ArrayList<>();
    private List<ExpenseSummaryItem> expenses = new ArrayList<>();

    public TripSummaryRequest() {
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<FlightSummaryItem> getFlights() {
        return flights;
    }

    public void setFlights(List<FlightSummaryItem> flights) {
        this.flights = flights;
    }

    public List<HotelSummaryItem> getHotels() {
        return hotels;
    }

    public void setHotels(List<HotelSummaryItem> hotels) {
        this.hotels = hotels;
    }

    public List<ExpenseSummaryItem> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<ExpenseSummaryItem> expenses) {
        this.expenses = expenses;
    }
}
