package com.inkhornsolutions.kitchen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.inkhornsolutions.kitchen.adapters.MainActivityAdapter;
import com.inkhornsolutions.kitchen.modelclasses.ItemsModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ArrayList<ItemsModelClass> items = new ArrayList<>();
    private RecyclerView rvItems;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView tvUserName;
    private CircleImageView ivProfileImage;
    private ImageView ivProfileSettings;
    private String m_Text = "";
    AlertDialog.Builder builder;

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
        View header_View = navigationView.getHeaderView(0);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(MainActivity.this);
        navigationView.setCheckedItem(R.id.navView);

        tvUserName = (TextView) header_View.findViewById(R.id.tvUserName);
        headerTextView();

        ivProfileImage = (CircleImageView) header_View.findViewById(R.id.ivProfileImage);
        headerImage();

        ivProfileSettings = (ImageView) header_View.findViewById(R.id.ivProfileSettings);
        ivProfileSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Profile.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        rvItems = (RecyclerView) findViewById(R.id.rvItems);
        rvItems.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.edittext_for_restaurant_name, null,false);

        EditText editText = (EditText) view.findViewById(R.id.editTextres);

        builder = new AlertDialog.Builder(this)
                .setTitle("Restaurant Name")
                .setView(view)
                .setMessage("Please write your issued restaurant name:")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getText().toString().length() <= 0){
                            editText.setError("Please write your issued restaurant name");
                            return;
                        }
                        m_Text = editText.getText().toString();

                        firebaseFirestore.collection("Restaurants").document(m_Text).collection("Items")
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
                                            itemsModelClass.setResName(m_Text);
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
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.myColor)).show();
                            }
                        });
                        dialog.dismiss();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setMessage("If you cancel it you will not be able to use this app.\nAre your sure you still want to cancel it?");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                        ((ViewGroup)view.getParent()).removeView(view);

                        builder.show();

                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.show();

            }
        });

        builder.setCancelable(false);

        builder.show();
    }

    public void headerTextView(){
        if (firebaseAuth != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            if (documentSnapshot.getString("firstName") != null && documentSnapshot.getString("lastName") != null){
                                String fuser_Name = documentSnapshot.getString("firstName");
                                String luser_Name = documentSnapshot.getString("lastName");
                                tvUserName.setText(fuser_Name +" "+luser_Name);
                            }
                            else {
                                Log.d("TAG", "No data found!");
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "No data found!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Log.d("TAG", task.getException().getMessage());
                    }
                }
            });
        }

    }

    public void headerImage(){
        if (firebaseAuth != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            if (documentSnapshot.getString("ResImageProfile") != null){
                                String imageUri = documentSnapshot.getString("ResImageProfile");
                                Glide.with(MainActivity.this).load(imageUri).placeholder(ContextCompat.getDrawable(getApplicationContext(), R.drawable.user)).into(ivProfileImage);
                            }
                            else {
                                Log.d("TAG", "Not found!");
                            }
                        }
                        else {
                            Log.d("TAG", "No data found!");
                        }
                    }
                    else {
                        Log.d("TAG", task.getException().getMessage());
                    }
                }
            });
        }

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
            intent.putExtra("resName", m_Text);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.pendingOrders){
            Intent intent = new Intent(MainActivity.this, Orders.class);
            intent.putExtra("resName", m_Text);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        else if (item.getItemId() == R.id.deliveredOrders){
            Intent intent = new Intent(MainActivity.this, CompletedOrders.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

}