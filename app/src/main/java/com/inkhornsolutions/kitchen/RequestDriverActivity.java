package com.inkhornsolutions.kitchen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.inkhornsolutions.kitchen.Common.Common;
import com.inkhornsolutions.kitchen.EventBus.DeclineRequestAndRemoveTripFromDriver;
import com.inkhornsolutions.kitchen.EventBus.DeclineRequestFromDriver;
import com.inkhornsolutions.kitchen.EventBus.DriverAcceptTripEvent;
import com.inkhornsolutions.kitchen.EventBus.DriverCompleteTripEvent;
import com.inkhornsolutions.kitchen.EventBus.SelectPlaceEvent;
import com.inkhornsolutions.kitchen.EventBus.TimeUp;
import com.inkhornsolutions.kitchen.Utils.UserUtils;
import com.inkhornsolutions.kitchen.modelclasses.DriverGeoModel;
import com.inkhornsolutions.kitchen.modelclasses.TripPlanModel;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.disposables.CompositeDisposable;

public class RequestDriverActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SelectPlaceEvent selectPlaceEvent;
    private TextView tvOrigin;
    //Routes
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Polyline blackPolyLine, greyPolyline;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private List<LatLng> polylineList;

    private Marker originMarker, destinationMarker;
    private SupportMapFragment mapFragment;

    private CardView layout_confirm_uber,layout_confirm_pickup, finding_your_ride_layout;
    private Button btnConfirmPickup, btnConfirmRider;
    private TextView tvPickupAddress;
    private View fillMaps;
    private DriverGeoModel lastDriverCall;
    private String driverOldPosition = "";
    private LatLng driverOldPositionLatLng;
    private Handler handler;
    private float v;
    private double lat, lng;
    private int index, next;
    private LatLng start,end;

    private String orderId="";

    private CardView driverInfoLayout;
    private CircleImageView ivDriver;
    private ImageView ivCallDriver;
    private TextView tvBikeType, tvCarNumber, tvDriverName, tvRating, tvForeigLanguage;
    private  EditText etNote;

    private static final String DIRECTION_API_KEY = "AIzaSyDl7YXtTZQNBkthV3PjFS0fQOKvL8SIR7k";

    //Effect
    private Circle lastUserCircle;
    private long duration = 1000;
    private ValueAnimator lastPulseAnimator;

    //Slowly camera spinning
    private ValueAnimator animator;
    private static final int DESIRED_NUMBER_OF_SPINS = 5;
    private static final int DESIRED_SECONDS_PER_ONE_360_SPIN = 40;

    private RelativeLayout main_layout;

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDriverAcceptEvent(DriverAcceptTripEvent event){
        //Get trip informations
        FirebaseDatabase.getInstance().getReference("Trips").child(event.getTripId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            TripPlanModel tripPlanModel = snapshot.getValue(TripPlanModel.class);
                            mMap.clear();
                            fillMaps.setVisibility(View.GONE);
                            if (animator != null){
                                animator.end();
                            }
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(mMap.getCameraPosition().target)
                                    .tilt(0f)
                                    .zoom(mMap.getCameraPosition().zoom)
                                    .build();
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

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
                                                greyPolyline = mMap.addPolyline(polylineOptions);
                                                blackPolyLine = mMap.addPolyline(blackPolylineOptions);

                                                LatLng origin = new LatLng(
                                                        Double.parseDouble(tripPlanModel.getOrigin().split(",")[0]),
                                                        Double.parseDouble(tripPlanModel.getOrigin().split(",")[1]));

                                                LatLng destination = new LatLng(tripPlanModel.getCurrentLat(), tripPlanModel.getCurrentLng());

                                                LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                                        .include(origin)
                                                        .include(destination)
                                                        .build();

                                                addPickupMarkerWithDuration(duration, origin);
                                                addDriverMarker(destination);

                                                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                                                mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 1));

                                                initDriverForMoving(event.getTripId(), tripPlanModel);

                                                Glide.with(RequestDriverActivity.this).load(tripPlanModel.getDriverInfoModel().getDriverProfileImage()).into(ivDriver);
                                                tvDriverName.setText(tripPlanModel.getDriverInfoModel().getFirstName()+" "+tripPlanModel.getDriverInfoModel().getLastName());


                                                layout_confirm_pickup.setVisibility(View.GONE);
                                                layout_confirm_uber.setVisibility(View.GONE);
                                                driverInfoLayout.setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void onDirectionFailure(@NonNull Throwable t) {

                                            }
                                        });
                            }
                            catch (Exception e){
                                Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                        else {
                            Snackbar.make(main_layout, "Trip not found with key"+event.getTripId(), Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Snackbar.make(main_layout, error.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void initDriverForMoving(String tripId, TripPlanModel tripPlanModel) {

        driverOldPosition = new StringBuilder()
                .append(tripPlanModel.getCurrentLat())
                .append(",")
                .append(tripPlanModel.getCurrentLng())
                .toString();

        driverOldPositionLatLng = new LatLng(tripPlanModel.getCurrentLat(), tripPlanModel.getCurrentLng());

        FirebaseDatabase.getInstance().getReference("Trips").child(tripId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    TripPlanModel newData = snapshot.getValue(TripPlanModel.class);

                    LatLng driverNewLocation = new LatLng(newData.getCurrentLat(), newData.getCurrentLng());

                    if (driverOldPositionLatLng == driverNewLocation){

                        Toast.makeText(RequestDriverActivity.this, "old"+driverOldPositionLatLng+"new"+driverNewLocation, Toast.LENGTH_LONG).show();
                        moveMarkerAnimation(destinationMarker, driverOldPositionLatLng, driverNewLocation);
                    }
                    else {
                        Toast.makeText(RequestDriverActivity.this, "not Matching", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(main_layout, error.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void moveMarkerAnimation(Marker marker, LatLng from, LatLng to) {
        try {
            GoogleDirection.withServerKey(DIRECTION_API_KEY)
                    .from(from)
                    .to(to)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(@Nullable Direction direction) {
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);
                            polylineList = leg.getDirectionPoint();

                            String duration = leg.getDuration().getText();
                            String distance = leg.getDistance().getText();


                            blackPolylineOptions = new PolylineOptions();
                            blackPolylineOptions.color(Color.BLACK);
                            blackPolylineOptions.width(5);
                            blackPolylineOptions.startCap(new SquareCap());
                            blackPolylineOptions.jointType(JointType.ROUND);
                            blackPolylineOptions.addAll(polylineList);
                            greyPolyline = mMap.addPolyline(polylineOptions);
                            blackPolyLine = mMap.addPolyline(blackPolylineOptions);

                            Bitmap bitmap = Common.createIconWithDuration(RequestDriverActivity.this, duration);
                            originMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));

                            handler = new Handler(Looper.myLooper());
                            index = -1;
                            next = 1;

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (index < polylineList.size() -2){
                                        index++;
                                        next = index + 1;
                                        start = polylineList.get(index);
                                        end = polylineList.get(next);
                                    }

                                    ValueAnimator valueAnimator = ValueAnimator.ofInt(0,1);
                                    valueAnimator.setDuration(1500);
                                    valueAnimator.setInterpolator(new LinearInterpolator());
                                    valueAnimator.addUpdateListener(animation -> {
                                        v = animation.getAnimatedFraction();
                                        lng = v * end.longitude + (1 - v) * start.longitude;
                                        lat = v * end.latitude + (1 - v) * start.latitude;
                                        LatLng newPosition = new LatLng(lat,lng );
                                        marker.setPosition(newPosition);
                                        marker.setAnchor(0.5f, 0.5f);
                                        marker.setRotation(Common.getBearing(start, newPosition));

                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(newPosition));
                                    });

                                    valueAnimator.start();
                                    if (index < polylineList.size() - 2){
                                        handler.postDelayed(this, 1500);
                                    }
                                    else if (index < polylineList.size() - 1){

                                    }
                                }
                            }, 1500);

                            driverOldPositionLatLng = to;
                        }

                        @Override
                        public void onDirectionFailure(@NonNull Throwable t) {
                            Snackbar.make(mapFragment.getView(), t.getMessage(), Snackbar.LENGTH_LONG).show();

                        }
                    });

        }
        catch (Exception e){
            Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
        }

    }

    private void addDriverMarker(LatLng destination) {
        destinationMarker = mMap.addMarker(new MarkerOptions().position(destination).flat(true)
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bike)));
    }

    private void addPickupMarkerWithDuration(String duration, LatLng origin) {
        Bitmap icon = Common.createIconWithDuration(this, duration);
        originMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(origin));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectPlaceEvent(SelectPlaceEvent event){
        selectPlaceEvent = event;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDeclineRequestEvent(DeclineRequestFromDriver event){

        if (lastDriverCall != null){
            Common.driverFound.get(lastDriverCall.getKey()).setDecline(true);
            findNearByDriver(selectPlaceEvent);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDeclineRequestAndRemoveTripEvent(DeclineRequestAndRemoveTripFromDriver event){

        if (lastDriverCall != null){
            Common.driverFound.get(lastDriverCall.getKey()).setDecline(true);
            finish();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDriverCompleteTrip(DriverCompleteTripEvent event){

        Intent intent = new Intent(RequestDriverActivity.this, Items.class);

        Common.showNotification(this, new Random().nextInt(),
                "Complete trip",
                "Your trip: "+event.getTripKey()+"has been completed.",
                getIntent());

        startActivity(intent);

        finish();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onTimeUp(TimeUp event){


        Common.showNotification(this, new Random().nextInt(),
                "TimeUp",
                "Please HarryUp",
                getIntent());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);

        layout_confirm_pickup = (CardView) findViewById(R.id.layout_confirm_Pickup);
        layout_confirm_uber = (CardView) findViewById(R.id.layout_confirm_uber);
        finding_your_ride_layout = (CardView) findViewById(R.id.finding_your_ride_layout);

        btnConfirmPickup = (Button) findViewById(R.id.btnConfirmPickup);
        btnConfirmRider = (Button) findViewById(R.id.btnConfirmRider);

        tvPickupAddress = (TextView) findViewById(R.id.tvPickupAddress);

        main_layout = (RelativeLayout) findViewById(R.id.main_layout);

        fillMaps = (View) findViewById(R.id.fillMaps);

        driverInfoLayout = (CardView) findViewById(R.id.driverInfoLayout);
        ivDriver = (CircleImageView) findViewById(R.id.ivDriver);
        ivCallDriver = (ImageView) findViewById(R.id.ivCallDriver);
        tvBikeType = (TextView) findViewById(R.id.tvBikeType);
        tvCarNumber = (TextView) findViewById(R.id.tvCarNumber);
        tvRating = (TextView) findViewById(R.id.tvRating);
        tvDriverName = (TextView) findViewById(R.id.tvDriverName);
        tvForeigLanguage = (TextView) findViewById(R.id.tvForeigLanguage);
        etNote = (EditText) findViewById(R.id.etNote);

        orderId = getIntent().getStringExtra("orderId");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnConfirmRider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_confirm_pickup.setVisibility(View.VISIBLE);
                layout_confirm_uber.setVisibility(View.GONE);

                setDataPickup();
            }
        });

        btnConfirmPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap == null) return;
                if (selectPlaceEvent == null) return;
                mMap.clear();
                CameraPosition cameraPosition = new CameraPosition.Builder().target(selectPlaceEvent.getOrigin())
                        .tilt(45f)
                        .zoom(16f)
                        .build();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                addMarkerWithPulseAnimation();
            }
        });
    }

    private void addMarkerWithPulseAnimation() {
        layout_confirm_pickup.setVisibility(View.GONE);
        fillMaps.setVisibility(View.VISIBLE);
        finding_your_ride_layout.setVisibility(View.VISIBLE);
        originMarker = mMap.addMarker(new MarkerOptions()
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
                lastUserCircle = mMap.addCircle(new CircleOptions()
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
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
            .target(selectPlaceEvent.getOrigin())
            .zoom(16f)
            .tilt(45f)
            .bearing(newBearingValue)
            .build()));
        });
        animator.start();
        
        findNearByDriver(selectPlaceEvent);
    }

    private void findNearByDriver(SelectPlaceEvent selectPlaceEvent) {
        if (Common.driverFound.size() > 0){

            float min_distace = 0;
            DriverGeoModel foundDriver = null;
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

                        foundDriver = Common.driverFound.get(key);
                        break;
                    }
                    else {
                        continue;
                    }
                }
                else if (driverLocation.distanceTo(currentRiderLocation) < min_distace){
                    min_distace = driverLocation.distanceTo(currentRiderLocation);

                    if (!Common.driverFound.get(key).isDecline()){

                        foundDriver = Common.driverFound.get(key);
                        break;
                    }
                    else {
                        continue;
                    }
                }
//                Snackbar.make(main_layout, new StringBuilder("Found driver: ")
//                        .append(foundDriver.getDriverInfoModel().getPhoneNumber()),Snackbar.LENGTH_LONG).show();

            }

            if (foundDriver != null){
                UserUtils.sendRequestToDriver(this, main_layout, foundDriver, selectPlaceEvent);
                lastDriverCall = foundDriver;
            }
            else {
                Toast.makeText(this, "Driver not found!",Toast.LENGTH_LONG).show();
                lastDriverCall = null;
                finish();
            }
        }
        else {
            Snackbar.make(main_layout, "Driver not found!",Snackbar.LENGTH_LONG).show();
            lastDriverCall = null;
            finish();
        }
    }

    private void setDataPickup() {
        tvPickupAddress.setText(tvOrigin != null ? tvOrigin.getText() : "None");
        mMap.clear();
        addPickupMarker();
    }

    private void addPickupMarker() {
        View view = getLayoutInflater().inflate(R.layout.pickup_info_window, null);
        IconGenerator generator = new IconGenerator(this);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = generator.makeIcon();
        originMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(selectPlaceEvent.getOrigin()));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        drawPath(selectPlaceEvent);
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
                            polylineOptions.color(Color.GRAY);
                            polylineOptions.width(12);
                            polylineOptions.startCap(new SquareCap());
                            polylineOptions.jointType(JointType.ROUND);
                            polylineOptions.addAll(polylineList);
                            greyPolyline = mMap.addPolyline(polylineOptions);

                            blackPolylineOptions = new PolylineOptions();
                            blackPolylineOptions.color(Color.BLACK);
                            blackPolylineOptions.width(5);
                            blackPolylineOptions.startCap(new SquareCap());
                            blackPolylineOptions.jointType(JointType.ROUND);
                            blackPolylineOptions.addAll(polylineList);
                            greyPolyline = mMap.addPolyline(polylineOptions);
                            blackPolyLine = mMap.addPolyline(blackPolylineOptions);

                            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
                            valueAnimator.setDuration(1000);
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

                            addOriginMarker(duration, startAddress);
                            addDestinationMarker(distance ,endAddress);

                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 1));
                            

                        }

                        @Override
                        public void onDirectionFailure(@NonNull Throwable t) {

                        }
                    });
            }
            catch (Exception e){
                Snackbar.make(mapFragment.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            }

    }

    private void addOriginMarker(String duration, String startAddress) {
        View view = getLayoutInflater().inflate(R.layout.origin_info_window, null);

        TextView tvTime = (TextView) view.findViewById(R.id.tvTime);
        tvOrigin = (TextView) view.findViewById(R.id.tvOrigin);

        tvTime.setText(duration);
        tvOrigin.setText(startAddress);

        IconGenerator generator = new IconGenerator(this);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = generator.makeIcon();
        originMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(selectPlaceEvent.getOrigin()));
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
        destinationMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(selectPlaceEvent.getDestination()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
        if (EventBus.getDefault().hasSubscriberForEvent(SelectPlaceEvent.class)){
            EventBus.getDefault().removeStickyEvent(SelectPlaceEvent.class);
        }
        if (EventBus.getDefault().hasSubscriberForEvent(DeclineRequestFromDriver.class)){
            EventBus.getDefault().removeStickyEvent(DeclineRequestFromDriver.class);
        }
        if (EventBus.getDefault().hasSubscriberForEvent(DriverAcceptTripEvent.class)){
            EventBus.getDefault().removeStickyEvent(DriverAcceptTripEvent.class);
        }
        if (EventBus.getDefault().hasSubscriberForEvent(DeclineRequestAndRemoveTripFromDriver.class)){
            EventBus.getDefault().removeStickyEvent(DeclineRequestAndRemoveTripFromDriver.class);
        }
        if (EventBus.getDefault().hasSubscriberForEvent(DriverCompleteTripEvent.class)){
            EventBus.getDefault().removeStickyEvent(DriverCompleteTripEvent.class);
        }
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (animator != null) animator.end();
        super.onDestroy();
    }
}