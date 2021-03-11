package com.example.kitchen;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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

public class FindDriver extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_driver);

        firebaseFirestore = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getClosestDriver();

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void getClosestDriver(){
        CollectionReference collectionReference = firebaseFirestore.collection("Users");
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

                        GeoFirestore geoFirestore = new GeoFirestore(collectionReference);
                        GeoQuery geoQuery = geoFirestore.queryAtLocation(new GeoPoint(31.4968802,74.3622108),1);
                        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                            @Override
                            public void onKeyEntered(@NotNull String s, @NotNull GeoPoint geoPoint) {

                            }

                            @Override
                            public void onKeyExited(@NotNull String s) {

                            }

                            @Override
                            public void onKeyMoved(@NotNull String s, @NotNull GeoPoint geoPoint) {

                            }

                            @Override
                            public void onGeoQueryReady() {

                            }

                            @Override
                            public void onGeoQueryError(@NotNull Exception e) {

                            }
                        });

                    }
                }
            }
        });
    }
}