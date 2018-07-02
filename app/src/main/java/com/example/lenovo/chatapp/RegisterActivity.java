package com.example.lenovo.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText DisplayName;
    private EditText DisplayEmail;
    private EditText DisplayPassword;
    private Button CreateBtn;

    private Toolbar mToolbar;
    private DatabaseReference mDatabase;

    FirebaseAuth mAuth;
    private ProgressDialog mRegProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        DisplayName = (EditText) findViewById(R.id.DisplayName);
        DisplayEmail = (EditText) findViewById(R.id.DisplayEmail);
        DisplayPassword = (EditText) findViewById(R.id.DisplayPassword);
        CreateBtn = (Button) findViewById(R.id.CreateBtn);
        mToolbar = (Toolbar) findViewById(R.id.reg_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRegProgress = new ProgressDialog(this);

        CreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name = DisplayName.getText().toString().trim();
                String display_email = DisplayEmail.getText().toString().trim();
                String display_password = DisplayPassword.getText().toString().trim();

                if(!TextUtils.isEmpty((display_name)) || !TextUtils.isEmpty(display_email) || !TextUtils.isEmpty(display_password))
                {
                    mRegProgress.setTitle("Processing");
                    mRegProgress.setMessage("Please wait!");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    RegisterUser(display_name , display_email , display_password);
                }
            }
        });
    }

    private void RegisterUser(final String display_name , String display_email , String display_password)
    {
        mAuth.createUserWithEmailAndPassword(display_email , display_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    FirebaseUser CurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String Uid = CurrentUser.getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(Uid);
                    HashMap<String , String> UserMap = new HashMap<>();
                    UserMap.put("Name" , display_name);
                    UserMap.put("Status" , "Hey There! , I,m using your Chat App");
                    UserMap.put("Image" , "default");
                    UserMap.put("Thumb_Image" , "default");
                    UserMap.put("Token" , deviceToken);

                    mDatabase.setValue(UserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                mRegProgress.dismiss();
                                Intent MainIntent = new Intent(RegisterActivity.this , MainActivity.class);
                                MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(MainIntent);
                                finish();
                            }
                        }
                    });
                }
                else
                {
                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this , "Cannot sign up try again" , Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
