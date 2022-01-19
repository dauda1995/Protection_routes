package com.example.ptapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ptapp.adaptors.imageAdaptor;
import com.example.ptapp.asset.Asset;
import com.example.ptapp.viewmodels.ThermalImageViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {

    EditText assetName, location, insulation, status, recommendation;
    FloatingActionButton thermal;
    Spinner feeder_spin;
    FloatingActionButton submit;
    RecyclerView thermRcy;
    private View mView;
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    ImageView mxc, dxc;

    private static final String TAG = "FirstFragment";
    ThermalImageViewModel thermalImageViewModel;
    private Asset restore;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_first, container, false);
        return mView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        thermalImageViewModel = new ViewModelProvider(getActivity()).get(ThermalImageViewModel.class);
        thermalImageViewModel.getListPoll().observe(getActivity(), v->{
            if (mView instanceof RecyclerView) {
                Context context = mView.getContext();
                RecyclerView recyclerView = (RecyclerView) mView;
                if (mColumnCount <= 1) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                } else {
                    recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                }
                recyclerView.setAdapter(new imageAdaptor(v));
            }

        });

        init(view);
    }

    private void init(View v) {
        assetName = v.findViewById(R.id.feeder_sub);
        location = v.findViewById(R.id.isolateddt);
        feeder_spin = v.findViewById(R.id.voltage);
        insulation = v.findViewById(R.id.date_of_occurr);
        status = v.findViewById(R.id.duration_of_outage);
        recommendation = v.findViewById(R.id.details);
        thermal = v.findViewById(R.id.submit_fb);
        dxc = v.findViewById(R.id.dxc);
        mxc = v.findViewById(R.id.mxc);
        Toast.makeText(getActivity(), "Add new Asset details and attached thermal images which are uploaded to Firebase database and can be retrieved", Toast.LENGTH_SHORT).show();
//        thermRcy = v.findViewById(R.id.recyclerview);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(v.getContext(), R.array.feeder, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feeder_spin.setAdapter(adapter);

//        for the first run when fragment is started, let subref return a false value
//        thermalImageViewModel.subcheck(false);

        thermalImageViewModel.getSavedImages().observe(getActivity(), img  ->{
            Log.d(TAG, "init: " + img.size());
            dxc.setImageBitmap(img.get(0));
        });
        thermalImageViewModel.subref().observe(getActivity(), sub->{
            if(sub){
                startSubmit();
            }
        });
        thermalImageViewModel.getSavePollLive().observe(getActivity(), saveLive->{
            thermalImageViewModel.setSavePoll(saveLive);
            dxc.setImageBitmap(saveLive.dcBitmap);
            mxc.setImageBitmap(saveLive.msxBitmap);
        });

        thermal.setOnClickListener(mv->{
            Asset asset = getInfo();
            thermalImageViewModel.setInfo(asset);
            if(assetName.getText() == null || asset.insulation == null){
                Toast.makeText(getActivity(), "Input Fields first before taking a picture", Toast.LENGTH_SHORT).show();
                return;
            }
            thermalImageViewModel.setPause(false);
            thermalImageViewModel.startDiscovery(true);

        });
        restore = thermalImageViewModel.restore();
        Log.d(TAG, "init: " + restore.latitude);
        try {
            if (restore.asset.equals(null)) {
                Log.d(TAG, "init: asset is null");
            } else {
                Log.d(TAG, "init: asset is " + restore.asset);
                setInfo(restore);
                retrieveImage();
            }
        }catch (Exception e){
            Log.d(TAG, "init: " + e.getMessage());
        }

    }


    private Asset getInfo(){
        Asset asset = new Asset();

        asset.asset = assetName.getText().toString();
        asset.location = location.getText().toString();
        asset.status = status.getText().toString();
        asset.insulation = insulation.getText().toString();
        asset.recommendation = recommendation.getText().toString();
        asset.feeder = (String) feeder_spin.getSelectedItem();
        Log.d(TAG, "getInfo: this should say feeder " + asset.asset);

        return asset;

    }

    private void startSubmit(){
       Asset asset = getInfo();
        Log.d(TAG, "startSubmit: " + asset.asset);
       thermalImageViewModel.setInfo(asset);
        Log.d(TAG, "startSubmit: " + asset.asset + " " + asset.insulation);
    }

    private void setInfo(Asset asset){
        assetName.setText(asset.asset);
        location.setText(asset.location);
        insulation.setText(asset.status);
        status.setText(asset.status);
        recommendation.setText(asset.recommendation);


    }

    public void retrieveImage(){
        List<String> imageList = new ArrayList<>();
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference().child("images").child(restore.uid);
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
        Log.d(TAG, "retrieveImage: " + restore.feeder);
        StorageReference photoReference= storageReference.child("images/" + restore.feeder );
        final long ONE_MEGABYTE = 1024 * 1024;
        thermalImageViewModel.getImagerefs().observe(getActivity(), img ->{
            final int[] count = {0};

            for(String ref: img){
                Log.d(TAG, "retrieveImage: " + ref);
                photoReference.child(ref).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        images.add(bmp);
                        count[0] +=1;
                        if(count[0] == img.size()) {
                            thermalImageViewModel.setSavedImages(images);

                        }
                        Log.d(TAG, "onSuccess: this is a sucess");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getActivity(), "No Such file or Path found!!", Toast.LENGTH_LONG).show();
                    }
                });


            }

        });

    }

}