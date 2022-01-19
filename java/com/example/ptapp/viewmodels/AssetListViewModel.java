package com.example.ptapp.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ptapp.asset.Asset;
import com.example.ptapp.interfaces.Repository;

import java.util.ArrayList;
import java.util.List;

public class AssetListViewModel extends ViewModel {

    MutableLiveData<List<Asset>> assetListLive = new MutableLiveData<>();
    List<Asset> assetList = new ArrayList<>();

    public void setAssetList(List<Asset> assetList){
        assetListLive.postValue(assetList);
    }

    public MutableLiveData<List<Asset>> getAssetList(){
        return assetListLive;
    }
}
