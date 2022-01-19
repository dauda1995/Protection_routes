package com.example.ptapp;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.example.ptapp.flir.FrameDataHolder;
import com.example.ptapp.utils.Contracts;
import com.example.ptapp.viewmodels.ThermalImageViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ThermalCameraFragment extends Fragment {

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     *
     */

    private static final String TAG = "ThermalCameraFragment";
    private static final boolean AUTO_HIDE = true;
    private View mView;

    private ImageView thermal;
    private ImageView digital;
    private ImageView cancel;
    Button saveImage;
    private FloatingActionButton snapShot, cancelView;
    private FrameDataHolder poll;
    private boolean pause = false;


    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

            Activity activity = getActivity();
            if (activity != null
                    && activity.getWindow() != null) {
                activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            }
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }

        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private View mContentView;
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private ThermalImageViewModel thermalImageViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thermal_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        mVisible = true;

        thermalImageViewModel = new ViewModelProvider(getActivity()).get(ThermalImageViewModel.class);
        setInfo(view);
//        view.findViewById(R.id.save_Image).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        thermalImageViewModel.getPause().observe(getActivity(), v->{
            pause = v;
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            // Clear the systemUiVisibility flag
            getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContentView = null;
        mControlsView = null;
    }


    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

//    @SuppressLint("InlinedApi")
//    private void show() {
//        // Show the system bar
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//        mVisible = true;
//
//        // Schedule a runnable to display UI elements after a delay
//        mHideHandler.removeCallbacks(mHidePart2Runnable);
//        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.show();
//        }
//    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Nullable
    private ActionBar getSupportActionBar() {
        ActionBar actionBar = null;
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            actionBar = activity.getSupportActionBar();
        }
        return actionBar;
    }

    private void setInfo(View v){
        thermal = v.findViewById(R.id.image_thermal);
        digital = v.findViewById(R.id.image_digital);
        snapShot = v.findViewById(R.id.takeShot);
        cancelView = v.findViewById(R.id.cancelView);
        saveImage = v.findViewById(R.id.save_Image);
        cancel = v.findViewById(R.id.continue_route);
        snapedstate(v);
        LiveData<FrameDataHolder> data = thermalImageViewModel.getPolls();
        data.observe(getActivity(), poll->{
            if(pause){
                return;
            }
            thermal.setImageBitmap(poll.msxBitmap);
            digital.setImageBitmap(poll.dcBitmap);
            this.poll = poll;
        });

        snapShot.setOnClickListener(view ->{
           previewState(view);
            thermalImageViewModel.setPause(true);
            thermal.setImageBitmap(poll.msxBitmap);
            digital.setImageBitmap(poll.dcBitmap);

        });

        saveImage.setOnClickListener(view->{
            saveImages(poll);
            startDetailFragment();
            Log.d(TAG, "onClick: popped");
            thermalImageViewModel.setPause(true);
            thermalImageViewModel.startDiscovery(false);


        });
        
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDetailFragment();
                Log.d(TAG, "onClick: popped");
                thermalImageViewModel.setPause(true);
                thermalImageViewModel.startDiscovery(false);

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thermalImageViewModel.setPause(false);
               snapedstate(v);
                thermalImageViewModel.startDiscovery(true);

            }
        });

    }



//    @Override
//    public void onDetach() {
//        super.onDetach();
//        thermalImageViewModel.setPause(true);
//        thermalImageViewModel.startDiscovery(false);
//    }

    private void saveImages(FrameDataHolder dataHolder){
        thermalImageViewModel.setSave(dataHolder);
       

    }

    private void snapedstate(View v){
        snapShot.setVisibility(View.VISIBLE);
        cancelView.setVisibility(View.VISIBLE);
        saveImage.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);

    }

    private void previewState(View v){
        snapShot.setVisibility(View.GONE);
        cancelView.setVisibility(View.GONE);
        saveImage.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
    }

    private void startDetailFragment(){
        Log.d(TAG, "startDetailFragment: this shoius");
        thermalImageViewModel.setFrag(Contracts.DETAILS_FRAGMENT);
        Fragment fragment = new FirstFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.thermal_cont, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }


}