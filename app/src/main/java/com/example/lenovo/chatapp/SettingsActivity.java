package com.example.lenovo.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private Toolbar mToolbar;

    private CircleImageView mCircleImage;
    private TextView mName;
    private TextView mStatus;
    private Button ImageBtn;
    private Button StatusBtn;

    private static final int GALLERY_PICK = 1;

    private StorageReference mImageStorage;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mCircleImage = (CircleImageView) findViewById(R.id.SettingsImage);
        mName = (TextView) findViewById(R.id.DisplayName);
        mStatus = (TextView) findViewById(R.id.HeyText);
        ImageBtn = (Button) findViewById(R.id.ImageBtn);
        StatusBtn = (Button) findViewById(R.id.StatusBtn);

        mToolbar = (Toolbar) findViewById(R.id.SettingsToolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String Uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(Uid);
        mUserDatabase.keepSynced(true);
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("Name").getValue().toString();
                final String image = dataSnapshot.child("Image").getValue().toString();
                String status = dataSnapshot.child("Status").getValue().toString();
                String thumbimage = dataSnapshot.child("Thumb_Image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if(!image.equals("default"))
                {
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatar).into(mCircleImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.avatar).into(mCircleImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        StatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String StatusValue = mStatus.getText().toString();
                Intent StatusIntent = new Intent(SettingsActivity.this , StatusActivity.class);
                StatusIntent.putExtra("StatusValue" , StatusValue);
                startActivity(StatusIntent);
            }
        });

        ImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent GalleryIntent = new Intent();
                GalleryIntent.setType("image/*");
                GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(GalleryIntent , "SELECT IMAGE") , GALLERY_PICK);

               /* CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this); */
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            Uri ImageUri = data.getData();

            CropImage.activity(ImageUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading...");
                mProgressDialog.setMessage("Please wait!");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File ThumbFilePath = new File(resultUri.getPath());

                String CurrentUserID = mCurrentUser.getUid();

                Bitmap ThumbBitMap = new Compressor(this)
                        .setMaxHeight(200)
                        .setMaxWidth(200)
                        .setQuality(75)
                        .compressToBitmap(ThumbFilePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ThumbBitMap.compress(Bitmap.CompressFormat.JPEG , 100 , baos);
                final byte [] ThumbByte = baos.toByteArray();

                StorageReference filepath = mImageStorage.child("profile_images").child(CurrentUserID + ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(CurrentUserID + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful())
                        {
                            final String DownloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(ThumbByte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String ThumbDownloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful())
                                    {
                                        Map UpdateHashMap = new HashMap();
                                        UpdateHashMap.put("Image" , DownloadUrl);
                                        UpdateHashMap.put("Thumb_Image" , ThumbDownloadUrl);

                                        mUserDatabase.updateChildren(UpdateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful())
                                                {
                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this , "Done!" , Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }

                                    else
                                    {
                                        mProgressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this , "Error!" , Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this , "Error!" , Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }

   /* public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(100);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    } */
}
