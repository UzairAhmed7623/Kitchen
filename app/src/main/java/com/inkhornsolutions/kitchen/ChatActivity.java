package com.inkhornsolutions.kitchen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.inkhornsolutions.kitchen.adapters.MessagesAdapter;
import com.inkhornsolutions.kitchen.modelclasses.Chat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbarChat;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private List<String> msg = new ArrayList<>();
    private List<String> time = new ArrayList<>();
    private MessagesAdapter messagesAdapter;
    Map<String, String> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbarChat = (Toolbar) findViewById(R.id.toolbarChat);
        rvMessages = (RecyclerView) findViewById(R.id.rvMessages);
        etMessage = (EditText) findViewById(R.id.etMessage);
        ImageButton ibSend = (ImageButton) findViewById(R.id.ibSend);

        rvMessages.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(linearLayoutManager);


        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = etMessage.getText().toString().trim();

                if (!TextUtils.isEmpty(msg)){

                    uploadMessages(msg);
                }
                else {
                    Snackbar.make(findViewById(android.R.id.content), "You cannot send empty message!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                }

                etMessage.setText("");
            }
        });

        readMessages();

    }

    private void uploadMessages(String msg) {

        Long timeStamp = System.currentTimeMillis()/1000;

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(Objects.requireNonNull(firebaseAuth.getUid()));

        Map<String, Object> messages = new HashMap<>();
        messages.put("message", msg);
        messages.put("time", getDate(timeStamp));

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){

                        documentReference.update("messages", FieldValue.arrayUnion(messages));
                        Log.w("TAG", "Document updated successfully!");

                    }
                    else {
                        firebaseFirestore.collection("Users").document(firebaseAuth.getUid()).set(messages, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.w("TAG", "Document created Successfully");

                            }
                        });
                    }
                }

            }
        });
    }

    private void readMessages(){

        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException error) {
                        msg.clear();
                        time.clear();

                        if (documentSnapshot.exists()){

                            Chat chat = documentSnapshot.toObject(Chat.class);


                            Log.d("message1", ""+chat);

                            for (int i=0; i<chat.getMessages().size(); i++){
                                msg.add(chat.getMessages().get(i).get("message").toString());
                                time.add(chat.getMessages().get(i).get("time").toString());
                            }


                            Log.d("message2", ""+msg);

                            messagesAdapter = new MessagesAdapter(ChatActivity.this, msg, time);
                            rvMessages.setAdapter(messagesAdapter);
                        }
                    }
                });
    }

    private String getDate(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timestamp * 1000);
        String date = DateFormat.format("dd-MM-yyyy hh-mm-ss", calendar).toString();
        return date;
    }

}