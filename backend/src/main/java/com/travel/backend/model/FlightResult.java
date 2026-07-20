package com.travel.backend.model;

public class FlightResult {
    private String itineraryId;
    private String airline;
    private String flightNumber;
    private String originCode;
    private String destinationCode;
    private String originCity;
    private String destinationCity;
    private String departureTime;
    private String arrivalTime;
    private int durationMinutes;
    private int stopCount;
    private double priceRaw;
    private String priceFormatted;

    public FlightResult() {}

    public String getItineraryId() { return itineraryId; }
    public void setItineraryId(String itineraryId) { this.itineraryId = itineraryId; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getOriginCode() { return originCode; }
    public void setOriginCode(String originCode) { this.originCode = originCode; }

    public String getDestinationCode() { return destinationCode; }
    public void setDestinationCode(String destinationCode) { this.destinationCode = destinationCode; }

    public String getOriginCity() { return originCity; }
    public void setOriginCity(String originCity) { this.originCity = originCity; }

    public String getDestinationCity() { return destinationCity; }
    public void setDestinationCity(String destinationCity) { this.destinationCity = destinationCity; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getStopCount() { return stopCount; }
    public void setStopCount(int stopCount) { this.stopCount = stopCount; }

    public double getPriceRaw() { return priceRaw; }
    public void setPriceRaw(double priceRaw) { this.priceRaw = priceRaw; }

    public String getPriceFormatted() { return priceFormatted; }
    public void setPriceFormatted(String priceFormatted) { this.priceFormatted = priceFormatted; }
}
