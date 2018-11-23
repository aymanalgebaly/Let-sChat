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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private Toolbar toolbar;
    private Button loginbtn;
    private EditText emailText, passText;
    private String mail, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbarlayout);
        loginbtn = findViewById(R.id.login);
        emailText = findViewById(R.id.loginMail);
        passText = findViewById(R.id.loginPass);

        toolbar = findViewById(R.id.toolbarlayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    }

    public void login(View view) {
        mail = emailText.getText().toString();
        pass = passText.getText().toString();

        if (!TextUtils.isEmpty(mail) || !TextUtils.isEmpty(pass)) {

            signIn(mail,pass);
        }
    }

    private void signIn(String mail, String pass) {

        auth.signInWithEmailAndPassword(mail,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    //Intent i = new Intent();
                    //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                }else {
                    Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
