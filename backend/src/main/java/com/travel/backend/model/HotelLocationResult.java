package com.travel.backend.model;

public class HotelLocationResult {
    private int geoId;
    private String name;
    private String subtitle;

    public HotelLocationResult() {
    }

    public HotelLocationResult(int geoId, String name, String subtitle) {
        this.geoId = geoId;
        this.name = name;
        this.subtitle = subtitle;
    }

    public int getGeoId() {
        return geoId;
    }

    public void setGeoId(int geoId) {
        this.geoId = geoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
}
