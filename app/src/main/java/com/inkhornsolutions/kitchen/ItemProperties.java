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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ItemProperties extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private EditText etItemName, etItemPrice, etItemDes, etItemQuantity;
    private MaterialButton btnFrom, btnTo, btnDone, btnPickImage;
    private int t1hour, t1minute, t2hour, t2minute;
    private CircleImageView etItemImage;
    private ProgressDialog dialog;
    private Uri fileUri;
    private String resName, itemName;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private TextView toolbar;
    private TextView tvAvailable, tvNotAvailable;
    private SwitchMaterial switchAvailable;
    private String isAvailable = "";
    private MaterialButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_properties);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        etItemName = (EditText) findViewById(R.id.etItemName);
        etItemQuantity = (EditText) findViewById(R.id.etItemQuantity);
        etItemDes = (EditText) findViewById(R.id.etItemDes);
        etItemPrice = (EditText) findViewById(R.id.etItemPrice);
        btnDone = (MaterialButton) findViewById(R.id.btnDone);
        btnFrom = (MaterialButton) findViewById(R.id.tvFrom);
        btnTo = (MaterialButton) findViewById(R.id.tvTo);
        etItemImage = (CircleImageView) findViewById(R.id.etItemImage);
        tvAvailable = (TextView) findViewById(R.id.tvAvailable);
        tvNotAvailable = (TextView) findViewById(R.id.tvNotAvailable);
        switchAvailable = (SwitchMaterial) findViewById(R.id.switchAvailable);
        backButton = (MaterialButton) findViewById(R.id.backButton);
        btnPickImage = (MaterialButton) findViewById(R.id.btnPickImage);

        toolbar = (TextView) findViewById(R.id.toolbar);

        resName = getIntent().getStringExtra("restaurant");
        itemName = getIntent().getStringExtra("itemName");

        Log.d("TAG", resName + " " + itemName);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ItemProperties.this, Items.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("resName", resName);
                startActivity(intent);
            }
        });

        switchAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tvAvailable.setVisibility(View.VISIBLE);
                    tvNotAvailable.setVisibility(View.GONE);
                    isAvailable = "yes";
                } else {
                    tvNotAvailable.setVisibility(View.VISIBLE);
                    tvAvailable.setVisibility(View.GONE);

                    isAvailable = "no";
                }
            }
        });

        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImagePicker.Companion.with(ItemProperties.this)
                        .crop(3f, 2f)                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .start(1002);
            }
        });

        btnFrom.setOnClickListener(new View.OnClickListener() {
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
                            SimpleDateFormat f12Hours = new SimpleDateFormat("kk:mm");

                            btnFrom.setText(f12Hours.format(date));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, 12, 0, false);

                timePickerDialog.updateTime(t1hour, t1minute);
                timePickerDialog.show();
            }
        });

        btnTo.setOnClickListener(new View.OnClickListener() {
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
                            SimpleDateFormat f12Hours = new SimpleDateFormat("kk:mm");

                            btnTo.setText(f12Hours.format(date));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, 12, 0, false);

                timePickerDialog.updateTime(t2hour, t2minute);
                timePickerDialog.show();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog = new ProgressDialog(ItemProperties.this);
                dialog.setTitle("Please wait...");
                dialog.setMessage("Product is updating");
                dialog.setCancelable(false);
                dialog.show();

                uploadImage(fileUri);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1002 || resultCode == RESULT_OK && data != null && data.getData() != null) {
            //Image Uri will not be null for RESULT_OK
            fileUri = data.getData();
            etItemImage.setImageURI(fileUri);

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

    private void uploadImage(Uri fileUri) {

        if (fileUri != null) {
            String someFilepath = String.valueOf(fileUri);
            String extension = someFilepath.substring(someFilepath.lastIndexOf("."));

            StorageReference riversRef = storageReference.child("images/Restaurants/Items" + "." + extension + " " + itemName + " " + resName);

            riversRef.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Log.d("Image", "on Success");

                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
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
        String from = btnFrom.getText().toString().replace("from", "");
        String to = btnTo.getText().toString().replace("from", "");
        String quantity = etItemQuantity.getText().toString().trim();
        String description = etItemDes.getText().toString().trim();

        if (!TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update("price", price);
            Snackbar.make(findViewById(android.R.id.content), "Price successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();

        } else if (TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update("available", isAvailable);
            Snackbar.make(findViewById(android.R.id.content), "Availability successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();

        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update("from", from);
            Snackbar.make(findViewById(android.R.id.content), "Starting time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();

        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update("to", to);
            Snackbar.make(findViewById(android.R.id.content), "End time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();

        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("quantity", quantity);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Quantity successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("description", description);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Description successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (!TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("price", price);
            updated.put("available", isAvailable);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Price and availability successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (!TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("price", price);
            updated.put("from", from);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Price and started time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (!TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("price", price);
            updated.put("to", to);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Price and end time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (!TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("price", price);
            updated.put("quantity", quantity);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Price and quantity successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (!TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("price", price);
            updated.put("description", description);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Price and description successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("available", isAvailable);
            updated.put("from", from);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability and starting time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("available", isAvailable);
            updated.put("to", to);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability and end time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("available", isAvailable);
            updated.put("quantity", quantity);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability and quantity successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("available", isAvailable);
            updated.put("description", description);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability and description successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("from", from);
            updated.put("to", to);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Starting time and end time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("from", from);
            updated.put("quantity", quantity);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Starting time and quantity successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("from", from);
            updated.put("description", description);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Starting time and description successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("to", to);
            updated.put("quantity", quantity);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "End time and quantity successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("to", to);
            updated.put("description", description);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Description and end time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("quantity", quantity);
            updated.put("description", description);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Quantity and description successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (!TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("price", price);
            updated.put("available", isAvailable);
            updated.put("from", from);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability, price and starting time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (!TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("price", price);
            updated.put("available", isAvailable);
            updated.put("to", to);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability, price and end time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (!TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("price", price);
            updated.put("available", isAvailable);
            updated.put("quantity", quantity);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability, price and quantity successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (!TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("price", price);
            updated.put("available", isAvailable);
            updated.put("description", description);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability, Price and description successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("available", isAvailable);
            updated.put("from", from);
            updated.put("to", to);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability, starting and end time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("from", from);
            updated.put("available", isAvailable);
            updated.put("quantity", quantity);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability, starting time and quantity successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("from", from);
            updated.put("available", isAvailable);
            updated.put("description", description);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability, description and starting time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("from", from);
            updated.put("quantity", quantity);
            updated.put("to", to);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Starting time, quantity and end time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("from", from);
            updated.put("description", description);
            updated.put("to", to);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Starting time, description and end time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("quantity", quantity);
            updated.put("description", description);
            updated.put("to", to);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "End time, description and quantity successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (!TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("price", price);
            updated.put("available", isAvailable);
            updated.put("to", to);
            updated.put("from", from);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability, Price, starting and end time successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (!TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("price", price);
            updated.put("available", isAvailable);
            updated.put("from", from);
            updated.put("quantity", quantity);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability, Price, starting time and quantity successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (!TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("price", price);
            updated.put("available", isAvailable);
            updated.put("from", from);
            updated.put("description", description);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability, Price, starting time and description successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("available", isAvailable);
            updated.put("from", from);
            updated.put("to", to);
            updated.put("quantity", quantity);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability, starting and end time and quantity successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("available", isAvailable);
            updated.put("from", from);
            updated.put("to", to);
            updated.put("description", description);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability, starting and end time and description successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("from", from);
            updated.put("to", to);
            updated.put("quantity", quantity);
            updated.put("description", description);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Starting and end time, quantity and description successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (!TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("price", price);
            updated.put("available", isAvailable);
            updated.put("from", from);
            updated.put("to", to);
            updated.put("quantity", quantity);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability, Price, starting and end time and quantity successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("available", isAvailable);
            updated.put("from", from);
            updated.put("to", to);
            updated.put("quantity", quantity);
            updated.put("description", description);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "Availability, starting and end time, description and quantity successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (!TextUtils.isEmpty(price)
                && !TextUtils.isEmpty(isAvailable)
                && !TextUtils.isEmpty(from)
                && !TextUtils.isEmpty(to)
                && !TextUtils.isEmpty(quantity)
                && !TextUtils.isEmpty(description)) {

            HashMap<String, Object> updated = new HashMap<>();
            updated.put("price", price);
            updated.put("available", isAvailable);
            updated.put("from", from);
            updated.put("to", to);
            updated.put("quantity", quantity);
            updated.put("description", description);

            firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                    .document(itemName).update(updated);
            Snackbar.make(findViewById(android.R.id.content), "All Fields are successfully updated!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();

            dialog.dismiss();

            goBack();
        } else if (TextUtils.isEmpty(price)
                && TextUtils.isEmpty(isAvailable)
                && TextUtils.isEmpty(from)
                && TextUtils.isEmpty(to)
                && TextUtils.isEmpty(quantity)
                && TextUtils.isEmpty(description)) {

            dialog.dismiss();

            Snackbar.make(findViewById(android.R.id.content), "Please update at least one field!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).setTextColor(Color.WHITE).show();
        }
    }

    private void goBack() {
        dialog.dismiss();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(ItemProperties.this, Items.class);
                intent.putExtra("resName", resName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }, 1000);
    }
}