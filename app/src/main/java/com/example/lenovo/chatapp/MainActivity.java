package com.example.lenovo.chatapp;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(" Chat App");

        if(mAuth.getCurrentUser() != null) {

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        mViewPager = (ViewPager) findViewById(R.id.MainPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabTextColors(Color.WHITE ,Color.WHITE);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser CurrentUser = mAuth.getCurrentUser();

        if(CurrentUser == null)
        {
            SendToStart();
        }
        else
        {
            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser CurrentUser = mAuth.getCurrentUser();

        if(CurrentUser != null) {

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void SendToStart()
    {
        Intent StartIntent = new Intent(MainActivity.this , StartActivity.class);
        startActivity(StartIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu , menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_btn)
        {
            FirebaseAuth.getInstance().signOut();
            SendToStart();
        }

        if(item.getItemId() == R.id.main_settings_btn)
        {
            Intent SettingsIntent = new Intent(MainActivity.this , SettingsActivity.class);
            startActivity(SettingsIntent);
        }

        if(item.getItemId() == R.id.main_all_btn)
        {
            Intent UsersIntent = new Intent(MainActivity.this , UsersActivity.class);
            startActivity(UsersIntent);
        }

        return true;
    }
}
