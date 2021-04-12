package com.inkhornsolutions.kitchen.EventBus;

public class DriverAcceptTripEvent {
    private String tripId;

    public DriverAcceptTripEvent(String tripId) {
        this.tripId = tripId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
