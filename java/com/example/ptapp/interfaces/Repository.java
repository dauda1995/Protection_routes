package com.example.ptapp.interfaces;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ptapp.Route.Route;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public interface Repository {
    public void setSavedRoutes(Route route);
    public MutableLiveData<Route> getSavedRoute();
    public ArrayList<LatLng> getRoutCordinates();
}
