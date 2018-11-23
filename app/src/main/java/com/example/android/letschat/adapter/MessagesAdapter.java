package com.example.android.letschat.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.letschat.R;
import com.example.android.letschat.model.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {

    private List<Messages> messagesList ;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;

    public MessagesAdapter(List<Messages> messagesList){
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_conv,parent,false);
        return new MessagesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessagesViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();

        String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = messagesList.get(position);

        String from_user =c.getFrom();
        String message_type = c.getType();
        
        userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(from_user);
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                holder.displayName.setText(name);

                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(holder.chatImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.chatConv.setText(c.getMessage());

        if (from_user != null && from_user.equals(current_user_id)){

            holder.chatConv.setBackgroundColor(Color.WHITE);
            holder.chatConv.setTextColor(Color.BLACK);

        }else {
            holder.chatConv.setBackgroundResource(R.drawable.style_conv);
            holder.chatConv.setTextColor(Color.WHITE);
        }

        if (message_type.equals("text")){
            holder.chatConv.setText(c.getMessage());
            holder.imageView.setVisibility(View.VISIBLE);
        }else {
            holder.chatConv.setVisibility(View.INVISIBLE);
            Picasso.get().load(c.getMessage()).placeholder(R.drawable.default_avatar).into(holder.imageView);
        }

    }

    @Override
    public int getItemCount() {
        return messagesList!=null?messagesList.size():0;
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder {

        TextView chatConv,displayName;
        CircleImageView chatImage;
        ImageView imageView;

        public MessagesViewHolder(View itemView) {
            super(itemView);
            chatConv = itemView.findViewById(R.id.textFriendConv);
            chatImage = itemView.findViewById(R.id.imageFriendConv);
            imageView = itemView.findViewById(R.id.message_image_layout);
            displayName = itemView.findViewById(R.id.displayNameFriend);


        }
    }
}
