package com.travel.frontend.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class HotelInfo {

    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty location = new SimpleStringProperty();
    private final StringProperty rating = new SimpleStringProperty();
    private final StringProperty pricePerNight = new SimpleStringProperty();
    private final StringProperty stay = new SimpleStringProperty();

    private String id;
    private double pricePerNightRaw;
    private String checkIn;
    private String checkOut;
    private int nights;

    public HotelInfo(String id, String name, String location, double ratingValue, String reviewCount,
                      double pricePerNightRaw, String priceFormatted,
                      LocalDate checkIn, LocalDate checkOut) {
        this.id = id;
        this.name.set(name);
        this.location.set(location);
        this.rating.set(ratingValue > 0 ? ratingValue + " ★ (" + reviewCount + ")" : "—");
        this.pricePerNightRaw = pricePerNightRaw;
        this.pricePerNight.set(priceFormatted + "/night");
        this.checkIn = checkIn.toString();
        this.checkOut = checkOut.toString();
        this.nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        this.stay.set(this.checkIn + " → " + this.checkOut + " (" + nights + "n)");
    }

    public StringProperty nameProperty() { return name; }
    public StringProperty locationProperty() { return location; }
    public StringProperty ratingProperty() { return rating; }
    public StringProperty pricePerNightProperty() { return pricePerNight; }
    public StringProperty stayProperty() { return stay; }

    public String getId() { return id; }
    public String getName() { return name.get(); }
    public String getLocation() { return location.get(); }
    public double getPricePerNightRaw() { return pricePerNightRaw; }
    public String getCheckIn() { return checkIn; }
    public String getCheckOut() { return checkOut; }
    public int getNights() { return nights; }
    public double getTotalCost() { return pricePerNightRaw * nights; }
}
