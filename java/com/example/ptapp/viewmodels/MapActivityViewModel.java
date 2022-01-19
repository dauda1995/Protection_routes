package com.example.ptapp.viewmodels;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ptapp.Joints.Joints;
import com.example.ptapp.Route.Route;
import com.example.ptapp.asset.Asset;
import com.example.ptapp.interfaces.Repository;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MapActivityViewModel extends ViewModel implements Repository {



    Route route;
    List<Route> routes = new ArrayList<>();
    MutableLiveData<Route> store = new MutableLiveData<>();
    MutableLiveData<List<Route>> storePre = new MutableLiveData<>();
    private List<Route> preRoutes = new ArrayList<>();
    MutableLiveData<LatLng> coordinates = new MutableLiveData<>();
    MutableLiveData<List<Asset>> assetMarkers = new MutableLiveData<>();
    List<Route> previousFireRoutes = new ArrayList<>();
    MutableLiveData<List<Route>> routeFireLive = new MutableLiveData<>();
    MutableLiveData<List<Joints>> jointObjs = new MutableLiveData<>();

    @Override
    public void setSavedRoutes(Route route) {
        this.route = route;
        routes.add(route);

    }

    public void clearRoute(){
        routes.clear();
    }


    public void setAssetMarkers(List<Asset> arr){
        assetMarkers.postValue(arr);
    }

    public void setPreviousFireRoutes(List<Route> route){
        routeFireLive.setValue(route);


    }

    public MutableLiveData<List<Route>> getPreviousFireRoutes(){
        return routeFireLive;
    }

    public void setJointObjs(List<Joints> joints){
        jointObjs.setValue(joints);
    }

    public MutableLiveData<List<Joints>> getJointObjs(){
        return jointObjs;
    }


    public MutableLiveData<List<Asset>> getAssetMarkers(){
        return assetMarkers;
    }

//    public void postSavedRoutes(Route route){
//        store.postValue(routes);
//    }

    @Override

    public MutableLiveData<Route> getSavedRoute() {
        return store;
    }

    @Override
    public ArrayList<LatLng> getRoutCordinates() {

        return null;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setPreviousRoutes(List<Route> route) {
        preRoutes = route;

    }

    public List<Route> getPrevRoutes(){
        return preRoutes;

    }

    public MutableLiveData<LatLng> getLatLng(){
        return coordinates;
    }

    public void setLatLng(LatLng latLng){
        coordinates.postValue(latLng);
    }


}
