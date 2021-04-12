package com.inkhornsolutions.kitchen.EventBus;

public class TimeUp {
    private String tripId;

    public TimeUp(String tripId) {
        this.tripId = tripId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
