package com.inkhornsolutions.kitchen;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inkhornsolutions.kitchen.adapters.ItemsAdapter;
import com.inkhornsolutions.kitchen.modelclasses.ItemsModelClass;

import java.util.ArrayList;
import java.util.Objects;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class Items extends AppCompatActivity {

    private TextView toolbarItems;
    private RecyclerView rvItems;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private WaveSwipeRefreshLayout layoutItems;
    private final ArrayList<ItemsModelClass> items = new ArrayList<>();
    private final Paint p = new Paint();
    private String resName = "";
    private ItemsAdapter adapter;
    private MaterialButton backButton, btnAddItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbarItems = (TextView) findViewById(R.id.toolbar);
        backButton = (MaterialButton) findViewById(R.id.backButton);
        btnAddItem = (MaterialButton) findViewById(R.id.btnAddItem);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Items.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("resName", resName);
                startActivity(intent);
            }
        });

        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Items.this, AddItem.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("resName", resName);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        resName = getIntent().getStringExtra("resName");

        layoutItems = (WaveSwipeRefreshLayout) findViewById(R.id.layoutItems);

        rvItems = (RecyclerView) findViewById(R.id.rvItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ItemsAdapter(this, items);

        loadRestaurant(resName);

        layoutItems.setColorSchemeColors(Color.WHITE, Color.WHITE);
        layoutItems.setWaveColor(getColor(R.color.myColor));
        layoutItems.setMaxDropHeight(750);
        layoutItems.setMinimumHeight(750);
//        layoutOrder.setWaveColor(0xFF000000+new Random().nextInt(0xFFFFFF)); // Random color assign

        layoutItems.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                loadRestaurant(resName);
            }
        });
    }

    private void loadRestaurant(String resName) {

        firebaseFirestore.collection("Restaurants").document(resName).collection("Items")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    boolean isEmpty = task.getResult().isEmpty();

                    if (!isEmpty) {

                        items.clear();

                        String itemName = null;
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (documentSnapshot.exists()) {
                                itemName = documentSnapshot.getId();
                                String imageUri = documentSnapshot.getString("imageUri");
                                String price = documentSnapshot.getString("price");
                                String available = documentSnapshot.getString("available");
                                String from = documentSnapshot.getString("from");
                                String to = documentSnapshot.getString("to");
                                String description = documentSnapshot.getString("description");

                                Log.d("TAG", itemName + " " + imageUri + " " + price + " " + available + " " + from + " " + to);

                                ItemsModelClass itemsModelClass = new ItemsModelClass();
                                itemsModelClass.setResName(resName);
                                itemsModelClass.setItemName(itemName);
                                itemsModelClass.setImage(imageUri);
                                itemsModelClass.setPrice(price);
                                itemsModelClass.setAvailability(available);
                                if (!TextUtils.isEmpty(from) || !TextUtils.isEmpty(to)) {
                                    itemsModelClass.setSchedule(from + " " + "to" + " " + to);
                                }
                                itemsModelClass.setDescription(description);

                                items.add(itemsModelClass);
                            }
                        }
                        rvItems.setAdapter(adapter);
                        layoutItems.setRefreshing(false);
                        initSwipe(itemName);
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "No data found!", Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
            }
        });
    }

    private void initSwipe(String itemName) {
        ItemTouchHelper.SimpleCallback itemTouchCallBack = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT) {

                    AlertDialog.Builder dialog = new AlertDialog.Builder(Items.this);
                    dialog.setTitle("Confirm");
                    dialog.setMessage("Do you want to delete this item?");
                    dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            firebaseFirestore.collection("Restaurants").document(resName).collection("Items").document(itemName)
                                    .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Snackbar.make(findViewById(android.R.id.content), "Deleted Successfully!", Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();

                                    items.remove(position);
                                    adapter.notifyDataSetChanged();
                                    dialog.dismiss();
                                }
                            });
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            adapter.notifyDataSetChanged();
                        }
                    }).show();
                    adapter.notifyDataSetChanged();
                } else {

                    Intent intent = new Intent(Items.this, ItemProperties.class);
                    intent.putExtra("restaurant", resName);
                    intent.putExtra("itemName", itemName);
                    startActivity(intent);

                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if (dX > 0) {
                        p.setColor(Color.parseColor("#388E3C"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        c.drawRect(background, p);
                        Drawable drawable = ContextCompat.getDrawable(Items.this, R.drawable.swipe_edit);
                        icon = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(icon);
                        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        drawable.draw(canvas);
                        c.drawBitmap(icon, (float) (itemView.getLeft() + 1.2 * width), (float) (itemView.getTop() + 1.1 * width), p);
                    } else {
                        p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background, p);
                        Drawable drawable = ContextCompat.getDrawable(Items.this, R.drawable.swipe_delete);
                        icon = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(icon);
                        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        drawable.draw(canvas);
                        c.drawBitmap(icon, (float) (itemView.getRight() - 1.5 * width), (float) (itemView.getTop() + 1.1 * width), p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallBack);
        itemTouchHelper.attachToRecyclerView(rvItems);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addItem) {

            Intent intent = new Intent(Items.this, AddItem.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("resName", resName);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

}