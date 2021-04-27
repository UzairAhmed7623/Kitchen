package com.inkhornsolutions.kitchen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ItemProperties extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private EditText etItemName, etItemPrice, etAvailable;
    private TextView tvFrom, tvTo;
    private int t1hour, t1minute, t2hour, t2minute;
    private ImageView etItemImage;
    private Button btnDone, btnPickImage;
    private ProgressDialog dialog;
    private Uri fileUri;
    private String resName, itemName;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_properties);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        etItemName = (EditText) findViewById(R.id.etItemName);
        etItemPrice = (EditText) findViewById(R.id.etItemPrice);
        etAvailable = (EditText) findViewById(R.id.etAvailable);
        btnDone = (Button) findViewById(R.id.btnDone);
        tvFrom = (TextView) findViewById(R.id.tvFrom);
        tvTo = (TextView) findViewById(R.id.tvTo);
        etItemImage = (ImageView) findViewById(R.id.etItemImage);

        btnDone = (Button) findViewById(R.id.btnDone);
        btnPickImage = (Button) findViewById(R.id.btnPickImage);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Update product");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        resName = getIntent().getStringExtra("restaurant");
        itemName = getIntent().getStringExtra("itemName");

        Log.d("TAG", resName+" "+itemName);

        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImagePicker.Companion.with(ItemProperties.this)
                        .crop(3f, 2f)	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .start(1002);
            }
        });

        tvFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(ItemProperties.this, new TimePickerDialog.OnTimeSetListener() {
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
                TimePickerDialog timePickerDialog = new TimePickerDialog(ItemProperties.this, new TimePickerDialog.OnTimeSetListener() {
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

                dialog = new ProgressDialog(ItemProperties.this);
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

        if (fileUri != null){
            String someFilepath = String.valueOf(fileUri);
            String extension = someFilepath.substring(someFilepath.lastIndexOf("."));

            StorageReference riversRef = storageReference.child("images/Restaurants/Items" + "." + extension+ " " + itemName +" "+ resName);

            riversRef.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Log.d("Image", "on Success");

                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();

                    String sdownload_url = String.valueOf(downloadUrl);

                    firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                            .document(itemName).update("imageUri", sdownload_url);

                    setDetails(itemName);

                }
            });
        }
        else {
            setDetails(itemName);
        }
    }

    private void setDetails(String name) {

        String price = etItemPrice.getText().toString().trim().replace("PKR", "");
        String availability = etAvailable.getText().toString();
        String from = tvFrom.getText().toString().replace("from","");
        String to = tvTo.getText().toString().replace("from","");;

        if (TextUtils.isEmpty(price)){
            Log.d("TAG", "Empty");
        }
        else {
            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update("price", price);
            Snackbar.make(findViewById(android.R.id.content), "Price successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();
            goBack();
        }
        if (TextUtils.isEmpty(availability)){
            Log.d("TAG", "Empty");
        }
        else {
            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update("available", availability);
            Snackbar.make(findViewById(android.R.id.content), "Availability successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();
            goBack();
        }
        if (TextUtils.isEmpty(from)){
            Log.d("TAG", "Empty");
        }
        else {
            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update("from", from);
            Snackbar.make(findViewById(android.R.id.content), "Begin time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();
            goBack();
        }
        if (TextUtils.isEmpty(to)){
            Log.d("TAG", "Empty");
        }
        else {
            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update("to", to);
            Snackbar.make(findViewById(android.R.id.content), "End time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();
            goBack();
        }
        if (TextUtils.isEmpty(price)
                & TextUtils.isEmpty(availability)
                & TextUtils.isEmpty(from)
                & TextUtils.isEmpty(to)){

            dialog.dismiss();

            Snackbar.make(findViewById(android.R.id.content), "Please fill at least one field.", Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
        }
    }

    private void goBack(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(ItemProperties.this, MainActivity.class);
                intent.putExtra("resName", resName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }, 1000);
    }

}