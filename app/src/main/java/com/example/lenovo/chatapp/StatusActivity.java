package com.example.lenovo.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText mStatus;
    private Button mSaveChanges;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String Uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(Uid);

        mToolbar = (Toolbar) findViewById(R.id.status_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String StatusValue = getIntent().getStringExtra("StatusValue");

        mStatus = (EditText) findViewById(R.id.StatusInput);
        mSaveChanges = (Button) findViewById(R.id.StatusSaveBtn);

        mStatus.setText(StatusValue);

        mSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes....");
                mProgress.setMessage("Please wait!");
                mProgress.show();

                String status = mStatus.getText().toString();
                mStatusDatabase.child("Status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            mProgress.dismiss();
                            Intent SettingsIntent = new Intent(StatusActivity.this , SettingsActivity.class);
                            startActivity(SettingsIntent);
                        }
                        else
                        {
                            Toast.makeText(StatusActivity.this, "Error!!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
