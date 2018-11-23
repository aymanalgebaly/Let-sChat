package com.example.android.letschat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.letschat.adapter.MessagesAdapter;
import com.example.android.letschat.model.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String userChat;
    private Toolbar userToolbar;
    private DatabaseReference mRootRef;
    private String userName;
    private TextView titleNameView,lastSeen;
    private CircleImageView frimage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private ImageButton sendChatBtn,addChatBtn;
    private EditText sendMessage;
    private RecyclerView recyclerView;
    private final List<Messages>messagesConv = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private MessagesAdapter adapter;
    private static final int LIMITE_MESSAGES = 10;
    private int current_page = 1;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int itemPos = 0;
    private String lastKeyMessage = "";
    private String prevKey = "" ;
    private static final int gallery_pick = 1;
    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ativity);

        userChat = getIntent().getStringExtra("user_id");
        userName = getIntent().getStringExtra("user_name");

        userToolbar = findViewById(R.id.toolbarlayout);
        setSupportActionBar(userToolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();


        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle(userName);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        adapter = new MessagesAdapter(messagesConv);

        recyclerView = findViewById(R.id.chatList);

        frimage = findViewById(R.id.chatFriendImage);
        titleNameView = findViewById(R.id.friendChatName);
        lastSeen = findViewById(R.id.friendChatLastSeen);

        titleNameView.setText(userName);

        sendMessage = findViewById(R.id.sendMessage);
        sendChatBtn = findViewById(R.id.sendChat);
        addChatBtn = findViewById(R.id.addChat);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        layoutManager.setStackFromEnd(true);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        loadMessages();

        mRootRef.child("users").child(userChat).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                if (online.equals("true")){
                    lastSeen.setText("online");
                }else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastTimeSeen = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());

                    lastSeen.setText(lastTimeSeen);
                }
                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(frimage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("chat").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(userChat)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("chat/" + currentUserId + "/" + userChat, chatAddMap);
                    chatUserMap.put("chat/" + userChat + "/" + currentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                          //  Log.i( "Chat LOG: ",databaseError.getMessage().toString());
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageText();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                current_page ++;
                itemPos = 0;
                loadMoreMessages();
            }
        });

        addChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),gallery_pick);
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == gallery_pick && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + currentUserId + "/" + userChat;
            final String chat_user_ref = "messages/" + userChat + "/" + currentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(currentUserId).child(userChat).push();

            final String push_id = user_message_push.getKey();


            StorageReference filepath = mImageStorage.child("message_images").child( push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        String download_url = task.getResult().getDownloadUrl().toString();


                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", currentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        sendMessage.setText("");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null){

                                    //Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });


                    }
                }

            });
        }

    }






    private void loadMoreMessages() {
        DatabaseReference reference =mRootRef.child("messages").child(currentUserId).child(userChat);
        Query messagesQuery =reference.orderByKey().endAt(lastKeyMessage).limitToFirst(10);
        messagesQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                String lastMessage = dataSnapshot.getKey();

                if (!prevKey.equals(lastMessage)){
                    messagesConv.add(itemPos++,messages);

                }else {
                    prevKey = lastKeyMessage;
                }

                if (itemPos == 1){
                    lastKeyMessage = lastMessage;
                }


                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messagesConv.size() -1);

                swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {

        DatabaseReference reference =mRootRef.child("messages").child(currentUserId).child(userChat);
        Query messagesQuery =reference.limitToLast(current_page = LIMITE_MESSAGES);

        messagesQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                itemPos ++;
                if (itemPos == 1){
                    String lastMessage = dataSnapshot.getKey();
                    lastKeyMessage = lastMessage;
                    prevKey = lastMessage;
                }

                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesConv.add(messages);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messagesConv.size() -1);

                swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessageText() {
        String message = sendMessage.getText().toString();

        if (!TextUtils.isEmpty(message)){

            String current_user_ref = "messages/" + currentUserId + "/" + userChat;
            String chat_user_ref = "messages/" + userChat + "/" + currentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(currentUserId)
                    .child(userChat).push();

            String user_push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from",currentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + user_push_id,messageMap);
            messageUserMap.put(chat_user_ref + "/" + user_push_id,messageMap);

            sendMessage.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                  //  Log.i( "Chat LOG: ",databaseError.getMessage().toString());

                }
            });

        }


    }

}

