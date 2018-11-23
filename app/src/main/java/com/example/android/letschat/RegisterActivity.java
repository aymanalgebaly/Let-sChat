package com.example.android.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private EditText userText, mailText, passText;
    private Button createbtn;
    private FirebaseAuth auth;//------- get uid
    String name, pass, mail;
    private Toolbar toolbar;
    private DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();//------- get uid

        toolbar = findViewById(R.id.toolbarlayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userText = findViewById(R.id.editTextName);
        mailText = findViewById(R.id.editTextMail);
        passText = findViewById(R.id.editTextPassword);
        createbtn = findViewById(R.id.create);
        createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = userText.getText().toString();
                mail = mailText.getText().toString();
                pass = passText.getText().toString();

                if (!TextUtils.isEmpty(name)|| !TextUtils.isEmpty(mail)|| !TextUtils.isEmpty(pass)){

                    validateForm(name,mail,pass);

                }

            }
        });
    }

         private void validateForm(final String name, String mail, String pass) {
        auth.createUserWithEmailAndPassword(mail,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

                    HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put("name",name);
                    hashMap.put("image","defult");
                    hashMap.put("status","Hi there,I'm using lET'S App");
                    hashMap.put("thumb_image","defult");
                   // hashMap.put("id",uid);

                    databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                             //   startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                Intent i = new Intent(RegisterActivity.this,MainActivity.class);
                               i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                               startActivity(i);
                               finish();//------ ميرجعش للاكتفتى دى تانى----------
                            }
                        }
                    });

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}

