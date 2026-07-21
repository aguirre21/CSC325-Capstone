package com.travel.backend.model;

public class FlightSummaryItem {
    private String airline;
    private String flightNumber;
    private String origin;
    private String destination;
    private String departure;
    private String arrival;
    private int durationMinutes;
    private int stops;
    private double cost;

    public FlightSummaryItem() {}

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDeparture() { return departure; }
    public void setDeparture(String departure) { this.departure = departure; }

    public String getArrival() { return arrival; }
    public void setArrival(String arrival) { this.arrival = arrival; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getStops() { return stops; }
    public void setStops(int stops) { this.stops = stops; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
}
