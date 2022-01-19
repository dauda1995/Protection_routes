package com.example.ptapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.util.Function;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ptapp.Joints.Joints;
import com.example.ptapp.Route.Route;
import com.example.ptapp.asset.Asset;
import com.example.ptapp.utils.Contracts;
import com.example.ptapp.viewmodels.MapActivityViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.prefs.PreferencesManager;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
         , com.google.android.gms.location.LocationListener, NavigationView.OnNavigationItemSelectedListener,  LayerDialog.OnLayerInteractionListener, JointFragment.OnJointInteractionListener, MaterialIntroListener {

    public static final String ADD_NEW_JOINT = "Add new Joint";
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private SupportMapFragment mapFragment;
    private Button stop, start;
    private MapActivityViewModel mapActivityViewModel;
    FloatingActionButton updateMap, addcv, thermalCam;
    private String feeder;


    //only when key is set
    boolean stRoute = false;

    int routeKey = -1;
    ArrayList<Route> arr = new ArrayList<>();


    private static final String TAG = "MapsActivity";
    private MutableLiveData<Location> mutableLiveData = new MutableLiveData<>();
    private Route route1 = new Route();
    private LatLng latLng;

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private FloatingActionButton add;
    private Route route;
    private Button save;
    private Button continue_route;
    private FloatingActionButton see_route;
    ProgressDialog progress;
    private DrawerLayout drawer;
    private Boolean once;


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.start_mapping:
                Log.d(TAG, "onOptionsItemSelected: start mapping");
                startFeederSelect(Contracts.FEEDER_MAPPING);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.new_asset:
                Log.d(TAG, "onOptionsItemSelected: new asset");
                startFeederSelect(Contracts.JOINTS);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;

            case R.id.asset_list:
                Log.d(TAG, "onOptionsItemSelected: asset list");
                startFeederSelect(Contracts.FEEDER_ASSET_LIST);
                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            case R.id.thermal_stream:

                Log.d(TAG, "onOptionsItemSelected: thermal stream");
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.settings:
                startFeederSelect(Contracts.FEEDER_MAP_PREV);
                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
        }


        return true;
    }

    @Override
    public void onLayerInteraction(String resID, String TAGE) {
        switch (TAGE) {
            case Contracts.FEEDER_MAPPING:
                startUI();
                feeder = resID;

                break;
            case Contracts.FEEDER_ASSET_LIST:
                feeder = resID;
                startAssetRecyc();
                break;
            case Contracts.FEEDER_ASSET_NEW:
                feeder = resID;
                startNewAsset(feeder);


                break;
            case Contracts.FEEDER_MAP_PREV:
                retrivePreviousRoutes();
                break;
            case Contracts.SELECT_ROUTE:
                feeder = resID;
                try {
                    mMap.clear();
                    populateRoute();
                    populateMap(feeder);
                    retrieveJoints(feeder);
                } catch (Exception e) {
                    Log.d(TAG, "onCreate: " + e.getMessage());
                }
                break;
            case Contracts.JOINTS:
                feeder = resID;
                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                JointFragment jointFragment = JointFragment.newInstance(feeder, latLng);
                jointFragment.show(getSupportFragmentManager(), ADD_NEW_JOINT);
                break;
            default:

                break;
        }
    }

    public void startFeederSelect(String tag) {
        String[] arraylist = getResources().getStringArray(R.array.feeder);
        LayerDialog layerDialog = new LayerDialog(arraylist, tag);
        layerDialog.show(getSupportFragmentManager(), "Feeder Select");
    }

    @Override
    public void onJointInteraction(Joints joints) {
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference mref = databaseReference.child(Contracts.JOINTS).child(joints.feeder).child(joints.Imageid);
//        mref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Joints joint = snapshot.getValue(Joints.class);
//                showMarker(joint);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//                DatabaseReference mref = databaseReference.child(Contracts.JOINTS).child(joints.feeder).child(joints.Imageid);
//            }
//        });
    }

    public void retrieveJoints(String feeder){

        mapActivityViewModel.getJointObjs().observe(this, v->{
            if(v == null){
                return;
            }
            for(Joints joint : v){
                showMarker(joint);
            }

        });
        ArrayList<Joints> joints = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mref = databaseReference.child(Contracts.JOINTS).child(feeder);
        mref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                  Joints joint = dataSnapshot.getValue(Joints.class);
                  joints.add(joint);
              }
              mapActivityViewModel.setJointObjs(joints);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public interface ShowMessage {
        void show(String message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        once = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        once = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        once = true;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.




        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }

        mapActivityViewModel = new ViewModelProvider(this).get(MapActivityViewModel.class);
        init();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void init() {
//        addcv = findViewById(R.id.addcv);
        stop = findViewById(R.id.stop);
        start = findViewById(R.id.start);
        add = findViewById(R.id.add_marker);
        save = findViewById(R.id.save);
        continue_route = findViewById(R.id.continue_route);
        see_route = findViewById(R.id.see_map);
        stopUI();
//        Toast.makeText(getApplicationContext(), "Use the Navigation drawer to Navigate through feature options \n click on floating action button to add new asset to map \n Use the Navigation drawer to Navigate through feature options \n Sorry, all features havent been implemented yet, such as live streaming \n ", Toast.LENGTH_SHORT).show();

        findViewById(R.id.ques).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTutorial();
            }
        });
        continue_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastLocation != null) {
                    if (routeKey != -1) {
                        stRoute = true;
                        Log.d(TAG, "onClick: continue with routin");
                        startUI();
                    }
                }

            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressShow("saving", "wait");
                List<Route> list = mapActivityViewModel.getRoutes();
                if (list.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "No data uploaded", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    updateDatabase(list);
                    stopUI();


                }
            }
        });


        see_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFeederSelect(Contracts.SELECT_ROUTE);


            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFeederSelect(Contracts.FEEDER_ASSET_NEW);

            }
        });

//        thermalCam.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Fragment fragment = new ThermalCameraFragment();
//                FragmentTransaction transaction = getSupportFragmentManager()
//                        .beginTransaction();
//                transaction.replace(R.id.map_container, fragment);
//                transaction.addToBackStack(null);
//                transaction.commit();
//            }
//        });

        mapActivityViewModel.getPreviousFireRoutes().observe(this, route -> {
            if (route == null) {
                return;
            }
            Log.d(TAG, "init: " + route.size());
            updateRoute(route);
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mLastLocation != null) {
                    progressShow("Routing", "Click anywhere");
                    getRouteKey();
//                    dummy();
//                    Log.d(TAG, "onClick: clicked");
                }

            }
        });

        mapActivityViewModel.getAssetMarkers().observe(this, v -> {
            Log.d(TAG, "init: getting markers");
            for (Asset asset : v) {
                showMarker(asset);
            }
            Log.d(TAG, "init: okay, updated");

        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressShow("Stopping", "Click anywhere");
                stRoute = false;
                List<Route> routes = mapActivityViewModel.getRoutes();
                List<Route> preRoutes = mapActivityViewModel.getPrevRoutes();

                List<LatLng> l = new ArrayList<>();
                for (Route r : routes) {
                    l.add(r.latLng);

//                    mMap.addMarker(new MarkerOptions()
//                    .position(r.latLng)
//                    .title(String.valueOf(r.id))
//                    );
                }
                mMap.addPolyline(new PolylineOptions()
                        .addAll(l)
                        .color(Color.RED)
                );

                List<LatLng> lp = new ArrayList<>();
                for (Route rp : preRoutes) {
                    lp.add(rp.latLng);

//                    mMap.addMarker(new MarkerOptions()
//                            .position(rp.latLng)
//                            .title(String.valueOf(rp.id))
//                    );
                }
                mMap.addPolyline(new PolylineOptions()
                        .addAll(l)
                        .color(Color.GREEN));
                start.setVisibility(View.VISIBLE);
                saveUI();
                progressDismiss();
            }

        });


//        updateMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MapsActivity.this, DetailsActivity.class);
//                intent.putExtra(Contracts.LOCATION_BUNDLE, latLng);
//                startActivity(intent);
//            }
//        });


    }

    public void startUI() {

        stop.setVisibility(View.VISIBLE);
        start.setVisibility(View.VISIBLE);
        add.setVisibility(View.INVISIBLE);
        continue_route.setVisibility(View.GONE);
        save.setVisibility(View.GONE);
        drawerLayout.setEnabled(false);
    }

    public void stopUI() {

        stop.setVisibility(View.GONE);
        start.setVisibility(View.GONE);
        add.setVisibility(View.VISIBLE);
        save.setVisibility(View.GONE);
        continue_route.setVisibility(View.GONE);
        drawerLayout.setEnabled(true);
    }

    public void saveUI() {
        stop.setVisibility(View.GONE);
        start.setVisibility(View.GONE);
        add.setVisibility(View.GONE);
        save.setVisibility(View.VISIBLE);
        continue_route.setVisibility(View.VISIBLE);
        drawerLayout.setEnabled(false);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();

//        mMap.setMyLocationEnabled(true);
//
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {
            mLastLocation = location;
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            Log.d(TAG, "onLocationChanged: " + mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
            Log.d(TAG, "onLocationChanged: " + route1.feeder);
            if (stRoute) {
                Log.d(TAG, "onLocationChanged: this is the captured loc: ");
                startRouting(location);
                moveToPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }


            if (once) {
                moveToPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                once = false;
            }

        }
    }

    public void moveToPosition(LatLng location) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
    }

    final int LOCATION_REQUEST_CODE = 1;

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mapFragment.getMapAsync(this);


                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }


    }

    public void getRouteKey() {
        Log.d(TAG, "getRouteKey: ");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Routes").child(feeder);
        final DatabaseReference mref = FirebaseDatabase.getInstance().getReference().child("Routes").child(feeder).child("cordinates");

        mref.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long key = snapshot.getChildrenCount();
                if (snapshot.hasChildren()) {
                    Log.d(TAG, "onDataChange: there are keys available");
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.getKey());
                        if (dataSnapshot.getKey().equals("a")) {
                            routeKey = 0;
                            stRoute = true;
                            progress.dismiss();
                            return;
                        }
                        routeKey = Integer.parseInt(dataSnapshot.getKey());
                        //confirms that the previous key has been set
                        stRoute = true;
                        progress.dismiss();

                    }
                } else {
                    Log.d(TAG, "onDataChange: there are no keys available");
                    routeKey = 0;
                    stRoute = true;
                    progress.dismiss();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void startRouting(Location location) {
        Toast.makeText(getApplicationContext(), "location : " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        start.setVisibility(View.INVISIBLE);
        if (routeKey != -1 && stRoute) {
            if (routeKey >= 1) {
                routeKey += 1;
            }
            if (routeKey == 0) {
                routeKey += 1;
            }

            Route route = new Route(routeKey, location.getLatitude(), location.getLongitude());
            Log.d(TAG, "startRouting: " + routeKey + " " + route.id + " " + route.latLng.latitude);
            mapActivityViewModel.setSavedRoutes(route);


//            DatabaseReference mref = FirebaseDatabase.getInstance().getReference().child("Routes").child("ademola").child("coords");
//            mref.child(String.valueOf(routeKey)).setValue(new LatLng(location.getLatitude(), location.getLongitude())).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    Log.d(TAG, "onComplete: completed");
//                }
//            });
        }
    }


    public void retrivePreviousRoutes() {
        final DatabaseReference mret = FirebaseDatabase.getInstance().getReference().child("Routes").child(feeder).child("cordinates");
        final DatabaseReference mret1 = FirebaseDatabase.getInstance().getReference().child("Routes").child(feeder);

        List<Route> cords = new ArrayList<>();
        mret.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    int uid = Integer.parseInt(snapshot.getKey());
                    Log.d(TAG, "onDataChange: uid for previous " + uid);
                    LatLng latLng = new LatLng(snapshot.child("latitude").getValue(Double.class), snapshot.child("longitude").getValue(Double.class));
                    cords.add(new Route(uid, snapshot.child("latitude").getValue(Double.class), snapshot.child("longitude").getValue(Double.class)));


                }

                Log.d(TAG, "onDataChange: " + cords.size());
//                route1.cordinates = cords;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mret1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String feeder = snapshot.child("feeder").getValue(String.class);
                int uid = snapshot.child("id").getValue(Integer.class);
                route1.feeder = feeder;
                route1.id = uid;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void dummy() {

        List<Route> coords = mapActivityViewModel.getRoutes();

        Map<String, Object> coordUpdates = new HashMap<>();

        for (Route route : coords) {
            coordUpdates.put("/" + route.id, route);
        }

        final DatabaseReference mref = FirebaseDatabase.getInstance().getReference().child("Routes").child(feeder);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + feeder, coords);

//        mref.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Log.d(TAG, "onSuccess: yes!");
//            }
//        });
//        Log.d(TAG, "dummy: ");


    }

    private class routeDeserializer implements Function<DataSnapshot, List<Route>> {

        @Override
        public List<Route> apply(DataSnapshot input) {
            for (DataSnapshot snapshot : input.getChildren()) {
                Log.d(TAG, "apply: " + snapshot);
//                   Route route = snapshot.getValue(Route.class);
//                Log.d(TAG, "onDataChange: " + snapshot.child("feeder").getValue().toString());
//               arr.add(route);

            }
            return arr;
        }


    }

    public void updateDatabase(List<Route> routes) {
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mRoutes = mref.child("Routes").child(feeder).child("cordinates");
        Map<String, Object> coordsUpdates = new HashMap<>();
        for (Route route : routes) {
            coordsUpdates.put(String.valueOf(route.id), route);
            Log.d(TAG, "updateDatabase: " + route.id);
        }
        mRoutes.updateChildren(coordsUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: coordinates have been uploaded");
                Toast.makeText(getApplicationContext(), "Coordinates have been uploaded", Toast.LENGTH_LONG).show();
                progressDismiss();
                mapActivityViewModel.clearRoute();
            }

        });
    }

    public void populateMap(String feeder) {
        progressShow("fetching", "please wait...");
        Log.d(TAG, "populateMap: about to populate");
        List<Asset> assetList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Assets").child(feeder);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Asset asset = dataSnapshot.getValue(Asset.class);
                    assetList.add(asset);
                    Log.d(TAG, "onDataChange: " + asset.uid);

                }
                mapActivityViewModel.setAssetMarkers(assetList);
                progressDismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void populateRoute() {
        Log.d(TAG, "populateMap: about to populate");
        List<Route> routes = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Joints").child(feeder).child("cordinates");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: " + dataSnapshot.getKey());

//                    Log.d(TAG, "onDataChange: " + dataSnapshot.child("latLng").child("latitude").getValue());
                    int id = dataSnapshot.child("id").getValue(int.class);
                    LatLng latLng = new LatLng(dataSnapshot.child("latLng").child("latitude").getValue(Double.class), dataSnapshot.child("latLng").child("longitude").getValue(Double.class));
                    Log.d(TAG, "onDataChange: " + latLng.latitude + " " + latLng.longitude);

                    Route route = new Route(id, latLng.latitude, latLng.longitude);
                    routes.add(route);


                }
                Log.d(TAG, "onDataChange: total number " + routes.size());
                mapActivityViewModel.setPreviousFireRoutes(routes);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void populateRoutePre() {
        Log.d(TAG, "populateMap: about to populate");
        List<Asset> assetList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Routes").child(feeder).child("cordinates");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Asset asset = dataSnapshot.getValue(Asset.class);
                    assetList.add(asset);
                    Log.d(TAG, "onDataChange: " + asset.uid);

                }
                mapActivityViewModel.setAssetMarkers(assetList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void showMarker(Object object) {
        if (object.getClass().equals(Asset.class)) {
            Asset asset = (Asset) object;
            if (asset.latitude == null && asset.asset == null) {
                return;
            }
            Log.d(TAG, "showMarker: showing markers");
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(asset.latitude, asset.longitude))
                    .snippet(asset.feeder)
                    .title(asset.asset));

        } else if (object.getClass().equals(Joints.class)) {
            Joints joint = (Joints) object;
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(joint.latitude, joint.longitude))
                    .title(joint.location)
                    .snippet(joint.type)
                    .snippet(joint.feeder)

            );

        }
    }

    public void startAssetRecyc() {
        Intent i = new Intent(this, DashBoardActivity.class);
        i.putExtra(Contracts.DASHBOARD_BUNDLE, feeder);
        startActivity(i);
    }

    public void startNewAsset(String feeder) {

        Asset asset = new Asset();
        asset.imageUrl = Contracts.FEEDER_ASSET_NEW;
        asset.latitude = latLng.latitude;
        asset.longitude = latLng.longitude;
        asset.feeder = feeder;
        Intent intent = new Intent(MapsActivity.this, DetailsActivity.class);
        intent.putExtra(Contracts.LOCATION_BUNDLE, asset);
        startActivity(intent);
    }

    public void updateRoute(List<Route> routes) {
        List<LatLng> l = new ArrayList<>();
        if (routes.size() == 0) {
            Toast.makeText(getApplicationContext(), "No routes available", Toast.LENGTH_SHORT).show();
            return;
        }
        for (Route r : routes) {
            l.add(r.latLng);
            Log.d(TAG, "updateRoute: " + r.id);

//                    mMap.addMarker(new MarkerOptions()
//                    .position(r.latLng)
//                    .title(String.valueOf(r.id))
//                    );
        }
        mMap.addPolyline(new PolylineOptions()
                .addAll(l)
                .color(Color.RED));
        moveToPosition(l.get(0));
        l.clear();
//        mapActivityViewModel.setPreviousFireRoutes(null);
    }

    private void progressShow(String title, String message) {
        progress = ProgressDialog.show(MapsActivity.this,
                title,
                message);
        progress.setCancelable(true);
    }

    private void progressDismiss() {
        progress.dismiss();
    }

    @Override
    public void onUserClicked(String materialIntroViewId) {
        if(materialIntroViewId == Contracts.INTRO_NEW_ASSET){
            showIntro(see_route, Contracts.INTRO_TOGGLE_NAV, getString(R.string.actionnav), Focus.NORMAL, false);
        }else if(materialIntroViewId == Contracts.INTRO_TOGGLE_NAV){
            drawerLayout.openDrawer(Gravity.LEFT);
            showIntro(drawerLayout, Contracts.INTRO_START, getString(R.string.actionstart), Focus.NORMAL, false);
        }else if(materialIntroViewId == Contracts.INTRO_START){
            drawerLayout.closeDrawer(Gravity.LEFT);
            showIntro(see_route, Contracts.INTRO_STOP, getString(R.string.actionstop), Focus.MINIMUM, false);
        }


    }

    public void showIntro(View view, String id, String text, Focus focusType, Boolean perform) {
        new MaterialIntroView.Builder(this)
                .enableDotAnimation(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(focusType)
                .setDelayMillis(200)
                .enableFadeAnimation(true)
                .setListener(this)
                .performClick(perform)
                .setInfoText(text)
                .setTarget(view)
                .setUsageId(id) //THIS SHOULD BE UNIQUE ID
                .show();
    }

    public void startTutorial() {
        new PreferencesManager(getApplicationContext()).resetAll();
        showIntro(add, Contracts.INTRO_NEW_ASSET, getString(R.string.new_asset), Focus.MINIMUM, false);
    }


}