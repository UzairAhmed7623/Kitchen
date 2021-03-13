package com.example.kitchen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoLocation;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.listeners.GeoQueryEventListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindDriver extends FragmentActivity implements OnMapReadyCallback {

    private CircleImageView driverImage;
    private TextView driverName,driverPhone;
    private LinearLayout layout;
    private GoogleMap mMap;
    private FirebaseFirestore firebaseFirestore;

    private LatLng latLng;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_driver);

        firebaseFirestore = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(FindDriver.this);

        driverImage = (CircleImageView) findViewById(R.id.driverImage);
        driverName = (TextView) findViewById(R.id.driverName);
        driverPhone = (TextView) findViewById(R.id.driverPhone);
        layout = (LinearLayout) findViewById(R.id.layout);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    latLng = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(latLng).title("You"));
                    getClosestCustomer(latLng);

                }
                else {
                    Toast.makeText(FindDriver.this, "Null", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void getClosestCustomer(LatLng latLng) {
//        String id = "cb0xbVIcK5dWphXuHIvVoUytfaM2";
//        CollectionReference collectionReference = firebaseFirestore.collection("Users");
//        GeoFirestore geoFirestore = new GeoFirestore(collectionReference);
//        geoFirestore.setLocation(id, new GeoPoint(latLng.latitude, latLng.longitude));

        getClosestDriver(latLng);
    }


    private int radius = 1;
    private boolean driverFound = false;
    private String driverFoundID;

    private void getClosestDriver(LatLng latLng){

        CollectionReference collectionReference = firebaseFirestore.collection("Users");

        Log.d("TAG", "LatLng"+latLng);

        GeoFirestore geoFirestore = new GeoFirestore(collectionReference);
        GeoQuery geoQuery = geoFirestore.queryAtLocation(new GeoPoint(latLng.latitude,latLng.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(@NotNull String key, @NotNull GeoPoint geoPoint) {
                if (!driverFound){
                    driverFound = true;
                    Log.d("TAG","Boolean: "+driverFound);

                    driverFoundID = key;
                    Log.d("TAG","Driver: "+driverFoundID);
                    Log.d("TAG","Driver's GeoPoints: "+geoPoint);

                    firebaseFirestore.collection("Users").document(key).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.exists()){
                                    String fName = documentSnapshot.getString("First Name");
                                    String lName = documentSnapshot.getString("Last Name");
                                    String phone = documentSnapshot.getString("Phone");
                                    String imageUri = documentSnapshot.getString("imageProfile");

                                    driverName.setText(fName + " " + lName);
                                    driverPhone.setText(phone);
                                    Glide.with(FindDriver.this).load(imageUri).placeholder(R.drawable.driver_placeholder).fitCenter().into(driverImage);

                                    LatLng latLng1 = new LatLng(geoPoint.getLatitude(),geoPoint.getLongitude());
                                    mMap.addMarker(new MarkerOptions().position(latLng1).title(fName + " " + lName));
//                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng1));
                                    LatLngBounds.Builder latlngBuilder = new LatLngBounds.Builder();

                                    latlngBuilder.include(latLng);
                                    latlngBuilder.include(latLng1);

                                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBuilder.build(), 100));

                                    GoogleDirection.withServerKey("AIzaSyDl7YXtTZQNBkthV3PjFS0fQOKvL8SIR7k")
                                            .from(latLng)
                                            .to(latLng1)
                                            .execute(new DirectionCallback() {
                                                @Override
                                                public void onDirectionSuccess(@Nullable Direction direction) {
//                                                PolylineOptions polylineOptions = new PolylineOptions();
//                                                polylineOptions.add()
//                                                polylineOptions.color(R.color.purple_700);
//                                                polylineOptions.width(2);
                                                    List<Step> stepList = direction.getRouteList().get(0).getLegList().get(0).getStepList();
                                                    ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(FindDriver.this, stepList, 5, Color.RED, 3, Color.BLUE);
                                                    for (PolylineOptions polylineOption : polylineOptionList) {
                                                        mMap.addPolyline(polylineOption);
                                                    }

                                                    Log.d("TAG222", ""+direction.getStatus());
                                                    Log.d("TAG222", ""+direction.describeContents());
                                                    Log.d("TAG222", ""+direction.getGeocodedWaypointList());
                                                    Log.d("TAG222", ""+direction.getRouteList().get(0).getLegList().get(0).getDuration().getText());

                                                }

                                                @Override
                                                public void onDirectionFailure(Throwable t) {
                                                    Log.d("TAG222", ""+t.getMessage());

                                                }
                                            });

                                    layout.setVisibility(View.VISIBLE);

                                }
                            }
                        }
                    });
                }
//                geoFirestore.removeLocation(driverFoundID);



            }

            @Override
            public void onKeyExited(@NotNull String key) {

            }

            @Override
            public void onKeyMoved(@NotNull String key, @NotNull GeoPoint geoPoint) {
                firebaseFirestore.collection("Users").document(key).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()){
                                String fName = documentSnapshot.getString("First Name");
                                String lName = documentSnapshot.getString("Last Name");
                                String phone = documentSnapshot.getString("Phone");
                                String imageUri = documentSnapshot.getString("imageProfile");

                                driverName.setText(fName + " " + lName);
                                driverPhone.setText(phone);
                                Glide.with(FindDriver.this).load(imageUri).placeholder(R.drawable.driver_placeholder).fitCenter().into(driverImage);

                                LatLng latLng1 = new LatLng(geoPoint.getLatitude(),geoPoint.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(latLng1).title(fName + " " + lName));
//                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng1));
                                LatLngBounds.Builder latlngBuilder = new LatLngBounds.Builder();

                                latlngBuilder.include(latLng);
                                latlngBuilder.include(latLng1);
                                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBuilder.build(), 1));




                                GoogleDirection.withServerKey("AIzaSyBa4XZ09JsXD8KYZr5wdle--0TQFpfyGew")
                                        .from(latLng)
                                        .to(latLng1)
                                        .execute(new DirectionCallback() {
                                            @Override
                                            public void onDirectionSuccess(@Nullable Direction direction) {
//                                                PolylineOptions polylineOptions = new PolylineOptions();
//                                                polylineOptions.add()
//                                                polylineOptions.color(R.color.purple_700);
//                                                polylineOptions.width(2);
                                                Log.d("TAG222", ""+direction.getStatus());
                                                Log.d("TAG222", ""+direction.describeContents());
                                                Log.d("TAG222", ""+direction.getGeocodedWaypointList());
                                                Log.d("TAG222", ""+direction.getRouteList());

                                            }

                                            @Override
                                            public void onDirectionFailure(Throwable t) {
                                                // Do something here
                                            }
                                        });







                                layout.setVisibility(View.VISIBLE);

                            }
                        }
                    }
                });
            }

            @Override
            public void onGeoQueryReady() {
                Log.d("TAG","onGeoQuery");

                if (!driverFound){
                    radius++;
                    getClosestDriver(latLng);
                }
            }

            @Override
            public void onGeoQueryError(@NotNull Exception e) {

            }
        });

        collectionReference.whereEqualTo("status","available").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (QueryDocumentSnapshot documentSnapshot : value){
                    if (documentSnapshot.exists()){
                        String id = documentSnapshot.getId();
                        String fname = documentSnapshot.getString("First Name");
                        String lname = documentSnapshot.getString("Last Name");
                        Double lat = documentSnapshot.getDouble("Location.latitude");
                        Double lng = documentSnapshot.getDouble("Location.longitude");



                    }
                }
            }
        });
    }
}