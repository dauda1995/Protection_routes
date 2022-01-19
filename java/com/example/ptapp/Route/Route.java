package com.example.ptapp.Route;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Route {

    public int id;
    public String feeder;
    public List<Route> cordinates;
    public LatLng latLng
   ;

    public Route() {
    }

    public Route(int id, Double lat, Double longitude){
        this.id = id;
       latLng = new LatLng(lat, longitude);

    }

    public Route(int id, String feeder, List<Route> cordinates) {
        this.id = id;
        this.feeder = feeder;
        this.cordinates = cordinates;

    }
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", id);
        result.put("feeder", feeder);
        result.put("cords", cordinates);
        return result;
    }

}
