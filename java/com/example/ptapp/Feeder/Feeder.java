package com.example.ptapp.Feeder;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class Feeder {

    private String id;
    private String feeder;
    private String voltage;
    private int joint;
    private int len;
    private String location;
    private ArrayList<LatLng> route;
    private String code;

    public Feeder() {
    }

    public Feeder(String id, String feeder, String voltage, int joint, int len, ArrayList<LatLng> route, String code, String location) {
        this.id = id;
        this.feeder = feeder;
        this.voltage = voltage;
        this.joint = joint;
        this.len = len;
        this.route = route;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFeeder() {
        return feeder;
    }

    public void setFeeder(String feeder) {
        this.feeder = feeder;
    }

    public String getVoltage() {
        return voltage;
    }

    public void setVoltage(String voltage) {
        this.voltage = voltage;
    }

    public int getJoint() {
        return joint;
    }

    public void setJoint(int joint) {
        this.joint = joint;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public ArrayList<LatLng> getRoute() {
        return route;
    }

    public void setRoute(ArrayList<LatLng> route) {
        this.route = route;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}