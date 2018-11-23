package com.example.android.letschat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.letschat.model.UsersModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

//import com.example.android.chatchat.adapter.UsersAdapter;

public class UsersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    //public UsersAdapter adapter;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseUser current_user;
    private StorageReference storageReference;
    private List<String> usersModels;
    private String userId;
    private FirebaseRecyclerAdapter<UsersModel, UsersViewHolder> adapter1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        firebaseDatabase = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


//        usersModels = new ArrayList<>();
//
//        setupRecycler();
//        viewUsers();


        toolbar = findViewById(R.id.toolbarlayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.users_rv);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = FirebaseDatabase.getInstance().getReference().child("users").limitToFirst(50);


        FirebaseRecyclerOptions<UsersModel> usersModelFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<UsersModel>()
                .setQuery(query, UsersModel.class).build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<UsersModel,UsersViewHolder>(usersModelFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull UsersModel model) {

                holder.setName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setImage(model.getThumb_image(),getApplicationContext());

                final String key = getRef(position).getKey();


                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(UsersActivity.this,ProfileActivity.class);
                        i.putExtra("user_id",key);
                        startActivity(i);
                    }
                });

            }



            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_list, parent, false);
                return new UsersViewHolder(v);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);

//        adapter1 = new FirebaseRecyclerAdapter<UsersModel, UsersViewHolder>(usersModelFirebaseRecyclerOptions) {
//            @Override
//            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull UsersModel model) {
//                holder.setName(model.getName());
//                String key = getRef(position).getKey();
//            }
//
//            @NonNull
//            @Override
//            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_list, parent, false);
//                return new UsersViewHolder(v);
//            }
//        };

//        recyclerView.setAdapter(adapter1);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {

            TextView userNameView = mView.findViewById(R.id.showName);
            userNameView.setText(name);
        }

        public void setStatus(String status) {
            TextView userStatusView = mView.findViewById(R.id.showStatus);
            userStatusView.setText(status);
        }

        public void setImage(String thumb_image , Context context) {
            CircleImageView userImageView = mView.findViewById(R.id.showImage);
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);
        }
    }

//    private void viewUsers() {
//        final List<UsersModel> usersModels = new ArrayList<>();
////        final ProfileActivity pro = new ProfileActivity();
//        firebaseDatabase.getReference().child("users").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
//                for (final DataSnapshot dataSnapshot1 : children) {
//                    UsersModel usersModel = dataSnapshot1.getValue(UsersModel.class);
//                    usersModels.add(usersModel);
//                }
//
//                addToAdapter(usersModels);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//    private void addToAdapter(List<UsersModel> usersModels) {
//        adapter.setData(usersModels);
//        adapter.notifyDataSetChanged();
//
//    }
//
//    private void setupRecycler() {
//
//        adapter = new UsersAdapter( this);
//        recyclerView.setAdapter(adapter);
//    }

    }

