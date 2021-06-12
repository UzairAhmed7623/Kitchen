package com.inkhornsolutions.kitchen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class AddItem extends AppCompatActivity {

    private EditText etItemName, etItemPrice, etItemDes, etItemQuantity;
    private TextView tvFrom, tvTo;
    private TextView  tvAvailable, tvNotAvailable;
    private SwitchMaterial switchAvailable;
    private int t1hour, t1minute, t2hour, t2minute;
    private ImageView etItemImage;
    private Button btnDone, btnPickImage;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String resName, name;
    private Uri fileUri;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private ProgressDialog dialog;
    private Toolbar toolbar;
    private String isAvailable = "No";
    private ImageView ivCategories;
    private String selectedCategory;
    private TextView tvSpinnerHead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add new product");

        resName = getIntent().getStringExtra("resName");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etItemName = (EditText) findViewById(R.id.etItemName);
        etItemQuantity = (EditText) findViewById(R.id.etItemQuantity);
        etItemDes = (EditText) findViewById(R.id.etItemDes);
        etItemPrice = (EditText) findViewById(R.id.etItemPrice);
        etItemImage = (ImageView) findViewById(R.id.etItemImage);
        tvFrom = (TextView) findViewById(R.id.tvFrom);
        tvTo = (TextView) findViewById(R.id.tvTo);
        btnPickImage = (Button) findViewById(R.id.btnPickImage);
        btnDone = (Button) findViewById(R.id.btnDone);
        tvAvailable = (TextView) findViewById(R.id.tvAvailable);
        tvNotAvailable = (TextView) findViewById(R.id.tvNotAvailable);
        switchAvailable = (SwitchMaterial) findViewById(R.id.switchAvailable);
        ivCategories = (ImageView) findViewById(R.id.ivCategories);
        tvSpinnerHead = (TextView) findViewById(R.id.tvSpinnerHead);

        switchAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    tvAvailable.setVisibility(View.VISIBLE);
                    tvNotAvailable.setVisibility(View.GONE);
                    isAvailable = "yes";
                }
                else {
                    tvNotAvailable.setVisibility(View.VISIBLE);
                    tvAvailable.setVisibility(View.GONE);

                    isAvailable = "no";
                }
            }
        });

        //Code for copy document data.
//        DocumentReference copyFrom = FirebaseFirestore.getInstance().collection("Restaurants").document("asd").collection("Items").document("Honey");
        DocumentReference copyTo = FirebaseFirestore.getInstance().collection("Restaurants").document("Organic Shop").collection("Items").document("Honey");
//
//        copyFrom.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()){
//                    DocumentSnapshot documentSnapshot = task.getResult();
//                    copyTo.set(documentSnapshot.getData());
//                }
//            }
//        });




        ivCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(AddItem.this, R.style.MaterialThemeDialog);
                builder.setCancelable(false);
                String[] categories = new String[]{"Main Course","Drinks","Frozen","Sides","Desserts","Organic"};

                final int checkedItem = -1;

                builder.setTitle(getString(R.string.select_category));

                builder.setSingleChoiceItems(categories, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tvSpinnerHead.setText(categories[which]);
                    }
                });
                builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        selectedCategory = tvSpinnerHead.getText().toString();

                        if (selectedCategory.length() > 0){
                            dialog.dismiss();
                        }
                        else {
                            Snackbar.make(findViewById(android.R.id.content), "Please select one from all off the categories.", Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImagePicker.Companion.with(AddItem.this)
                        .crop(3f, 2f)	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .start(1002);
            }
        });

        tvFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddItem.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        t1hour = hourOfDay;
                        t1minute = minute;

                        String time = t1hour + ":" + t1minute;

                        SimpleDateFormat f24Hours = new SimpleDateFormat("HH:mm");

                        try {
                            Date date = f24Hours.parse(time);
                            SimpleDateFormat f12Hours = new SimpleDateFormat("hh:mm aa");

                            tvFrom.setText(f12Hours.format(date));

                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                },12, 0, false);

                timePickerDialog.updateTime(t1hour, t1minute);
                timePickerDialog.show();
            }
        });

        tvTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddItem.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        t2hour = hourOfDay;
                        t2minute = minute;

                        String time = t2hour + ":" + t2minute;

                        SimpleDateFormat f24Hours = new SimpleDateFormat("HH:mm");

                        try {
                            Date date = f24Hours.parse(time);
                            SimpleDateFormat f12Hours = new SimpleDateFormat("hh:mm aa");

                            tvTo.setText(f12Hours.format(date));

                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                },12, 0, false);

                timePickerDialog.updateTime(t2hour, t2minute);
                timePickerDialog.show();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog = new ProgressDialog(AddItem.this);
                dialog.setTitle("Please wait...");
                dialog.setMessage("We are working on your request");
                dialog.setCancelable(false);
                dialog.show();

                uploadImage(fileUri);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1002 || resultCode == RESULT_OK && data!= null && data.getData() != null) {
            //Image Uri will not be null for RESULT_OK
            fileUri = data.getData();
            etItemImage.setImageURI(fileUri);

//            //You can get File object from intent
//            File file = ImagePicker.getFile(data);
//
//
//            //You can also get File Path from intent
//            String filePath = ImagePicker.getFilePath(data);
        }
        else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ""+ImagePicker.RESULT_ERROR, Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage(Uri fileUri) {

        name = etItemName.getText().toString();

        if (fileUri != null){
            String someFilepath = String.valueOf(fileUri);
            String extension = someFilepath.substring(someFilepath.lastIndexOf("."));

            StorageReference riversRef = storageReference.child("images/Restaurants/Items" + "." + extension+ " " + name +" "+ resName);

            riversRef.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Log.d("Image", "on Success");

                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();

                    String sdownload_url = String.valueOf(downloadUrl);

                    setDetails(sdownload_url, name);
                }
            });
        }
        else {
            dialog.dismiss();
            Snackbar.make(findViewById(android.R.id.content), "Please fill all the fields.", Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
        }
    }

    private void setDetails(String url, String name) {

        String price = etItemPrice.getText().toString().trim();
        String quantity = etItemQuantity.getText().toString().trim();
        String description = etItemDes.getText().toString().trim();
        String from = tvFrom.getText().toString().replace("from","");
        String to = tvTo.getText().toString().replace("from","");;

        if (TextUtils.isEmpty(price)
                || TextUtils.isEmpty(from)
                || TextUtils.isEmpty(to)
                || TextUtils.isEmpty(description)){

            dialog.dismiss();

            Snackbar.make(findViewById(android.R.id.content), "Please fill all the fields.", Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
        }
        else {
            HashMap<String, Object> newItem = new HashMap<>();
            newItem.put("price",price);
            newItem.put("imageUri",url);
            newItem.put("from", from);
            newItem.put("to", to);
            newItem.put("available",isAvailable);
            newItem.put("category",selectedCategory);
            newItem.put("description",description);
            newItem.put("quantity",quantity);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items").document(name)
                    .set(newItem, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    dialog.dismiss();

                    Snackbar.make(findViewById(android.R.id.content), "You have added one product named "+name, Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

                    android.os.Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(AddItem.this, Items.class);
                            intent.putExtra("resName", resName);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }, 1000);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                }
            });
        }
    }

    private String getDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long time = System.currentTimeMillis();
        calendar.setTimeInMillis(time);

        //dd=day, MM=month, yyyy=year, hh=hour, mm=minute, ss=second.

        String date = DateFormat.format("dd-MM-yyyy hh-mm",calendar).toString();

        return date;
    }

}