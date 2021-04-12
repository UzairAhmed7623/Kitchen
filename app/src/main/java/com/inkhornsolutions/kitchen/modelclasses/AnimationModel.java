package com.inkhornsolutions.kitchen.modelclasses;

import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class AnimationModel {
    private boolean isRunning;
    private GeoQueryModel geoQueryModel;

    //Moving marker
    private List<LatLng> polylineList = new ArrayList<>();
    private Handler handler;
    private int index, next;
    private LatLng start, end;
    private float v;
    private double lat, lng;


    public AnimationModel(boolean isRunning, GeoQueryModel geoQueryModel) {
        this.isRunning = isRunning;
        this.geoQueryModel = geoQueryModel;
        this.handler = new Handler(Looper.myLooper());
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public GeoQueryModel getGeoQueryModel() {
        return geoQueryModel;
    }

    public void setGeoQueryModel(GeoQueryModel geoQueryModel) {
        this.geoQueryModel = geoQueryModel;
    }

    public List<LatLng> getPolylineList() {
        return polylineList;
    }

    public void setPolylineList(List<LatLng> polylineList) {
        this.polylineList = polylineList;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    public LatLng getStart() {
        return start;
    }

    public void setStart(LatLng start) {
        this.start = start;
    }

    public LatLng getEnd() {
        return end;
    }

    public void setEnd(LatLng end) {
        this.end = end;
    }

    public float getV() {
        return v;
    }

    public void setV(float v) {
        this.v = v;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
