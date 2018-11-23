package com.example.android.letschat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.letschat.model.FriendsModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

//import com.example.android.chatchat.adapter.FriendsAdapter;
//import com.example.android.chatchat.adapter.UsersAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference friendDatabaseRef;
    private FirebaseAuth mAuth;
    private String currentUser;
    private View mView;
    private DatabaseReference mUsersDatabase;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mView = inflater.inflate(R.layout.fragment_friends,container,false);

        recyclerView = mView.findViewById(R.id.friendfr);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser().getUid();

        friendDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUser);
        friendDatabaseRef.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        friendDatabaseRef.keepSynced(true);



        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUser).limitToFirst(50);

        final FirebaseRecyclerOptions<FriendsModel> friendsModelFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<FriendsModel>()
                .setQuery(query, FriendsModel.class).build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<FriendsModel,FriendsViewHolder>(friendsModelFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final FriendsModel model) {

                holder.setDate(model.getDate());



                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String friendName = dataSnapshot.child("name").getValue().toString();
                        String friendThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        //String onlineUser = dataSnapshot.child("online").getValue().toString();

                        holder.setName(friendName);
                        holder.setImage(friendThumb);

                        if (dataSnapshot.hasChild("online")) {
                            final String userOnline = (String) dataSnapshot.child("online").getValue();
                            holder.onlineUserIcon(userOnline);
                        }
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options [] = new CharSequence[]{"Open Profile","Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0){
                                            Intent i = new Intent(getContext(),ProfileActivity.class);
                                            i.putExtra("user_id",list_user_id);
                                            startActivity(i);
                                        }
                                        else if (which == 1){
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id",list_user_id);
                                            chatIntent.putExtra("user_name",friendName);
                                            startActivity(chatIntent);
                                        }
                                    }
                                });

                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            
            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_list, parent, false);
                return new FriendsViewHolder(v);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }


        public void setDate(String date) {
            TextView userNameView = mView.findViewById(R.id.showStatus);
            userNameView.setText(date);
        }

        public void setName(String friendName) {
            TextView userStatusView = mView.findViewById(R.id.showName);
            userStatusView.setText(friendName);
        }

        public void setImage(String image){
            CircleImageView userNameImage = mView.findViewById(R.id.showImage);
            Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(userNameImage);
        }

        public void onlineUserIcon(String userStatus){
            ImageView userIconOnline = mView.findViewById(R.id.imageView);
            if (userStatus.equals("true")){
                userIconOnline.setVisibility(View.VISIBLE);
            }else {
                userIconOnline.setVisibility(View.INVISIBLE);
            }
        }
    }


}





