package com.travel.backend.model;

public class AirportResult {
    private String skyId;
    private String entityId;
    private String name;
    private String subtitle;

    public AirportResult() {}

    public AirportResult(String skyId, String entityId, String name, String subtitle) {
        this.skyId = skyId;
        this.entityId = entityId;
        this.name = name;
        this.subtitle = subtitle;
    }

    public String getSkyId() { return skyId; }
    public void setSkyId(String skyId) { this.skyId = skyId; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    @Override
    public String toString() {
        return name + " (" + skyId + ")";
    }
}
