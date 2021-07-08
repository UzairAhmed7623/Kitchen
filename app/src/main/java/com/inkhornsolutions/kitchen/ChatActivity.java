package com.inkhornsolutions.kitchen;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.inkhornsolutions.kitchen.adapters.ChatAdapter;
import com.inkhornsolutions.kitchen.modelclasses.Chat;
import com.inkhornsolutions.kitchen.modelclasses.ChatList;

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
    private List<Chat> msg = new ArrayList<>();
    private ChatAdapter messagesAdapter;
    private String myID, chatRoom;

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

        setSupportActionBar(toolbarChat);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        rvMessages.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(linearLayoutManager);

        myID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        chatRoom = "igCh7xT4IVcrnfKBjR4W5hCo8jK2"+myID;

        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = etMessage.getText().toString().trim();

                if (!TextUtils.isEmpty(msg)){

                    SendMessages(msg);
                }
                else {
                    Snackbar.make(findViewById(android.R.id.content), "You cannot send empty message!", Snackbar.LENGTH_SHORT).setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.myColor)).show();
                }
                etMessage.setText("");
            }
        });

        readMessages();
    }

    private void SendMessages(String msg) {

        Long timeStamp = System.currentTimeMillis()/1000;

        Chat chat = new Chat(msg,getDate(timeStamp),myID,"igCh7xT4IVcrnfKBjR4W5hCo8jK2");

        Map<String, Object> map = new HashMap<>();
        map.put("messages", FieldValue.arrayUnion(chat));

        firebaseFirestore.collection("Chats").document(chatRoom).set(map, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.w("TAG", "Document created Successfully");

            }
        });
        firebaseFirestore.collection("Chats").document(chatRoom).set(map, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.w("TAG", "Document created Successfully");

            }
        });
    }

    private void readMessages(){

        firebaseFirestore.collection("Chats").document(chatRoom)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException error) {
                        msg.clear();

                        if (error != null) {
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            msg = Objects.requireNonNull(documentSnapshot.toObject(ChatList.class)).messages;

                            Log.d("TAG", msg.get(0).getMessage());

                            messagesAdapter = new ChatAdapter(ChatActivity.this, msg);
                            rvMessages.setAdapter(messagesAdapter);
                        }
                        else {
                            Toast.makeText(ChatActivity.this, "Chat is empty!", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return false;
    }
}