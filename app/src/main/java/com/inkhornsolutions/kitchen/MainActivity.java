package com.inkhornsolutions.kitchen;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bitvale.switcher.SwitcherX;
import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inkhornsolutions.kitchen.Fragments.InProgress;
import com.inkhornsolutions.kitchen.Fragments.RecentOrders;
import com.inkhornsolutions.kitchen.Utils.UserUtils;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView tvUserName, tvOnline, tvOffline;
    private SwitcherX statusSwitch;
    private CircleImageView ivProfileImage;
    private ImageView ivProfileSettings;
    private String getResNamefromEditText = "";
    private String resName = "";
    private final List<String> resIdsList = new ArrayList<>();
    private final List<String> resNamesList = new ArrayList<>();
    private Dialog builder;
    private View view;
    private String type = "";
    private Uri fileUri;
    private String sdownload_url = "";
    private ImageView ivResImage;
    private ImageButton ibChat;
    private TextView tvResName, tvTotalPrice;
    private double sum = 0;
    Double deductedTotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
//        String resName = getIntent().getStringExtra("resName");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvOnline = (TextView) findViewById(R.id.tvOnline);
        tvOffline = (TextView) findViewById(R.id.tvOffline);
        statusSwitch = (SwitcherX) findViewById(R.id.statusSwitch);
        ibChat = (ImageButton) findViewById(R.id.ibChat);
        tvResName = (TextView) findViewById(R.id.tvResName);
        tvTotalPrice = (TextView) findViewById(R.id.tvTotalPrice);

//        layoutOrder.setWaveColor(0xFF000000+new Random().nextInt(0xFFFFFF)); // Random color assign

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                // Get new FCM registration token
                String token = task.getResult();

                UserUtils.updateToken(MainActivity.this, token);
            }
        });

        statusSwitch.setOnCheckedChangeListener(new Function1<Boolean, Unit>() {
            @Override
            public Unit invoke(Boolean aBoolean) {
                if (aBoolean) {
                    tvOnline.setVisibility(View.VISIBLE);
                    tvOffline.setVisibility(View.GONE);

                    firebaseFirestore.collection("Restaurants").document(resName).update("status", "online").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("status", "online");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                        }
                    });
                } else {
                    tvOffline.setVisibility(View.VISIBLE);
                    tvOnline.setVisibility(View.GONE);

                    firebaseFirestore.collection("Restaurants").document(resName).update("status", "offline").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("status", "online");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                        }
                    });
                }
                return Unit.INSTANCE;
            }
        });

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        View header_View = navigationView.getHeaderView(0);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.navigation_icon, getTheme());
        toggle.setHomeAsUpIndicator(drawable);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(MainActivity.this);
        navigationView.setCheckedItem(R.id.navView);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

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
            }
        });

        builder = new Dialog(this);

        firebaseFirestore.collection("Restaurants").whereEqualTo("id", firebaseAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                String resNames = documentSnapshot.getString("resName");

                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("restaurantName", resNames);
                                editor.apply();

//                                SharedPreferences prefs = getApplicationContext().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
//                                resName = prefs.getString("restaurantName", "");
//
//                                if (resName.length() <= 0) {
//                                    dialog();
//                                }
//                                else {
//                                    loadRestaurant(resName);
//                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                    }
                });

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
        resName = prefs.getString("restaurantName", "");

        if (resName.length() <= 0) {
            dialog();
        } else {
            validateRes(resName);
            tvResName.setText(resName);
        }

        ibChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        OrdersList(resName);
    }

    @SuppressLint("InflateParams")
    private void dialog() {
        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.edittext_for_restaurant_name, null);

        EditText etResName = (EditText) view.findViewById(R.id.etResName);
        Spinner spResCat = (Spinner) view.findViewById(R.id.spResCat);
        Button btnDone = (Button) view.findViewById(R.id.btnDone);
        Button btnCancel = (Button) view.findViewById(R.id.btnCancel);
        Button btnPickImage = (Button) view.findViewById(R.id.btnPickImage);
        ivResImage = (ImageView) view.findViewById(R.id.ivResImage);

        List<String> category = new ArrayList<>();
        category.add("Desi");
        category.add("FastFood");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, category);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spResCat.setAdapter(adapter);

        spResCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    type = "0";
                } else {
                    type = "1";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImagePicker.Companion.with(MainActivity.this)
                        .crop(3f, 2f)   //Crop image(Optional), Check Customization for more option
                        .compress(1024)       //Final image size will be less than 1 MB(Optional)
                        .start(1002);
            }
        });

        builder.setContentView(view);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etResName.getText().toString().length() <= 0) {
                    Toast.makeText(MainActivity.this, "Please write your restaurant name!", Toast.LENGTH_SHORT).show();
                } else {
                    getResNamefromEditText = etResName.getText().toString().trim();

                    firebaseFirestore.collection("Restaurants").get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                                        //Get data from database
                                        if (documentSnapshot.exists()) {

                                            if (documentSnapshot.getString("id") != null) {
                                                resIdsList.add(documentSnapshot.getString("id").trim());
                                            }
                                            if (!documentSnapshot.getId().isEmpty()) {
                                                resNamesList.add(documentSnapshot.getId().toLowerCase().trim().replace(" ", ""));
                                            }
                                        }
                                    }
                                    //Now compare that data to get results
                                    if (resNamesList.contains(getResNamefromEditText.toLowerCase().trim().replace(" ", ""))
                                            && !resIdsList.contains(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())) {
                                        Toast.makeText(MainActivity.this, "Restaurant already registered with these credentials!", Toast.LENGTH_SHORT).show();
                                    }
                                    if (!resNamesList.contains(getResNamefromEditText.toLowerCase().trim().replace(" ", ""))
                                            && resIdsList.contains(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())) {
                                        Toast.makeText(MainActivity.this, "Restaurant already registered with these credentials!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        SharedPreferences prefs = getApplicationContext().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("restaurantName", getResNamefromEditText);
                                        editor.apply();

                                        uploadImage(fileUri, getResNamefromEditText);

                                        HashMap<String, Object> resReg = new HashMap<>();
                                        resReg.put("resName", getResNamefromEditText);
                                        resReg.put("id", firebaseAuth.getCurrentUser().getUid());
                                        resReg.put("type", type);
                                        resReg.put("approved", "no");
                                        resReg.put("status", "offline");

                                        firebaseFirestore.collection("Restaurants").document(getResNamefromEditText).set(resReg, SetOptions.merge())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                                        alertDialog.setMessage("Your request has been sent to the system. Please wait for the approval.\n\nAfter request approval you" +
                                                                "will be able to use the app.");
                                                        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                finish();
                                                            }
                                                        });
                                                        alertDialog.setCancelable(false);
                                                        alertDialog.show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                                                    }
                                                });

                                        HashMap<String, Object> resReg1 = new HashMap<>();
                                        resReg.put("resName", getResNamefromEditText);
                                        resReg.put("id", Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());

                                        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).set(resReg1, SetOptions.merge())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("resReg", "request successfully sent.");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                                                    }
                                                });
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(findViewById(android.R.id.content), Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                                }
                            });
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();

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

//                        ((ViewGroup)view.getParent()).removeView(view);

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

    private void uploadImage(Uri fileUri, String resName) {

        if (fileUri != null) {
            String someFilepath = String.valueOf(fileUri);
            String extension = someFilepath.substring(someFilepath.lastIndexOf("."));

            StorageReference riversRef = storageReference.child("images/Restaurants" + "." + extension + " " + resName);

            riversRef.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Log.d("Image", "on Success");

                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    Uri downloadUrl = urlTask.getResult();

                    sdownload_url = String.valueOf(downloadUrl);

                    addNewRestaurantImage(sdownload_url, resName);

                    builder.dismiss();
                }
            });
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Image not found!", Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
        }
    }

    private void addNewRestaurantImage(String sdownload_url, String resName) {
        HashMap<String, Object> image = new HashMap<>();
        image.put("imageUri", sdownload_url);

        firebaseFirestore.collection("Restaurants").document(resName).set(image, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Image", "Image uploaded successfully!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
            }
        });
    }

    private void validateRes(String resName) {
        firebaseFirestore.collection("Restaurants").document(resName).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String approved = documentSnapshot.getString("approved");
                            String status = documentSnapshot.getString("status");

                            if (status != null && status.equals("online")) {
                                statusSwitch.setChecked(true, true);
                            }

                            if (Objects.equals(approved, "yes")) {

                                final boolean firstTime;

                                SharedPreferences preferences = getApplicationContext().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
                                firstTime = preferences.getBoolean("firstTime", true);

                                if (firstTime) {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                    alertDialog.setTitle("Congrats");
                                    alertDialog.setMessage("Your restaurant was approved. Now you can add your products by clicking on the upper right corner button.");
                                    alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            boolean firstTime = false;

                                            SharedPreferences prefs = getApplicationContext().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putBoolean("firstTime", firstTime);
                                            editor.apply();

                                            dialog.dismiss();

                                            loadRestaurant(resName);
                                        }
                                    });
                                    alertDialog.setCancelable(false);
                                    alertDialog.show();
                                } else {
                                    loadRestaurant(resName);
                                }


                            } else {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                alertDialog.setTitle("Oops");
                                alertDialog.setMessage("Your restaurant was not approved yet. Please wait for the approval.");
                                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                                alertDialog.setCancelable(false);
                                alertDialog.show();
                            }
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                    }
                });
    }

    private void loadRestaurant(String resName) {

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Recent Orders", RecentOrders.class)
                .add("In Progress", InProgress.class)
                .create());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);
    }

    public String sendData(){
        return resName;
    }

    private void removeView() {
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    private void OrdersList(String resName) {
        firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot value) {

                for (QueryDocumentSnapshot documentSnapshot : value){
                    if (documentSnapshot.exists()){
                        String id = documentSnapshot.getId();

                        firebaseFirestore.collection("Users").document(id).collection("Cart")
                                .whereEqualTo("status", "Dispatched")
                                .whereEqualTo("restaurantName", resName)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                    if (documentSnapshot.exists()){

                                        String total = documentSnapshot.getString("total");

                                        ArrayList<Double> totalPrice = new ArrayList<>();
                                        totalPrice.add(Double.parseDouble(total)-45);

                                        for (int i = 0; i < totalPrice.size(); i++)
                                        {
                                            sum = sum + totalPrice.get(i);
                                        }
                                        deductedTotal = sum* 0.8;

                                    }
                                    else {
                                        Snackbar.make(getParent().findViewById(android.R.id.content), "Data not found!", Snackbar.LENGTH_LONG).show();
                                    }
                                }

                                tvTotalPrice.setText("PKR" + deductedTotal);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1002 || resultCode == RESULT_OK && data != null && data.getData() != null) {
            //Image Uri will not be null for RESULT_OK
            fileUri = data.getData();
            Glide.with(this).load(fileUri).placeholder(ContextCompat.getDrawable(this, R.drawable.food_placeholder)).fitCenter().into(ivResImage);


//            //You can get File object from intent
//            File file = ImagePicker.getFile(data);
//
//
//            //You can also get File Path from intent
//            String filePath = ImagePicker.getFilePath(data);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, "" + ImagePicker.RESULT_ERROR, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    public void headerTextView() {
        if (firebaseAuth != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.getString("firstName") != null && documentSnapshot.getString("lastName") != null) {
                                String fuser_Name = documentSnapshot.getString("firstName");
                                String luser_Name = documentSnapshot.getString("lastName");
                                tvUserName.setText(fuser_Name + " " + luser_Name);
                            } else {
                                Log.d("TAG", "No data found!");
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "No data found!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("TAG", task.getException().getMessage());
                    }
                }
            });
        }

    }

    public void headerImage() {
        if (firebaseAuth != null) {
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.getString("ResImageProfile") != null) {
                                String imageUri = documentSnapshot.getString("ResImageProfile");
                                Glide.with(MainActivity.this).load(imageUri).placeholder(ContextCompat.getDrawable(getApplicationContext(), R.drawable.user)).into(ivProfileImage);
                            } else {
                                Log.d("TAG", "Not found!");
                            }
                        } else {
                            Log.d("TAG", "No data found!");
                        }
                    } else {
                        Log.d("TAG", task.getException().getMessage());
                    }
                }
            });
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.yourItems) {
            Intent intent = new Intent(MainActivity.this, Items.class);
            intent.putExtra("resName", resName);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        else if (item.getItemId() == R.id.wallet) {
            Intent intent = new Intent(MainActivity.this, Wallet.class);
            intent.putExtra("resName", resName);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
}