package com.example.android.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private TabLayout tabLayout;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbarlayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatChat");

        //---------- slide between fragments -----------------
        viewPager = findViewById(R.id.tabpager);
        tabLayout = findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);//-----input viewpager------

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        if (mAuth.getCurrentUser() != null) {


            mRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            sendToStart();
        }else {
            mRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            mRef.child("online").setValue(ServerValue.TIMESTAMP);
            //mRef.child("LastSeen").setValue(ServerValue.TIMESTAMP);
        }

    }

    private void sendToStart() {
        Intent i = new Intent(MainActivity.this,StartActivity.class);
        //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        //startActivity(new Intent(MainActivity.this,StartActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mainlogout :
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this,StartActivity.class));
                finish();
                break;

            case R.id.settingbtn:
                startActivity(new Intent(MainActivity.this,SettingsActivity.class));
                break;

            case R.id.usersbtn:
                startActivity(new Intent(MainActivity.this,UsersActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
