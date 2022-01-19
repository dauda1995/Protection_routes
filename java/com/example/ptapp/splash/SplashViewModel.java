package com.example.ptapp.splash;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


public class SplashViewModel extends AndroidViewModel {

    public MutableLiveData<Boolean> mCheck;

    public SplashViewModel(Application application) {
        super(application);

    }

    public void sleepThread() {
        mCheck = new MutableLiveData<>();
        mCheck.setValue(false);
        Thread timer = new Thread() {
            public void run() {
                try {
                    //Display for 3 seconds
                    sleep(3000);
                } catch (InterruptedException e) {
                    // TODO: handle exception
                    e.printStackTrace();
                } finally {
                    mCheck.postValue(true);
                }
            }
        };
        timer.start();
    }
}