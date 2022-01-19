package com.example.ptapp.viewmodels;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ptapp.asset.Asset;
import com.example.ptapp.flir.FrameDataHolder;

import java.util.ArrayList;
import java.util.List;

public class ThermalImageViewModel extends ViewModel {
    MutableLiveData<Boolean> start = new MutableLiveData<>();
    Asset info ;
    MutableLiveData<FrameDataHolder> polls = new MutableLiveData<>();
    MutableLiveData<Boolean> pause = new MutableLiveData<>();
    MutableLiveData<List<FrameDataHolder>> listPoll = new MutableLiveData<>();
    MutableLiveData<Boolean> sub = new MutableLiveData<>();
    List<FrameDataHolder> listrecyc = new ArrayList<>();
    String activity;
    MutableLiveData<List<String>> imagerefs = new MutableLiveData<>();
    MutableLiveData<List<Bitmap>> savedImages = new MutableLiveData<>();
    int val = 0;
    private static final String TAG = "ThermalImageViewModel";


    FrameDataHolder savePoll = new FrameDataHolder();
    MutableLiveData<FrameDataHolder> savePollLive = new MutableLiveData<>();

    private Asset asset1 = new Asset();
    private Boolean boo = false;

    public void setPoll(FrameDataHolder poll){
        polls.postValue(poll);
    }

    public MutableLiveData<FrameDataHolder> getPolls(){
        return polls;
    }

    public void startDiscovery(Boolean bool){
        start.setValue(bool);
    }

    public MutableLiveData<Boolean> checkStatus(){
        return start;
    }

    public void setInfo(Asset asset){
        info = asset;
    }

    public Asset getInfo(){
        return info;
    }

    public void setPause(Boolean bool){
        pause.setValue(bool);
    }

    public MutableLiveData<Boolean> getPause(){
        return pause;
    }

    public void setSave(FrameDataHolder poll){
//        listrecyc.add(poll);
        savePollLive.setValue(poll);
//        savePoll = poll;

    }

    public void setSavePoll(FrameDataHolder frameDataHolder){
//        savePoll = frameDataHolder;
        Log.d(TAG, "setSavePoll: conspiracy");
    }



    public MutableLiveData<List<FrameDataHolder>> getListPoll(){
        listPoll.setValue(listrecyc);
        return listPoll;
    }

    public FrameDataHolder getSavePoll(){
        return savePoll;
    }

    public MutableLiveData<FrameDataHolder> getSavePollLive(){
        return savePollLive;
    }

    public void subcheck(Boolean bool){
        sub.setValue(bool);
    }

    public MutableLiveData<Boolean> subref(){
        return sub;
    }

    public void setFrag(int value){
        val = value;
    }

    public int getFrag(){
        return val;
    }

    public void backup(Asset asset){
        asset1 = asset;
    }

    public Asset restore(){
        return asset1;
    }

    public Boolean getImage(){
        return boo;
    }


    public void setImage(Boolean bool){
        boo = bool;
    }

    public void setActivity(String activity){
        this.activity = activity;
    }

    public String getActivity(){
        return activity;
    }

    public MutableLiveData<List<String>> getImagerefs(){
        return imagerefs;
    }
    public void setRefs(List<String> refs){
        imagerefs.postValue(refs);
    }

    public MutableLiveData<List<Bitmap>> getSavedImages(){
        return savedImages;
    }
    public void setSavedImages(List<Bitmap> uris){
        savedImages.postValue(uris);
    }
}
