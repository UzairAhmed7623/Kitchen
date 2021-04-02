package com.example.kitchen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.example.kitchen.Common.Common;
import com.example.kitchen.EventBus.SelectPlaceEvent;
import com.example.kitchen.Utils.UserUtils;
import com.example.kitchen.modelclasses.DriverGeoModel;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.ui.IconGenerator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.time.LocalDate;
import java.util.List;

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

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
        if (EventBus.getDefault().hasSubscriberForEvent(SelectPlaceEvent.class)){
            EventBus.getDefault().removeStickyEvent(SelectPlaceEvent.class);
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        if (animator != null) animator.end();
        super.onDestroy();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectPlaceEvent(SelectPlaceEvent event){
        selectPlaceEvent = event;
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

        init();
    }

    private void addMarkerWithPulseAnimation() {
        layout_confirm_pickup.setVisibility(View.GONE);
        fillMaps.setVisibility(View.VISIBLE);
        finding_your_ride_layout.setVisibility(View.VISIBLE);
        originMarker = mMap.addMarker(new MarkerOptions()
        .icon(BitmapDescriptorFactory.defaultMarker())
        .position(selectPlaceEvent.getOrigin()));

        addPulseEffect(selectPlaceEvent.getOrigin());
    }

    private void addPulseEffect(LatLng origin) {
        if (lastPulseAnimator != null) lastPulseAnimator.cancel();
        if (lastUserCircle != null) lastUserCircle.setCenter(origin);

        lastPulseAnimator = Common.valueAnimator(duration, animation ->{
            if (lastUserCircle != null) lastUserCircle.setRadius((Float)animation.getAnimatedValue());
            else {
                lastUserCircle = mMap.addCircle(new CircleOptions()
                .center(origin)
                .radius((Float)animation.getAnimatedValue())
                .strokeColor(Color.WHITE)
                .fillColor(Color.parseColor("#33333333")));
            }
        });

        startMapCameraSpinningAnimation(origin);
    }

    private void startMapCameraSpinningAnimation(LatLng target) {
        if (animator != null) animator.cancel();
        animator = ValueAnimator.ofFloat(0, DESIRED_NUMBER_OF_SPINS * 360);
        animator.setDuration(DESIRED_NUMBER_OF_SPINS * DESIRED_SECONDS_PER_ONE_360_SPIN * 1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setStartDelay(100);
        animator.addUpdateListener(valueAnimator -> {
            Float newBearingValue = (Float) valueAnimator.getAnimatedValue();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
            .target(target)
            .zoom(16f)
            .tilt(45f)
            .bearing(newBearingValue)
            .build()));
        });
        animator.start();
        
        findNearByDriver(target);
    }

    private void findNearByDriver(LatLng target) {
        if (Common.driverFound.size() > 0){

            float min_distace = 0;
            DriverGeoModel foundDriver = Common.driverFound.get(Common.driverFound.keySet().iterator().next());
            Location currentRiderLocation = new Location("");
            currentRiderLocation.setLatitude(target.latitude);
            currentRiderLocation.setLongitude(target.longitude);
            for (String key : Common.driverFound.keySet()){
                Location driverLocation = new Location("");
                driverLocation.setLatitude(Common.driverFound.get(key).getGeoLocation().latitude);
                driverLocation.setLongitude(Common.driverFound.get(key).getGeoLocation().longitude);

                //Compare 2 locations
                if (min_distace == 0){
                    min_distace = driverLocation.distanceTo(currentRiderLocation);
                    foundDriver = Common.driverFound.get(key);
                }
                else if (driverLocation.distanceTo(currentRiderLocation) < min_distace){
                    min_distace = driverLocation.distanceTo(currentRiderLocation);
                    foundDriver = Common.driverFound.get(key);
                }
//                Snackbar.make(main_layout, new StringBuilder("Found driver: ")
//                        .append(foundDriver.getDriverInfoModel().getPhoneNumber()),Snackbar.LENGTH_LONG).show();

                UserUtils.sendRequestToDriver(this, main_layout, foundDriver, target);

            }
        }
        else {
            Snackbar.make(main_layout, "Driver not found!",Snackbar.LENGTH_LONG).show();
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

    private void init() {

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
                            String startAddress = leg.getStartAddress().replace(", Punjab,","").replace("Pakistan","");
                            String endAddress = leg.getEndAddress().replace(", Punjab,","").replace("Pakistan","");

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
}