package com.example.ptapp.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.ptapp.DetailsActivity;
import com.example.ptapp.MapsActivity;
import com.example.ptapp.R;
import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler;
import com.flir.thermalsdk.live.Identity;


public class SplashActivity extends AppCompatActivity {
    SplashViewModel splashViewModel;
    LiveData<Boolean> timer;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        initSplashViewModel();

    }

    private void initSplashViewModel() {
        splashViewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        splashViewModel.sleepThread();
        splashViewModel.mCheck.observe(this, bool ->{
            if(bool == true){
                goToMainActivity();
            }
        });

    }


    private void goToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }



}