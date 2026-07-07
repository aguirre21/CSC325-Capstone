package com.travel.frontend.model;

import javafx.beans.property.*;

public class FlightInfo {

    private final StringProperty airline = new SimpleStringProperty();
    private final StringProperty flightNumber = new SimpleStringProperty();
    private final StringProperty route = new SimpleStringProperty();
    private final StringProperty departure = new SimpleStringProperty();
    private final StringProperty arrival = new SimpleStringProperty();
    private final StringProperty duration = new SimpleStringProperty();
    private final StringProperty stops = new SimpleStringProperty();
    private final StringProperty price = new SimpleStringProperty();

    private String itineraryId;
    private String originCode;
    private String destinationCode;
    private double priceRaw;
    private int durationMinutes;
    private int stopCount;
    private String departureRaw;
    private String arrivalRaw;

    public FlightInfo(String itineraryId, String airline, String flightNumber,
                      String originCode, String destinationCode,
                      String originCity, String destinationCity,
                      String departure, String arrival,
                      int durationMinutes, int stopCount,
                      double priceRaw, String priceFormatted) {
        this.itineraryId = itineraryId;
        this.airline.set(airline);
        this.flightNumber.set(flightNumber != null ? flightNumber : "N/A");
        this.originCode = originCode;
        this.destinationCode = destinationCode;
        this.route.set(originCity + " (" + originCode + ") → " + destinationCity + " (" + destinationCode + ")");
        this.departureRaw = departure;
        this.arrivalRaw = arrival;
        this.departure.set(formatTime(departure));
        this.arrival.set(formatTime(arrival));
        this.durationMinutes = durationMinutes;
        this.stopCount = stopCount;
        this.priceRaw = priceRaw;
        int h = durationMinutes / 60;
        int m = durationMinutes % 60;
        this.duration.set(h + "h " + m + "m");
        this.stops.set(stopCount == 0 ? "Nonstop" : stopCount + " stop(s)");
        this.price.set(priceFormatted);
    }

    private String formatTime(String iso) {
        if (iso == null || iso.length() < 16) return iso != null ? iso : "-";
        return iso.substring(0, 16).replace("T", " ");
    }

    public StringProperty airlineProperty() { return airline; }
    public StringProperty flightNumberProperty() { return flightNumber; }
    public StringProperty routeProperty() { return route; }
    public StringProperty departureProperty() { return departure; }
    public StringProperty arrivalProperty() { return arrival; }
    public StringProperty durationProperty() { return duration; }
    public StringProperty stopsProperty() { return stops; }
    public StringProperty priceProperty() { return price; }

    public String getItineraryId() { return itineraryId; }
    public String getAirline() { return airline.get(); }
    public String getFlightNumber() { return flightNumber.get(); }
    public String getOriginCode() { return originCode; }
    public String getDestinationCode() { return destinationCode; }
    public double getPriceRaw() { return priceRaw; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getStopCount() { return stopCount; }
    public String getDepartureRaw() { return departureRaw; }
    public String getArrivalRaw() { return arrivalRaw; }
    public String getRoute() { return route.get(); }
}
