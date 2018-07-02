package com.example.lenovo.chatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button RegisterBtn;
    private Button LogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        LogIn = (Button) findViewById(R.id.LogIn);

        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent LogInIntent = new Intent(StartActivity.this , LoginActivity.class);
                startActivity(LogInIntent);
            }
        });

        RegisterBtn = (Button) findViewById(R.id.RegisterBtn);

        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent RegisterIntent = new Intent(StartActivity.this , RegisterActivity.class);
                startActivity(RegisterIntent);
            }
        });
    }
}
