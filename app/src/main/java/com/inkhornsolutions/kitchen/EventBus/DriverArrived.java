package com.inkhornsolutions.kitchen.EventBus;

public class DriverArrived {
    private String tripKey;

    public DriverArrived(String tripKey) {
        this.tripKey = tripKey;
    }

    public String getTripKey() {
        return tripKey;
    }

    public void setTripKey(String tripKey) {
        this.tripKey = tripKey;
    }
}
