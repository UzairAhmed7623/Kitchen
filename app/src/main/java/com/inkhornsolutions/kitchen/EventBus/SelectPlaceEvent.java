package com.inkhornsolutions.kitchen.EventBus;

import com.google.android.gms.maps.model.LatLng;

public class SelectPlaceEvent {
    private LatLng origin, destination;
    private String originString, destinationString;
    private String address;
    private String dropOffUserId;
    private String orderId;

    public SelectPlaceEvent(LatLng origin, LatLng destination, String originString, String destinationString, String address, String dropOffUserId, String orderId) {
        this.origin = origin;
        this.destination = destination;
        this.originString = originString;
        this.destinationString = destinationString;
        this.address = address;
        this.dropOffUserId = dropOffUserId;
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDropOffUserId() {
        return dropOffUserId;
    }

    public void setDropOffUserId(String dropOffUserId) {
        this.dropOffUserId = dropOffUserId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getOrigin() {
        return origin;
    }

    public void setOrigin(LatLng origin) {
        this.origin = origin;
    }

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public String getOriginString() {
        return originString;
    }

    public void setOriginString(String originString) {
        this.originString = originString;
    }

    public String getDestinationString() {
        return destinationString;
    }

    public void setDestinationString(String destinationString) {
        this.destinationString = destinationString;
    }
}
