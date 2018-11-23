package com.example.android.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Button button = findViewById(R.id.register);
    }

    public void register(View view) {
        startActivity(new Intent(StartActivity.this,RegisterActivity.class));
    }

    public void signin(View view) {
        startActivity(new Intent(StartActivity.this,LoginActivity.class));
    }
}
