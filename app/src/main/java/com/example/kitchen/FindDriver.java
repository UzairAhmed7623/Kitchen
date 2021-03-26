package com.example.kitchen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kitchen.Callback.IFirebaseDriverInfoListener;
import com.example.kitchen.Callback.IFirebaseFailedListener;
import com.example.kitchen.Common.Common;
import com.example.kitchen.Utils.UserUtils;
import com.example.kitchen.fragments.BottomSheetRiderFragment;
import com.example.kitchen.modelclasses.DriverGeoModel;
import com.example.kitchen.modelclasses.DriverInfoModel;
import com.example.kitchen.modelclasses.GeoQueryModel;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class FindDriver extends FragmentActivity implements OnMapReadyCallback, IFirebaseFailedListener, IFirebaseDriverInfoListener {

    private CircleImageView driverImage;
    private TextView driverName, driverPhone;
    private LinearLayout layout;
    private GoogleMap mgoogleMap;
    private FirebaseFirestore firebaseFirestore;

    private LatLng latLng;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DocumentReference documentReference;
    private CollectionReference ref;
    //    private GeoFire geoFire;
    private String keyFound = "";
    String id = "P5397d1k8cYDoW8dtEIOQClO8OI2";
    private Marker marker, driMarker;

    private ImageView ivArrow;
    private Button btnPickup;
    private BottomSheetRiderFragment riderFragment;
    private Location location;
    private Marker userMarker;
    private boolean isDriverFound = false;
    private String driverId = "";
    private int radius = 1; //Km
    private static final int LIMIT = 3;
    private Marker driverMarker;

    //new wale
    private SupportMapFragment mapFragment;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double distance = 1.0; //in km
    private static final double LIMIT_RANGE = 10.0;  //km
    private Location previousLocation, currentLocation; //use to calculate distance
    private boolean firstTime = true;

    IFirebaseDriverInfoListener iFirebaseDriverInfoListener;
    IFirebaseFailedListener iFirebaseFailedListener;
    private String cityName;
    DriverGeoModel driverGeoModel = new DriverGeoModel();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_driver);

        firebaseFirestore = FirebaseFirestore.getInstance();

        //        driverImage = (CircleImageView) findViewById(R.id.driverImage);
//        driverName = (TextView) findViewById(R.id.driverName);
//        driverPhone = (TextView) findViewById(R.id.driverPhone);
        layout = (LinearLayout) findViewById(R.id.layout);

//        ivArrow = (ImageView) findViewById(R.id.ivArrow);
//        btnPickup = (Button) findViewById(R.id.btnPickup);


        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(FindDriver.this);


        riderFragment = BottomSheetRiderFragment.newInstance("Rider bottom sheet");

//        ivArrow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                riderFragment.show(getSupportFragmentManager(), riderFragment.getTag());
//            }
//        });


        init();

        updateFirebaseToken();

    }

    private void updateFirebaseToken() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(FindDriver.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();

                        UserUtils.updateToken(FindDriver.this, token);

                    }
                });

    }

    private void init() {

        iFirebaseDriverInfoListener = this;
        iFirebaseFailedListener = this;

        locationRequest = LocationRequest.create();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(9000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                mgoogleMap.moveCamera(CameraUpdateFactory.newLatLng(newPosition));

                if (firstTime) {
                    previousLocation = currentLocation = locationResult.getLastLocation();
                    firstTime = false;
                } else {
                    previousLocation = currentLocation;
                    currentLocation = locationResult.getLastLocation();
                }

                if (previousLocation.distanceTo(currentLocation) / 1000 <= LIMIT_RANGE) {
                    loadAllAvailableDrivers();
                } else {
                    //DoNothing
                }
//                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
//                        new GeoLocation(locationResult.getLastLocation().getLatitude(),
//                                locationResult.getLastLocation().getLongitude()),
//                        new GeoFire.CompletionListener() {
//                            @Override
//                            public void onComplete(String key, DatabaseError error) {
//                                if (error != null) {
//                                    Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
//                                } else {
//                                    Snackbar.make(mapFragment.getView(), "You're online!", Snackbar.LENGTH_LONG).show();
//                                }
//                            }
//                        });

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        loadAllAvailableDrivers();
    }

    private void loadAllAvailableDrivers() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mgoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f));

                Geocoder geocoder = new Geocoder(FindDriver.this, Locale.getDefault());
                List<Address> addressList;
                try {
                    addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    cityName = addressList.get(0).getLocality();

                    DatabaseReference driver_location_ref = FirebaseDatabase.getInstance()
                            .getReference("driversLocation").
                            child(cityName);

                    GeoFire geoFire = new GeoFire(driver_location_ref);
                    GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), distance);
                    geoQuery.removeAllListeners();

                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                            Common.driverFound.add(new DriverGeoModel(key, location));
                            keyFound = key;
//                            Toast.makeText(FindDriver.this, "geoQuery"+keyFound, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onKeyExited(String key) {

                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {
                            if (distance <= LIMIT_RANGE){
                                distance++;
                                loadAllAvailableDrivers();
                            }
                            else {
                                distance = 1.0;
                                addDriverMarker();
                            }
                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {
                            Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();

                        }
                    });

                    driver_location_ref.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
                            GeoLocation geoLocation = new GeoLocation(geoQueryModel.getL().get(0),
                                    geoQueryModel.getL().get(1));
                            DriverGeoModel driverGeoModel = new DriverGeoModel(snapshot.getKey(), geoLocation);
                            Location newDriverLocation = new Location("");
                            newDriverLocation.setLatitude(geoLocation.latitude);
                            newDriverLocation.setLongitude(geoLocation.longitude);
                            float newDistance = location.distanceTo(newDriverLocation) / 1000;
                            if (newDistance <= LIMIT_RANGE)
                                findDriverByKey(driverGeoModel);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                catch (IOException e){
                    e.printStackTrace();
                    Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();

            }
        });

    }

    private void addDriverMarker() {
        if (Common.driverFound.size() > 0){

            Observable.fromIterable(Common.driverFound)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(driverGeoModel ->  {
                        findDriverByKey(driverGeoModel);
                    },throwable -> {},()->{});


        }
        else {
            Snackbar.make(mapFragment.getView(), "Driver not found!", Snackbar.LENGTH_LONG).show();
        }
    }

    private void findDriverByKey(DriverGeoModel driverGeoModel) {
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(driverGeoModel.getKey());
//        Toast.makeText(FindDriver.this, driverGeoModel.getKey(), Toast.LENGTH_SHORT).show();

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){

                    driverGeoModel.setDriverInfoModel(documentSnapshot.toObject(DriverInfoModel.class));
                    iFirebaseDriverInfoListener.onDriverInfoLoadSuccess(driverGeoModel);

                }
                else {
                    iFirebaseFailedListener.onFirebaseLoadFailed(getString(R.string.not_found_key)+driverGeoModel.getKey());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iFirebaseFailedListener.onFirebaseLoadFailed(e.getMessage());

            }
        });

    }

    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mgoogleMap = googleMap;


        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                if (ActivityCompat.checkSelfPermission(FindDriver.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(FindDriver.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mgoogleMap.setMyLocationEnabled(true);
                mgoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                mgoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {

                        if (ActivityCompat.checkSelfPermission(FindDriver.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(FindDriver.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return false;
                        }
                        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                mgoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f));
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(FindDriver.this, "dosra wala" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                        return true;
                    }
                });

                View locationbutton = ((View)mapFragment.getView().findViewById(Integer.parseInt("1")).getParent())
                        .findViewById(Integer.parseInt("2"));
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationbutton.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                params.setMargins(0,0,0,50);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Toast.makeText(FindDriver.this, "Permission "+permissionDeniedResponse.getPermissionName()+" "+
                        "was denied!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();



    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Snackbar.make(mapFragment.getView(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDriverInfoLoadSuccess(DriverGeoModel driverGeoModel) {
        if (!Common.markerList.containsKey(driverGeoModel.getKey())){
            Common.markerList.put(driverGeoModel.getKey(),
                    mgoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(driverGeoModel.getGeoLocation().latitude,
                            driverGeoModel.getGeoLocation().longitude))
                    .flat(true)
                    .title(Common.buildName(driverGeoModel.getDriverInfoModel().getFirstName(),
                            driverGeoModel.getDriverInfoModel().getLastName()))
                    .snippet(driverGeoModel.getDriverInfoModel().getPhoneNumber())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))));

//            Toast.makeText(this, "onDriverInfoLoadSuccess"+driverGeoModel.getKey(), Toast.LENGTH_SHORT).show();

            if (!TextUtils.isEmpty(cityName)){
                DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference("driversLocation")
                        .child(cityName)
                        .child(driverGeoModel.getKey());
                driverLocation.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.hasChildren()){
                            if (Common.markerList.get(driverGeoModel.getKey()) != null){
                                Common.markerList.get(driverGeoModel.getKey()).remove();
                            }
                            Common.markerList.remove(driverGeoModel.getKey());
                            driverLocation.removeEventListener(this);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }

        }
    }

//    private void getDriverData(String key, GeoLocation location) {
//
//        ref.document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
//
//                if (documentSnapshot.exists()){
//                    String requestID = documentSnapshot.getString("requestID");
//
//                    if (requestID != null){
//
//                        String fName = documentSnapshot.getString("First Name");
//                        String lName = documentSnapshot.getString("Last Name");
//                        String phone = documentSnapshot.getString("Phone");
//                        String imageUri = documentSnapshot.getString("imageProfile");
//
//                        driverName.setText(fName + " " + lName);
//                        driverPhone.setText(phone);
//                        Glide.with(getApplicationContext()).load(imageUri).placeholder(R.drawable.driver_placeholder).fitCenter().into(driverImage);
//
//                        LatLng latLng1 = new LatLng(location.latitude,location.longitude);
//
//                        if (driMarker != null){
//                            driMarker.remove();
//                        }
//                        driMarker = mMap.addMarker(new MarkerOptions().position(latLng1).title(fName + " " + lName));
////                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng1));
//                        LatLngBounds.Builder latlngBuilder = new LatLngBounds.Builder();
//
//                        latlngBuilder.include(latLng);
//                        latlngBuilder.include(latLng1);
//
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBuilder.build(), 100));
//
//                        GoogleDirection.withServerKey("AIzaSyDl7YXtTZQNBkthV3PjFS0fQOKvL8SIR7k")
//                                .from(latLng)
//                                .to(latLng1)
//                                .execute(new DirectionCallback() {
//                                    @Override
//                                    public void onDirectionSuccess(@Nullable Direction direction) {
//
//                                        List<Step> stepList = direction.getRouteList().get(0).getLegList().get(0).getStepList();
//                                        ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(FindDriver.this, stepList, 5, Color.RED, 3, Color.BLUE);
//                                        for (PolylineOptions polylineOption : polylineOptionList) {
//                                            mMap.addPolyline(polylineOption);
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onDirectionFailure(Throwable t) {
//                                        Log.d("TAG222", ""+t.getMessage());
//
//                                    }
//                                });
//
//                        layout.setVisibility(View.VISIBLE);
//
//                    }
//                    else {
//                        Toast.makeText(FindDriver.this, "Driver canceled!", Toast.LENGTH_SHORT).show();
////                        geoFire.removeLocation(id);
//                        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
//
//                    }
//
//                }
//            }
//        });
//    }


}