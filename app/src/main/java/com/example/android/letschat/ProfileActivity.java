package com.example.android.letschat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

//import com.example.android.chatchat.adapter.UsersAdapter;

public class ProfileActivity extends AppCompatActivity {


    TextView profTextName, profTextStatu, profTextFr;
    private CircleImageView profImg;
    private Button friendRequest, declinebtn;
    private FirebaseUser current_user;
    private DatabaseReference mFriendReDatabase;
    private DatabaseReference notification;
    private DatabaseReference reference;
    private DatabaseReference mRootRef;
    private String current_state;
    private String user_id;
    private DatabaseReference mUserReference;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profTextName = findViewById(R.id.profileName);
        profImg = findViewById(R.id.profileImg);
        profTextFr = findViewById(R.id.numOfFriends);
        profTextStatu = findViewById(R.id.profileStatus);
        friendRequest = findViewById(R.id.sendRequest);
        declinebtn = findViewById(R.id.decline);



            final String user_id = getIntent().getStringExtra("user_id");

            mRootRef = FirebaseDatabase.getInstance().getReference();

            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
            mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
            mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
            notification = FirebaseDatabase.getInstance().getReference().child("notifications");
            current_user = FirebaseAuth.getInstance().getCurrentUser();



            current_state = "not_friends";

            declinebtn.setVisibility(View.INVISIBLE);
            declinebtn.setEnabled(false);



            mUsersDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String display_name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();

                    profTextName.setText(display_name);
                    profTextStatu.setText(status);

                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(profImg);

                    if(current_user.getUid().equals(user_id)){

                        declinebtn.setEnabled(false);
                        declinebtn.setVisibility(View.INVISIBLE);

                        friendRequest.setEnabled(false);
                        friendRequest.setVisibility(View.INVISIBLE);

                    }


                    //--------------- FRIENDS LIST / REQUEST FEATURE -----

                    mFriendReqDatabase.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChild(user_id)){

                                String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                                if(req_type.equals("received")){

                                    current_state = "req_received";
                                    friendRequest.setText("Accept Friend Request");

                                    declinebtn.setVisibility(View.VISIBLE);
                                    declinebtn.setEnabled(true);


                                } else if(req_type.equals("sent")) {

                                    current_state = "req_sent";
                                    friendRequest.setText("Cancel Friend Request");

                                    declinebtn.setVisibility(View.INVISIBLE);
                                    declinebtn.setEnabled(false);

                                }



                            } else {


                                mFriendDatabase.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.hasChild(user_id)){

                                            current_state = "friends";
                                            friendRequest.setText("Unfriend this Person");

                                            declinebtn.setVisibility(View.INVISIBLE);
                                            declinebtn.setEnabled(false);

                                        }


                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {


                                    }
                                });

                            }



                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            friendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    friendRequest.setEnabled(false);

                    // --------------- NOT FRIENDS STATE ------------

                    if(current_state.equals("not_friends")){


                        DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
                        String newNotificationId = newNotificationref.getKey();

                        HashMap<String, String> notificationData = new HashMap<>();
                        notificationData.put("from", current_user.getUid());
                        notificationData.put("type", "request");

                        Map requestMap = new HashMap();
                        requestMap.put("Friend_req/" + current_user.getUid() + "/" + user_id + "/request_type", "sent");
                        requestMap.put("Friend_req/" + user_id + "/" + current_user.getUid() + "/request_type", "received");
                        requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                        mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null){

                                    Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                                } else {

                                    current_state = "req_sent";
                                    friendRequest.setText("Cancel Friend Request");

                                }

                                friendRequest.setEnabled(true);


                            }
                        });

                    }


                    // - -------------- CANCEL REQUEST STATE ------------

                    if(current_state.equals("req_sent")){

                        mFriendReqDatabase.child(current_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                mFriendReqDatabase.child(user_id).child(current_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {


                                        friendRequest.setEnabled(true);
                                        current_state = "not_friends";
                                        friendRequest.setText("Send Friend Request");

                                        declinebtn.setVisibility(View.INVISIBLE);
                                        declinebtn.setEnabled(false);


                                    }
                                });

                            }
                        });

                    }


                    // ------------ REQ RECEIVED STATE ----------

                    if(current_state.equals("req_received")){

                        final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                        Map friendsMap = new HashMap();
                        friendsMap.put("Friends/" + current_user.getUid() + "/" + user_id + "/date", currentDate);
                        friendsMap.put("Friends/" + user_id + "/"  + current_user.getUid() + "/date", currentDate);


                        friendsMap.put("Friend_req/" + current_user.getUid() + "/" + user_id, null);
                        friendsMap.put("Friend_req/" + user_id + "/" + current_user.getUid(), null);


                        mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                                if(databaseError == null){

                                    friendRequest.setEnabled(true);
                                    current_state = "friends";
                                    friendRequest.setText("Unfriend this Person");

                                    declinebtn.setVisibility(View.INVISIBLE);
                                    declinebtn.setEnabled(false);

                                } else {

                                    String error = databaseError.getMessage();

                                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                                }

                            }
                        });

                    }


                    // ------------ UNFRIENDS ---------

                    if(current_state.equals("friends")){

                        Map unfriendMap = new HashMap();
                        unfriendMap.put("Friends/" + current_user.getUid() + "/" + user_id, null);
                        unfriendMap.put("Friends/" + user_id + "/" + current_user.getUid(), null);

                        mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                                if(databaseError == null){

                                    current_state = "not_friends";
                                    friendRequest.setText("Send Friend Request");

                                    declinebtn.setVisibility(View.INVISIBLE);
                                    declinebtn.setEnabled(false);

                                } else {

                                    String error = databaseError.getMessage();

                                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                                }

                                friendRequest.setEnabled(true);

                            }
                        });

                    }


                }
            });


        }


    }


















//        final String user_id = getIntent().getStringExtra("user_id");
//        mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
//
//        reference = FirebaseDatabase.getInstance().getReference().child("friends");
//        notification = FirebaseDatabase.getInstance().getReference().child("notification");
//
//        current_user = FirebaseAuth.getInstance().getCurrentUser();
//
//        current_state = "not_friend";
//
//        mRootRef = FirebaseDatabase.getInstance().getReference();
//        mFriendReDatabase = FirebaseDatabase.getInstance().getReference().child("friend_req");
//
//
//        mUserReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                String user_name = dataSnapshot.child("name").getValue().toString();
//                String user_status = dataSnapshot.child("status").getValue().toString();
//                String user_image = dataSnapshot.child("image").getValue().toString();
//
//                profTextName.setText(user_name);
//                profTextStatu.setText(user_status);
//
//                Picasso.get().load(user_image).placeholder(R.drawable.default_avatar).into(profImg);
//
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        friendRequest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (current_state.equals("not_friends")){
//                    mFriendReDatabase.child(current_user.getUid()).child(user_id).child("request_type").setValue("sent")
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()){
//                                        mFriendReDatabase.child(user_id).child(current_user.getUid()).child("request_type")
//                                                .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void aVoid) {
//                                                Toast.makeText(ProfileActivity.this, "ok", Toast.LENGTH_SHORT).show();
//                                            }
//                                        });
//                                    }
//                                    else {
//                                        Toast.makeText(ProfileActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            });
//                }
//            }
//        });
//
//
////        Intent intent = getIntent();
////        if (intent != null && intent.hasExtra("model")) {
////            UsersModel usersModel = getIntent().getParcelableExtra("model");
////            user_id = getIntent().getStringExtra("user_id");
////
////
////
////            profTextName.setText(usersModel.getName());
////            profTextStatu.setText(usersModel.getStatus());
//
//
////            mFriendReDatabase.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
////                @Override
////                public void onDataChange(DataSnapshot dataSnapshot) {
////                    if (dataSnapshot.hasChild(user_id)){
////                        String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
////
////                        if (req_type.equals("received")){
////                            current_state = "req_received";
////                            friendRequest.setText("Accept Friend Request");
////
////                            declinebtn.setVisibility(View.VISIBLE);
////                            declinebtn.setEnabled(true);
////                        }
////                        else if (req_type.equals("sent")){
////                            current_state = "req_sent";
////                            friendRequest.setText("Cancel Friend Request");
////
////                            declinebtn.setVisibility(View.INVISIBLE);
////                            declinebtn.setEnabled(false);
////                        }
////                    }else {
////                        reference.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
////                            @Override
////                            public void onDataChange(DataSnapshot dataSnapshot) {
////                                if (dataSnapshot.hasChild(user_id)){
////                                    current_state = "friends";
////                                    friendRequest.setText("unfriend this person");
////
////                                    declinebtn.setVisibility(View.INVISIBLE);
////                                    declinebtn.setEnabled(false);
////                                }
////                            }
////
////                            @Override
////                            public void onCancelled(DatabaseError databaseError) {
////
////                            }
////                        });
////                    }
////                }
////
////                @Override
////                public void onCancelled(DatabaseError databaseError) {
////
////                }
////            });
////
////
//        }
//
////        current_state = "not_friend";
//
//
//
////        public void sendRequest (View view){
////            friendRequest.setEnabled(false);
////            if (current_state.equals("not_friend")) {
//
//////            //<-------short code for send request friend-------->
//////
////////            Map requestMap = new HashMap();
////////            requestMap.put(current_user.getUid() + "/" + user_id + "request_type","sent");
////////            requestMap.put(user_id + "/" +current_user.getUid() + "request_type","received");
////////
////////            mFriendReDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
////////                @Override
////////                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
////////                    HashMap<String,String> notificationData = new HashMap<>();
////////                    notificationData.put("from",current_user.getUid());
////////                    notificationData.put("type","request");
////////
////////                    notification.child(user_id).push().setValue(notificationData)
////////                            .addOnSuccessListener(new OnSuccessListener<Void>() {
////////                                @Override
////////                                public void onSuccess(Void aVoid) {
////////                                    friendRequest.setEnabled(true);
////////                                    current_state = "req_sent";
////////                                    friendRequest.setText("Cancel Friend Request");
////////
////////                                    declinebtn.setVisibility(View.INVISIBLE);
////////                                    declinebtn.setEnabled(false);
////////                                }
////////                            });
////////
////////                }
////////            });
//
////                mFriendReDatabase.child(current_user.getUid()).child(user_id).child("request_type").setValue("sent")
//////                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//////                            @Override
//////                            public void onSuccess(Void aVoid) {
//////                                mFriendReDatabase.child(user_id).child(current_user.getUid()).child("request_type").setValue("received");
//////
//////                                HashMap<String, String> notificationData = new HashMap<>();
//////                                notificationData.put("from", current_user.getUid());
//////                                notificationData.put("type", "request");
//////
//////                                notification.child(user_id).push().setValue(notificationData)
//////                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//////                                            @Override
//////                                            public void onSuccess(Void aVoid) {
//////                                                friendRequest.setEnabled(true);
//////                                                current_state = "req_sent";
//////                                                friendRequest.setText("Cancel Friend Request");
//////
//////                                                declinebtn.setVisibility(View.INVISIBLE);
//////                                                declinebtn.setEnabled(false);
//////                                            }
//////                                        });
//////
//////
//////                            }
//////                        });
//////
//////            }
//////
//////            if (current_state.equals("req_sent")) {
//////
//////                mFriendReDatabase.child(current_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//////                    @Override
//////                    public void onSuccess(Void aVoid) {
//////                        mFriendReDatabase.child(user_id).child(current_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//////                            @Override
//////                            public void onSuccess(Void aVoid) {
//////                                friendRequest.setEnabled(true);
//////                                current_state = "not_friend";
//////                                friendRequest.setText("Send Friend Request");
//////
//////                                declinebtn.setVisibility(View.INVISIBLE);
//////                                declinebtn.setEnabled(false);
//////                            }
//////                        });
//////                    }
//////                });
//////            }
//////
//////            if (current_state.equals("req_received")) {
//////
//////                final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
//////
//////                reference.child(current_user.getUid()).child(user_id).setValue(currentDate)
//////                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//////                            @Override
//////                            public void onSuccess(Void aVoid) {
//////                                reference.child(user_id).child(current_user.getUid()).setValue(currentDate)
//////                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//////                                            @Override
//////                                            public void onSuccess(Void aVoid) {
//////                                                mFriendReDatabase.child(current_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//////                                                    @Override
//////                                                    public void onSuccess(Void aVoid) {
//////                                                        mFriendReDatabase.child(user_id).child(current_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//////                                                            @Override
//////                                                            public void onSuccess(Void aVoid) {
//////                                                                friendRequest.setEnabled(true);
//////                                                                current_state = "friends";
//////                                                                friendRequest.setText("unfriend this person");
//////
//////                                                                declinebtn.setVisibility(View.INVISIBLE);
//////                                                                declinebtn.setEnabled(false);
//////                                                            }
//////                                                        });
//////
//////                                                        if (current_state.equals("friends")) {
//////
//////                                                            Map unFriendMap = new HashMap();
//////                                                            unFriendMap.put("friends/" + current_user.getUid() + "/" + user_id, null);
//////                                                            unFriendMap.put("friends/" + user_id + "/" + current_user.getUid(), null);
//////
//////                                                            mRootRef.updateChildren(unFriendMap, new DatabaseReference.CompletionListener() {
//////                                                                @Override
//////                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//////                                                                    if (databaseError == null) {
//////                                                                        current_state = "not_friend";
//////                                                                        friendRequest.setText("Send Friend Request");
//////
//////                                                                        declinebtn.setVisibility(View.INVISIBLE);
//////                                                                        declinebtn.setEnabled(false);
//////                                                                    }
//////
//////                                                                    friendRequest.setEnabled(true);
//////
//////                                                                }
//////                                                            });
//////
////////////                                                mFriendReDatabase.child(current_user.getUid()).child(user_id).removeValue()
////////////                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
////////////                                                            @Override
////////////                                                            public void onSuccess(Void aVoid) {
////////////                                                                mFriendReDatabase.child(user_id).child(current_user.getUid()).removeValue()
////////////                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
////////////                                                                            @Override
////////////                                                                            public void onSuccess(Void aVoid) {
////////////                                                                                current_state = "not_friend";
////////////                                                                                friendRequest.setText("Send Friend Request");
////////////                                                                            }
////////////                                                                        });
////////////                                                            }
////////////                                                        });
//////                                                        }
//////
//////                                                    }
//////                                                });
//////                                            }
//////                                        });
//////                            }
//////                        });
////            }
//











