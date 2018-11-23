package com.example.android.letschat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity implements StatusDialog.StatusDialogListener{

    private DatabaseReference databaseReference;
    private FirebaseUser current_user;
    private TextView displayName,displayText;
    private CircleImageView circleImageView;
    private Button imgbtn,statusbtn;
    private static final int gallery_pick =1;
    private StorageReference storageReference;
    private String status;
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        imgbtn = findViewById(R.id.chageimg);
        statusbtn = findViewById(R.id.changesta);
        statusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value = displayText.getText().toString();

                openDialog();
            }
        });

        displayName = findViewById(R.id.nameTextView);
        displayText = findViewById(R.id.statusTextView);

        circleImageView = findViewById(R.id.circleimg);

        storageReference = FirebaseStorage.getInstance().getReference();


        current_user = FirebaseAuth.getInstance().getCurrentUser();

        String uid = current_user.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        databaseReference.keepSynced(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                displayName.setText(name);
                displayText.setText(status);

                if (!image.equals("default")){
                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(circleImageView);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void openDialog() {

        StatusDialog statusDialog = new StatusDialog();
        statusDialog.show(getSupportFragmentManager(),"Dialog Status");

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)



    public void changeimage(View view) {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),gallery_pick);

        /*
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
                */


    }

    @Override
    public void applyText(String statusText) {
        displayText.setText(statusText);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==gallery_pick && resultCode==RESULT_OK){
            Uri imageurl = data.getData();
            CropImage.activity(imageurl)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                File file_path = new File(resultUri.getPath());

                String current_user_id = current_user.getUid();

                try {
                     bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(file_path);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference filepath = storageReference.child("profile_image").child(current_user_id + "jpg");
                final StorageReference tumb_filePath = storageReference.child("profile_image").child("thumbs").child(current_user_id + "jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){

                            final String download_uri = task.getResult().getDownloadUrl().toString();



                            UploadTask uploadTask = tumb_filePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_url = thumb_task.getResult().getDownloadUrl().toString();

                                    if (thumb_task.isSuccessful()){

                                        Map hashMap = new HashMap<>();
                                        hashMap.put("image",download_uri);
                                        hashMap.put("thumb_image",thumb_url);

                                        databaseReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){

                                                }
                                            }
                                        });
                                    }
                                }
                            });



                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
