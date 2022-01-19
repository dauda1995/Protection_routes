
package com.example.ptapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.example.ptapp.asset.Asset;


import com.example.ptapp.flir.FrameDataHolder;
import com.example.ptapp.utils.Contracts;
import com.example.ptapp.viewmodels.ThermalImageViewModel;
import com.flir.thermalsdk.androidsdk.ThermalSdkAndroid;

import com.flir.thermalsdk.log.ThermalLog;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity  implements ThermalCamera.OnFragmentInteractionListener{

    EditText assetName, location, status, comment;
    FrameDataHolder frameDataHolder = new FrameDataHolder();
    private Asset extra;
    private static final String TAG = "DetailsActivity";
    ThermalImageViewModel thermalImageViewModel;
    private FragmentTransaction transaction;
    private Asset assetExtra;

    @Override
    public void onFragmentInteraction(FrameDataHolder frameDataHolder) {
        this.frameDataHolder = frameDataHolder;
    }

    public interface ShowMessage {
        void show(String message);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        thermalImageViewModel = new ViewModelProvider(this).get(ThermalImageViewModel.class);
        init();
    }

    private void init(){
        setExtra();

        thermalImageViewModel.backup(assetExtra);
        startDetailFragment();

        ThermalLog.LogLevel enableLoggingInDebug = BuildConfig.DEBUG ? ThermalLog.LogLevel.DEBUG : ThermalLog.LogLevel.NONE;
        ThermalSdkAndroid.init(getApplicationContext(), enableLoggingInDebug);

        LiveData<Boolean> check = thermalImageViewModel.checkStatus();
        check.observe(this, v->{
            if(v){
//                startDiscovery();
//                connect(cameraHandler.getFlirOneEmulator());

                Asset asset =  thermalImageViewModel.getInfo();
                if(assetExtra.imageUrl.equals(Contracts.FEEDER_ASSET_LIST)){
                    asset.uid = assetExtra.uid;

                }
                Log.d(TAG, "init DetailsActivity: this shoild give name" + asset.asset);
                asset.latitude = assetExtra.latitude;
                asset.longitude = assetExtra.longitude;
                asset.imageUrl = assetExtra.imageUrl;
                Log.d(TAG, "saveImage: " + asset.longitude + " "  + asset.latitude);
                asset.location = asset.latitude + "," + asset.longitude;

                startFullScreen(asset);
            }
        });
    }

    private void startFullScreen(Asset asset){
        thermalImageViewModel.setFrag(Contracts.THERMAL_FRAGMENT);
        Intent i = new Intent(this, ThermalCamera.class);
        i.putExtra(Contracts.DETAILS_BUNDLE, asset);
        startActivity(i);

    }

    private void startDetailFragment(){
        thermalImageViewModel.setFrag(Contracts.DETAILS_FRAGMENT);
        Fragment fragment = new FirstFragment();
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.thermal_cont, fragment);
//        transaction.addToBackStack(null);
        transaction.commit();

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
       finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Always close the connection with a connected FLIR ONE when going into background
//        disconnect();
    }


  public void setExtra(){
      assetExtra = getIntent().getParcelableExtra(Contracts.LOCATION_BUNDLE);
  }

  public void retrieveImage(){
      List<String> imageList = new ArrayList<>();
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference().child("images").child(assetExtra.uid);
        mref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String id = dataSnapshot.getValue(String.class);
                    imageList.add(id);
                }
                thermalImageViewModel.setRefs(imageList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        List<Bitmap> images = new ArrayList<>();
      StorageReference storageReference = FirebaseStorage.getInstance().getReference();
      StorageReference photoReference= storageReference.child("images/" + assetExtra.feeder + "/");
      final long ONE_MEGABYTE = 1024 * 1024;
      thermalImageViewModel.getImagerefs().observe(this, img ->{
          final int[] count = {0};

          for(String ref: img){
              photoReference.child(ref).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                  @Override
                  public void onSuccess(byte[] bytes) {
                      Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                      images.add(bmp);
                      count[0] +=1;
                      if(count[0] == img.size()) {
                          thermalImageViewModel.setSavedImages(images);
                      }

                  }
              }).addOnFailureListener(new OnFailureListener() {
                  @Override
                  public void onFailure(@NonNull Exception exception) {
                      Toast.makeText(getApplicationContext(), "No Such file or Path found!!", Toast.LENGTH_LONG).show();
                  }
              });


          }

      });

  }

}