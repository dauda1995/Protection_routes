package com.example.ptapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ptapp.asset.Asset;
import com.example.ptapp.flir.CameraHandler;
import com.example.ptapp.flir.FrameDataHolder;
import com.example.ptapp.flir.PermissionHandler;
import com.example.ptapp.utils.Contracts;
import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler;
import com.flir.thermalsdk.image.ThermalImage;
import com.flir.thermalsdk.live.CommunicationInterface;
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.connectivity.ConnectionStatusListener;
import com.flir.thermalsdk.live.discovery.DiscoveryEventListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class ThermalCamera extends AppCompatActivity {

    private PermissionHandler permissionHandler;
    private CameraHandler cameraHandler;
    private Identity connectedIdentity = null;

    private LinkedBlockingQueue<FrameDataHolder> framesBuffer = new LinkedBlockingQueue(21);
    private UsbPermissionHandler usbPermissionHandler = new UsbPermissionHandler();
    private static final String TAG = "ThermalCamera";

    private ImageView thermal;
    private ImageView digital;
    private ImageView cancel;
    Button saveImage;
    private FloatingActionButton snapShot, cancelView;
    private FrameDataHolder poll;
    private boolean pause = false;
    private OnFragmentInteractionListener mListener;


    public interface ShowMessage {
        void show(String message);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(FrameDataHolder frameDataHolder);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermal_camera);
//        mListener = (OnFragmentInteractionListener) ThermalCamera.this;


        permissionHandler = new PermissionHandler(showMessage, ThermalCamera.this);

        cameraHandler = new CameraHandler();
        startDiscovery();
//        connect(cameraHandler.getFlirOneEmulator());

        setInfo();
    }

    private void connect(Identity identity) {
        //We don't have to stop a discovery but it's nice to do if we have found the camera that we are looking for
        cameraHandler.stopDiscovery(discoveryStatusListener);

        if (connectedIdentity != null) {
            Log.d(TAG, "connect(), in *this* code sample we only support one camera connection at the time");
            showMessage.show("connect(), in *this* code sample we only support one camera connection at the time");
            return;
        }

        if (identity == null) {
            Log.d(TAG, "connect(), can't connect, no camera available");
            showMessage.show("connect(), can't connect, no camera available");
            return;
        }

        connectedIdentity = identity;

        updateConnectionText(identity, "CONNECTING");
        //IF your using "USB_DEVICE_ATTACHED" and "usb-device vendor-id" in the Android Manifest
        // you don't need to request permission, see documentation for more information
        if (UsbPermissionHandler.isFlirOne(identity)) {
            usbPermissionHandler.requestFlirOnePermisson(identity, this, permissionListener);
        } else {

            doConnect(identity);
        }

    }

    private DetailsActivity.ShowMessage showMessage = new DetailsActivity.ShowMessage() {
        @Override
        public void show(String message) {
            Toast.makeText(ThermalCamera.this, message, Toast.LENGTH_SHORT).show();
        }
    };

    private UsbPermissionHandler.UsbPermissionListener permissionListener = new UsbPermissionHandler.UsbPermissionListener() {
        @Override
        public void permissionGranted(Identity identity) {

            doConnect(identity);
        }

        @Override
        public void permissionDenied(Identity identity) {
            Log.d(TAG, "Permission was denied for identity ");
        }

        @Override
        public void error(UsbPermissionHandler.UsbPermissionListener.ErrorType errorType, final Identity identity) {
            Log.d(TAG,"Error when asking for permission for FLIR ONE, error:" + errorType + " identity:" + identity);
        }
    };


    private void doConnect(Identity identity) {
        new Thread(() -> {
            try {
                cameraHandler.connect(identity, connectionStatusListener);
                runOnUiThread(() -> {
                    updateConnectionText(identity, "CONNECTED");
                });
                cameraHandler.startStream(streamDataListener);
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Could not connect: " + e);
                    updateConnectionText(identity, "DISCONNECTED");
                });
            }
        }).start();
    }

    private void disconnect() {
        updateConnectionText(connectedIdentity, "DISCONNECTING");
        connectedIdentity = null;
        Log.d(TAG, "disconnect() called with: connectedIdentity = [" + connectedIdentity + "]");
        new Thread(() -> {

            cameraHandler.disconnect();
            runOnUiThread(() -> {
                updateConnectionText(null, "DISCONNECTED");
            });
        }).start();
    }

    private void updateConnectionText(Identity identity, String status) {
        String deviceId = identity != null ? identity.deviceId : "";
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
//        connectionStatus.setText(getString(R.string.connection_status_text, deviceId + " " + status));
    }

    private void startDiscovery() {
        cameraHandler.startDiscovery(cameraDiscoveryListener, discoveryStatusListener);
    }

    private void stopDiscovery() {
        cameraHandler.stopDiscovery(discoveryStatusListener);
    }


    private CameraHandler.DiscoveryStatus discoveryStatusListener = new CameraHandler.DiscoveryStatus() {
        @Override
        public void started() {
            Log.d(TAG, "started: discovering");
        }

        @Override
        public void stopped() {
            Log.d(TAG, "stopped: not dscovering");
        }
    };

    private ConnectionStatusListener connectionStatusListener = new ConnectionStatusListener() {
        @Override
        public void onDisconnected(@org.jetbrains.annotations.Nullable ErrorCode errorCode) {
            Log.d(TAG, "onDisconnected errorCode:" + errorCode);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateConnectionText(connectedIdentity, "DISCONNECTED");
                }
            });
        }
    };




    private final CameraHandler.StreamDataListener streamDataListener = new CameraHandler.StreamDataListener() {


        @Override
        public void images(FrameDataHolder dataHolder) {

        }

        @Override
        public void images(ThermalImage thermalImage, Bitmap msxBitmap, Bitmap dcBitmap) {
            try {
                framesBuffer.put(new FrameDataHolder(thermalImage, msxBitmap, dcBitmap));
            } catch (InterruptedException e) {
                //if interrupted while waiting for adding a new item in the queue
                Log.e(TAG, "images(), unable to add incoming images to frames buffer, exception:" + e);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Log.d(TAG, "framebuffer size:" + framesBuffer.size());
                    if(pause){
                        return;
                    }else {
                        poll = framesBuffer.poll();
                        thermal.setImageBitmap(poll.msxBitmap);
                        digital.setImageBitmap(poll.dcBitmap);
                    }

                }
            });
        }

        @Override
        public void images(Bitmap msxBitmap, Bitmap dcBitmap) {

        }


    };

    private DiscoveryEventListener cameraDiscoveryListener = new DiscoveryEventListener() {
        @Override
        public void onCameraFound(Identity identity) {
            Log.d(TAG, "onCameraFound identity:" + identity);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cameraHandler.add(identity);
                    connect(cameraHandler.getFlirOneEmulator());
                }
            });
        }

        @Override
        public void onDiscoveryError(CommunicationInterface communicationInterface, ErrorCode errorCode) {
            Log.d(TAG, "onDiscoveryError communicationInterface:" + communicationInterface + " errorCode:" + errorCode);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopDiscovery();
                    Toast.makeText(getApplicationContext(), "onDiscoveryError communicationInterface:" + communicationInterface + " errorCode:" + errorCode, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
        return;


        // other 'case' lines to check for other
        // permissions this app might request
    }


    private void setInfo() {
        thermal = findViewById(R.id.image_thermal);
        digital = findViewById(R.id.image_digital);
        snapShot = findViewById(R.id.takeShot);
        cancelView = findViewById(R.id.cancelView);
        saveImage = findViewById(R.id.save_Image);
        cancel = findViewById(R.id.continue_route);
        snapedstate();

        snapShot.setOnClickListener(view -> {
            previewState();
            pause = true;
            disconnect();
            stopDiscovery();

        });

        saveImage.setOnClickListener(view->{
            Asset asset = getIntent().getParcelableExtra(Contracts.DETAILS_BUNDLE);
            //include asset for submission
            saveImages(asset);
        });

        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snapedstate();
                startDiscovery();
//                connect(cameraHandler.getFlirOneEmulator());
                pause = false;


            }
        });


    }

    private void saveImages(Asset asset){
        Log.d(TAG, "Submitting... + " + asset.imageUrl);

        if(asset.imageUrl.equals(Contracts.FEEDER_ASSET_NEW)) {
            Log.d(TAG, "saveImages: " + asset.asset);
            submit(poll.msxBitmap, asset);
        }else if(asset.imageUrl.equals(Contracts.FEEDER_ASSET_LIST)){
            Log.d(TAG, "saveImages: this ahould update");
            upDate(poll.msxBitmap, asset);
        }
    }

    private void snapedstate(){
        snapShot.setVisibility(View.VISIBLE);
        cancelView.setVisibility(View.VISIBLE);
        saveImage.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);

    }

    private void previewState(){
        snapShot.setVisibility(View.GONE);
        cancelView.setVisibility(View.GONE);
        saveImage.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disconnect();
        stopDiscovery();
    }

    public void submit(Bitmap bmp, Asset asset) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        String dataRef = ref.child("Asset").push().getKey();
        asset.uid = dataRef;
        String imageref = ref.child("Asset").child(asset.feeder).child(dataRef).push().getKey();
        Log.d(TAG, "saveImage: " + dataRef + " " + imageref);
        Map<String, Object> assetUpdates = new HashMap<>();
//        String saveRef = ref.child("Assets").child(asset.feeder).child(asset.asset).child(asset.imageUrl).push().getKey();
        DatabaseReference mref = ref.child("Assets").child(asset.feeder);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + dataRef, asset );
        assetUpdates.put("/images/"+asset.feeder + "/" + dataRef + "/" + imageref, imageref);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference("images/" + asset.feeder + "/" + imageref);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = storageReference.putBytes(data);

        uploadTask.addOnCompleteListener(task ->{
        ref.updateChildren(assetUpdates).addOnSuccessListener(v-> Log.d(TAG, "onSuccess: logged picture id to database"));
//            uploadResult.postValue("onComplete: successfully added picture to firebase storage");
        mref.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: uploaded");
                goBackToMaps(dataRef);
            }
        });
        });

    }

    private void upDate(Bitmap bmp, Asset asset){
        Log.d(TAG, "upDate: begin update" );
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG, "upDate: " + asset.uid);
        DatabaseReference mref = ref.child("Assets").child(asset.uid);

        String dataRef = asset.uid;
        String imageref = ref.child("Asset").child(dataRef).push().getKey();
        Map<String, Object> assetUpdates = new HashMap<>();

        assetUpdates.put("/images/" + dataRef + "/" + imageref, imageref);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference("images/" + asset.feeder + "/" + imageref);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = storageReference.putBytes(data);


        uploadTask.addOnCompleteListener(task ->{
            ref.updateChildren(assetUpdates).addOnSuccessListener(v-> Log.d(TAG, "onSuccess: logged picture id to database"));
        });

        mref.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                currentData.setValue(asset);
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
               goBackToMaps(dataRef);
            }
        });
    }

    private void goBackToMaps(String data){
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra(Contracts.MARKER_BUNDLE, data);
        startActivity(i);
    }
}