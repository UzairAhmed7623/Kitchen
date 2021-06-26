package com.inkhornsolutions.kitchen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.bumptech.glide.Glide;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.android.ui.IconGenerator;
import com.inkhornsolutions.kitchen.Common.Common;
import com.inkhornsolutions.kitchen.EventBus.DriverAcceptTripEvent;
import com.inkhornsolutions.kitchen.EventBus.SelectPlaceEvent;
import com.inkhornsolutions.kitchen.Utils.UserUtils;
import com.inkhornsolutions.kitchen.modelclasses.AnimationModel;
import com.inkhornsolutions.kitchen.modelclasses.DriverGeoModel;
import com.inkhornsolutions.kitchen.modelclasses.DriverInfoModel;
import com.inkhornsolutions.kitchen.modelclasses.GeoQueryModel;
import com.inkhornsolutions.kitchen.modelclasses.TripPlanModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.rxjava3.core.Observable;

public class FindRider extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mgoogleMap;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private String cityName;
    private double distance = 1.0; //in km
    private static final double LIMIT_RANGE = 10.0;  //km
    private DriverGeoModel driverGeoModel;
    private static final String DIRECTION_API_KEY = "AIzaSyDl7YXtTZQNBkthV3PjFS0fQOKvL8SIR7k";
    private double orderLat, orderLng;
    private String orderId="";


    private TextView tvOrigin;
    private Marker originMarker, destinationMarker;

    //layout confirm pickup
    private TextView tvPickupAddress;
    private Button btnConfirmPickup;
    private CardView layout_confirm_Pickup;

    //layout finding your ride
    private CardView finding_your_ride_layout;

    //driver info layout
    private TextView tvDriverName;
    private CircleImageView ivDriver;
    private CardView driverInfoLayout;

    private Polyline blackPolyLine, greyPolyline;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private List<LatLng> polylineList;

    private SelectPlaceEvent selectPlaceEvent;

    private View fill_all_screen;
    //Effect
    private Circle lastUserCircle;
    private long duration = 1000;
    private ValueAnimator lastPulseAnimator;

    //Slowly camera spinning
    private ValueAnimator animator;
    private static final int DESIRED_NUMBER_OF_SPINS = 5;
    private static final int DESIRED_SECONDS_PER_ONE_360_SPIN = 40;

    private DriverGeoModel lastDriverCall;

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDriverAcceptEvent(DriverAcceptTripEvent event){
        //Get trip informations
        FirebaseDatabase.getInstance().getReference("Trips").child(event.getTripId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            TripPlanModel tripPlanModel = snapshot.getValue(TripPlanModel.class);
                            mgoogleMap.clear();
                            fill_all_screen.setVisibility(View.GONE);
                            if (animator != null){
                                animator.end();
                            }
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(mgoogleMap.getCameraPosition().target)
                                    .tilt(0f)
                                    .zoom(mgoogleMap.getCameraPosition().zoom)
                                    .build();
                            mgoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            LatLng driverLocation = new LatLng(tripPlanModel.getCurrentLat(), tripPlanModel.getCurrentLng());
                            LatLng userOrigin = new LatLng(Double.parseDouble(tripPlanModel.getOrigin().split(",")[0]),
                                    Double.parseDouble(tripPlanModel.getOrigin().split(",")[1]));

                            try {

                                GoogleDirection.withServerKey(DIRECTION_API_KEY)
                                        .from(userOrigin)
                                        .to(driverLocation)
                                        .execute(new DirectionCallback() {
                                            @Override
                                            public void onDirectionSuccess(@Nullable Direction direction) {
                                                Route route = direction.getRouteList().get(0);
                                                Leg leg = route.getLegList().get(0);

                                                PolylineOptions blackPolylineOptions = new PolylineOptions();
                                                List<LatLng> polyLineList = null;
                                                Polyline blackPolyline = null;

                                                polylineList = leg.getDirectionPoint();

                                                String duration = leg.getDuration().getText();
                                                String distance = leg.getDistance().getText();


                                                blackPolylineOptions = new PolylineOptions();
                                                blackPolylineOptions.color(Color.BLACK);
                                                blackPolylineOptions.width(5);
                                                blackPolylineOptions.startCap(new SquareCap());
                                                blackPolylineOptions.jointType(JointType.ROUND);
                                                blackPolylineOptions.addAll(polylineList);
                                                greyPolyline = mgoogleMap.addPolyline(polylineOptions);
                                                blackPolyLine = mgoogleMap.addPolyline(blackPolylineOptions);

                                                LatLng origin = new LatLng(
                                                        Double.parseDouble(tripPlanModel.getOrigin().split(",")[0]),
                                                        Double.parseDouble(tripPlanModel.getOrigin().split(",")[1]));

                                                LatLng destination = new LatLng(tripPlanModel.getCurrentLat(), tripPlanModel.getCurrentLng());

                                                LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                                        .include(origin)
                                                        .include(destination)
                                                        .build();

//                                                addPickupMarkerWithDuration(duration, origin);
//                                                addDriverMarker(destination);

                                                mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                                                mgoogleMap.moveCamera(CameraUpdateFactory.zoomTo(mgoogleMap.getCameraPosition().zoom - 1));

//                                                initDriverForMoving(event.getTripId(), tripPlanModel);

                                                Glide.with(FindRider.this).load(tripPlanModel.getDriverInfoModel().getDriverProfileImage()).into(ivDriver);
                                                tvDriverName.setText(tripPlanModel.getDriverInfoModel().getFirstName()+" "+tripPlanModel.getDriverInfoModel().getLastName());


                                                layout_confirm_Pickup.setVisibility(View.GONE);
                                                finding_your_ride_layout.setVisibility(View.GONE);
                                                driverInfoLayout.setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void onDirectionFailure(@NonNull Throwable t) {

                                            }
                                        });
                            }
                            catch (Exception e){
                                Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                        else {
                            Snackbar.make(findViewById(android.R.id.content), "Trip not found with key"+event.getTripId(), Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Snackbar.make(findViewById(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_rider);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

//        orderLat = getIntent().getDoubleExtra("lat", 1);
//        orderLng = getIntent().getDoubleExtra("lng", 2);
//        orderId = getIntent().getStringExtra("orderId");
        orderLat = 31.530351;
        orderLng = 74.4912343;
        orderId = "12345678";

        tvPickupAddress = (TextView) findViewById(R.id.tvPickupAddress);
        btnConfirmPickup = (Button) findViewById(R.id.btnConfirmPickup);
        layout_confirm_Pickup = (CardView) findViewById(R.id.layout_confirm_Pickup);
        fill_all_screen = (View) findViewById(R.id.fill_all_screen);
        finding_your_ride_layout = (CardView) findViewById(R.id.finding_your_ride_layout);
        driverInfoLayout = (CardView) findViewById(R.id.driverInfoLayout);
        tvDriverName = (TextView) findViewById(R.id.tvDriverName);
        ivDriver = (CircleImageView) findViewById(R.id.ivDriver);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(FindRider.this);


        init();
    }

    private void updateFirebaseToken() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(FindRider.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();

                        UserUtils.updateToken(FindRider.this, token);
                    }
                });

    }

    private void drawPath(SelectPlaceEvent selectPlaceEvent) {

        try {
            GoogleDirection.withServerKey(DIRECTION_API_KEY)
                    .from(selectPlaceEvent.getOrigin())
                    .to(selectPlaceEvent.getDestination())
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(@Nullable Direction direction) {
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);

                            polylineList = leg.getDirectionPoint();


                            polylineOptions = new PolylineOptions();
                            polylineOptions.color(getColor(R.color.red));
                            polylineOptions.width(10);
                            polylineOptions.startCap(new SquareCap());
                            polylineOptions.jointType(JointType.ROUND);
                            polylineOptions.addAll(polylineList);
                            greyPolyline = mgoogleMap.addPolyline(polylineOptions);

                            blackPolylineOptions = new PolylineOptions();
                            blackPolylineOptions.color(getColor(R.color.myColor));
                            blackPolylineOptions.width(5);
                            blackPolylineOptions.startCap(new SquareCap());
                            blackPolylineOptions.jointType(JointType.ROUND);
                            blackPolylineOptions.addAll(polylineList);
                            greyPolyline = mgoogleMap.addPolyline(polylineOptions);
                            blackPolyLine = mgoogleMap.addPolyline(blackPolylineOptions);

                            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
                            valueAnimator.setDuration(1500);
                            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
                            valueAnimator.setInterpolator(new LinearInterpolator());
                            valueAnimator.addUpdateListener(animation -> {
                                List<LatLng> points = greyPolyline.getPoints();
                                int percentValue = (int) animation.getAnimatedValue();
                                int size = points.size();
                                int newPoints = (int) (size*(percentValue/100.0f));
                                List<LatLng> p = points.subList(0, newPoints);
                                blackPolyLine.setPoints(p);
                            });

                            valueAnimator.start();

                            LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                    .include(selectPlaceEvent.getOrigin())
                                    .include(selectPlaceEvent.getDestination())
                                    .build();

                            //Add car icon for origin

                            Info distanceInfo = leg.getDistance();
                            Info durationInfo = leg.getDuration();
                            String distance = distanceInfo.getText();
                            String duration = durationInfo.getText();
                            String startAddress = leg.getStartAddress().replace(", Punjab,","").replace("54000,","").replace("Pakistan","");
                            String endAddress = leg.getEndAddress().replace(", Punjab,","").replace("54000,","").replace("Pakistan","");

                            addRestaurantMarker(duration, startAddress);
                            addDestinationMarker(distance ,endAddress);

                            mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                            mgoogleMap.moveCamera(CameraUpdateFactory.zoomTo(mgoogleMap.getCameraPosition().zoom - 1));


                        }

                        @Override
                        public void onDirectionFailure(@NonNull Throwable t) {

                        }
                    });
        }
        catch (Exception e){
            Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    private void addRestaurantMarker(String duration, String startAddress) {
        View view = getLayoutInflater().inflate(R.layout.origin_info_window, null);

        TextView tvTime = (TextView) view.findViewById(R.id.tvTime);
        tvOrigin = (TextView) view.findViewById(R.id.tvOrigin);

        tvTime.setText(duration);
        tvOrigin.setText(startAddress);

        IconGenerator generator = new IconGenerator(this);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = generator.makeIcon();
        originMarker = mgoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(selectPlaceEvent.getOrigin()));
    }

    private void addDestinationMarker(String distance, String endAddress) {
        View view = getLayoutInflater().inflate(R.layout.destination_info_window, null);

        TextView tvDestination = (TextView) view.findViewById(R.id.tvDestination);
        TextView tvDistance = (TextView) view.findViewById(R.id.tvDistance);

        tvDistance.setText(distance);
        tvDestination.setText(endAddress);

        IconGenerator generator = new IconGenerator(this);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = generator.makeIcon();
        destinationMarker = mgoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(selectPlaceEvent.getDestination()));
    }

    private void init() {

        if (ActivityCompat.checkSelfPermission(FindRider.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(FindRider.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {

                    LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                    LatLng destination = new LatLng(orderLat, orderLng);

                    String originString = new StringBuilder("").append(origin.latitude).append(",")
                            .append(origin.longitude).toString();

                    String destinationString = new StringBuilder("").append(destination.latitude).append(",")
                            .append(destination.longitude).toString();

                    Geocoder geocoder = new Geocoder(FindRider.this, Locale.getDefault());
                    List<Address> addressList;
                    try {

                        addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        String address = addressList.get(0).getAddressLine(0);

                        selectPlaceEvent = new SelectPlaceEvent(origin, destination, originString, destinationString, address);

                        drawPath(selectPlaceEvent);

                        updateFirebaseToken();

                        layout_confirm_Pickup.setVisibility(View.VISIBLE);
                        tvPickupAddress.setText(address);
                        btnConfirmPickup.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mgoogleMap == null) return;
                                if (selectPlaceEvent == null) return;
                                mgoogleMap.clear();
                                CameraPosition cameraPosition = new CameraPosition.Builder().target(selectPlaceEvent.getOrigin())
                                        .tilt(45f)
                                        .zoom(16f)
                                        .build();
                                mgoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                addMarkerWithPulseAnimation();
                            }
                        });
//                        Intent intent = new Intent(FindRider.this, RequestDriverActivity.class);
//                        intent.putExtra("orderId", orderId);
//                        startActivity(intent);
                        EventBus.getDefault().postSticky(new SelectPlaceEvent(origin, destination, originString, destinationString, address));
                    }
                    catch (Exception e){
                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });

        buildLocationRequest();
        buildLocationCallBack();
        updateLocation();

    }

    private void addMarkerWithPulseAnimation() {
        layout_confirm_Pickup.setVisibility(View.GONE);
        fill_all_screen.setVisibility(View.VISIBLE);
        finding_your_ride_layout.setVisibility(View.VISIBLE);
        originMarker = mgoogleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker())
                .position(selectPlaceEvent.getOrigin()));

        addPulseEffect(selectPlaceEvent);
    }

    private void addPulseEffect(SelectPlaceEvent selectPlaceEvent) {
        if (lastPulseAnimator != null) lastPulseAnimator.cancel();
        if (lastUserCircle != null) lastUserCircle.setCenter(selectPlaceEvent.getOrigin());

        lastPulseAnimator = Common.valueAnimator(duration, animation ->{
            if (lastUserCircle != null) lastUserCircle.setRadius((Float)animation.getAnimatedValue());
            else {
                lastUserCircle = mgoogleMap.addCircle(new CircleOptions()
                        .center(selectPlaceEvent.getOrigin())
                        .radius((Float)animation.getAnimatedValue())
                        .strokeColor(Color.WHITE)
                        .fillColor(Color.parseColor("#33333333")));
            }
        });

        startMapCameraSpinningAnimation(selectPlaceEvent);
    }

    private void startMapCameraSpinningAnimation(SelectPlaceEvent selectPlaceEvent) {
        if (animator != null) animator.cancel();
        animator = ValueAnimator.ofFloat(0, DESIRED_NUMBER_OF_SPINS * 360);
        animator.setDuration(DESIRED_NUMBER_OF_SPINS * DESIRED_SECONDS_PER_ONE_360_SPIN * 1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setStartDelay(100);
        animator.addUpdateListener(valueAnimator -> {
            Float newBearingValue = (Float) valueAnimator.getAnimatedValue();
            mgoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(selectPlaceEvent.getOrigin())
                    .zoom(16f)
                    .tilt(45f)
                    .bearing(newBearingValue)
                    .build()));
        });
        animator.start();

        findingNearByDriver(selectPlaceEvent);
    }

    private void findingNearByDriver(SelectPlaceEvent selectPlaceEvent) {

        Geocoder geocoder = new Geocoder(FindRider.this, Locale.getDefault());
        List<Address> addressList;
        try {
            addressList = geocoder.getFromLocation(selectPlaceEvent.getOrigin().latitude, selectPlaceEvent.getOrigin().longitude, 1);
            cityName = addressList.get(0).getLocality();

            DatabaseReference driver_location_ref = FirebaseDatabase.getInstance().getReference("driversLocation").child(cityName);

            GeoFire geoFire = new GeoFire(driver_location_ref);
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(selectPlaceEvent.getOrigin().latitude,  selectPlaceEvent.getOrigin().longitude), distance);
            geoQuery.removeAllListeners();

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {

                    if (!Common.driverFound.containsKey(key)) {
                        Common.driverFound.put(key, new DriverGeoModel(key, location));
                    }
                }

                @Override
                public void onKeyExited(String key) {
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                }

                @Override
                public void onGeoQueryReady() {
                    if (distance <= LIMIT_RANGE) {
                        distance++;
                    }
                    else {
                        distance = 1.0;
                    }
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    Toast.makeText(FindRider.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Common.driverFound.size() > 0){
                        findDriver();
                    }
                    else{
                        finding_your_ride_layout.setVisibility(View.GONE);
                        mgoogleMap.clear();
                        fill_all_screen.setVisibility(View.GONE);
                        if (animator != null){
                            animator.end();
                        }
                        Toast.makeText(FindRider.this, "Driver not found!", Toast.LENGTH_SHORT).show();

                    }
                }
            }, 2000);


        }
        catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(FindRider.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void findDriver() {

        Toast.makeText(FindRider.this, "Chala", Toast.LENGTH_SHORT).show();


        float min_distace = 0;
            DriverGeoModel driverGeoModel = null;
            Location currentRiderLocation = new Location("");
            currentRiderLocation.setLatitude(selectPlaceEvent.getOrigin().latitude);
            currentRiderLocation.setLongitude(selectPlaceEvent.getOrigin().longitude);
            for (String key : Common.driverFound.keySet()){
                Location driverLocation = new Location("");
                driverLocation.setLatitude(Common.driverFound.get(key).getGeoLocation().latitude);
                driverLocation.setLongitude(Common.driverFound.get(key).getGeoLocation().longitude);

                //Compare 2 locations
                if (min_distace == 0){
                    min_distace = driverLocation.distanceTo(currentRiderLocation);

                    if (!Common.driverFound.get(key).isDecline()){

                        driverGeoModel = Common.driverFound.get(key);
                        break;
                    }
                    else {
                        continue;
                    }
                }
                else if (driverLocation.distanceTo(currentRiderLocation) < min_distace){
                    min_distace = driverLocation.distanceTo(currentRiderLocation);

                    if (!Common.driverFound.get(key).isDecline()){

                        driverGeoModel = Common.driverFound.get(key);
                        break;
                    }
                    else {
                        continue;
                    }
                }
//                Snackbar.make(main_layout, new StringBuilder("Found driver: ")
//                        .append(foundDriver.getDriverInfoModel().getPhoneNumber()),Snackbar.LENGTH_LONG).show();

            }

            if (driverGeoModel != null){
                UserUtils.sendRequestToDriver(this, fill_all_screen, driverGeoModel, selectPlaceEvent);
                lastDriverCall = driverGeoModel;
            }
            else {
                Toast.makeText(this, "Driver not found!",Toast.LENGTH_LONG).show();
                lastDriverCall = null;
                finish();
            }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mgoogleMap = googleMap;

        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                if (ActivityCompat.checkSelfPermission(FindRider.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(FindRider.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mgoogleMap.setMyLocationEnabled(true);
                mgoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                mgoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {

                        if (ActivityCompat.checkSelfPermission(FindRider.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(FindRider.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                                        Toast.makeText(FindRider.this, "dosra wala" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                        return true;
                    }
                });

                View locationbutton = ((View)findViewById(Integer.parseInt("1")).getParent())
                        .findViewById(Integer.parseInt("2"));
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationbutton.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.CENTER_IN_PARENT);
                params.setMargins(0,0,0,400);

                buildLocationRequest();
                buildLocationCallBack();
                updateLocation();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Toast.makeText(FindRider.this, "Permission "+permissionDeniedResponse.getPermissionName()+" "+
                        "was denied!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();

    }

    private void buildLocationRequest() {
        if (locationRequest == null){
            locationRequest = LocationRequest.create();
            locationRequest.setSmallestDisplacement(10f);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(9000);
        }
    }

    private void buildLocationCallBack() {
        if (locationCallback == null){
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                    mgoogleMap.moveCamera(CameraUpdateFactory.newLatLng(newPosition));
                }
            };
        }
    }

    private void updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!EventBus.getDefault().isRegistered(this)) {

            EventBus.getDefault().register(this);
        }
    }

}