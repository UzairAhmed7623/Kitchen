package com.example.kitchen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.kitchen.adapters.MainActivityAdapter;
import com.example.kitchen.modelclasses.ItemsModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<ItemsModelClass> items = new ArrayList<>();
    private RecyclerView rvItems;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    MainActivityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

//        String resName = getIntent().getStringExtra("resName");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(MainActivity.this);
        navigationView.setCheckedItem(R.id.navView);

        rvItems = (RecyclerView) findViewById(R.id.rvItems);
        rvItems.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        Toast.makeText(this, "Alfredo Pizza Pasta", Toast.LENGTH_SHORT).show();

        firebaseFirestore.collection("Restaurants").document("Alfredo Pizza Pasta").collection("Items")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        if (documentSnapshot.exists()){
                            String itemName = documentSnapshot.getId();
                            String imageUri = documentSnapshot.getString("imageUri");
                            String price = documentSnapshot.getString("price");
                            String available = documentSnapshot.getString("available");
                            String schedule = documentSnapshot.getString("schedule");

                            Log.d("TAG", itemName+imageUri+price);

                            ItemsModelClass itemsModelClass = new ItemsModelClass();
                            itemsModelClass.setResName("Alfredo Pizza Pasta");
                            itemsModelClass.setItemName(itemName);
                            itemsModelClass.setImage(imageUri);
                            itemsModelClass.setPrice(price);
                            itemsModelClass.setAvailability(available);
                            itemsModelClass.setSchedule(schedule);

                            items.add(itemsModelClass);
                        }
                    }
                    rvItems.setAdapter(new MainActivityAdapter(MainActivity.this, items));
                }

            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addItem){
            Intent intent = new Intent(MainActivity.this, AddItem.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("resName", "Alfredo Pizza Pasta");
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.pendingOrders){
            Intent intent = new Intent(MainActivity.this, Orders.class);
            intent.putExtra("resName", "Alfredo Pizza Pasta");
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        else if (item.getItemId() == R.id.deliveredOrders){
            Intent intent = new Intent(MainActivity.this, CompletedOrders.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        else if (item.getItemId() == R.id.Profile){
            Intent intent = new Intent(MainActivity.this, Profile.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

}