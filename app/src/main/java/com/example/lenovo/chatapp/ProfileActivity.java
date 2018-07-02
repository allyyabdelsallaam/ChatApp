package com.example.lenovo.chatapp;

import android.app.ProgressDialog;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    //private Toolbar mToolbar;
    private ImageView mProfileImage;
    private TextView mProfileUser , mProfileStatus , mFriendsCount;
    private Button mSendRequest , mDeclineBtn;

    private DatabaseReference mUserDatabase;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;
    private String current_states;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String UserID = getIntent().getStringExtra("UserID");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(UserID);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Requests");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //mToolbar = (Toolbar) findViewById(R.id.ProfileToolBar);
        //setSupportActionBar(mToolbar);
        //getSupportActionBar().setTitle("Profile");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProfileImage = (ImageView) findViewById(R.id.ProfileImage);
        mProfileUser = (TextView) findViewById(R.id.ProfileUserName);
        mProfileStatus = (TextView) findViewById(R.id.ProfileStatus);
        mFriendsCount = (TextView) findViewById(R.id.TotalFriends);
        mSendRequest = (Button) findViewById(R.id.SendRequestBtn);
        mDeclineBtn = (Button) findViewById(R.id.DeclineBtn);

        current_states = "not_friends";

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Profile...");
        mProgressDialog.setMessage("Please Wait!");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("Name").getValue().toString();
                String status = dataSnapshot.child("Status").getValue().toString();
                String image = dataSnapshot.child("Image").getValue().toString();

                mProfileUser.setText(name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.avatar).into(mProfileImage);

                if(mCurrentUser.getUid().equals(UserID)) {

                    mDeclineBtn.setEnabled(false);
                    mDeclineBtn.setVisibility(View.INVISIBLE);

                    mSendRequest.setEnabled(false);
                    mSendRequest.setVisibility(View.INVISIBLE);
                }

                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(UserID))
                        {
                            String ReqType = dataSnapshot.child(UserID).child("request_type").getValue().toString();

                            if(ReqType.equals("Received"))
                            {
                                current_states = "req_received";
                                mSendRequest.setText("Accept friend request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);
                            }
                            else if(ReqType.equals("Sent"))
                            {
                                current_states ="req_sent";
                                mSendRequest.setText("Cancel friend request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }

                            mProgressDialog.dismiss();
                        }
                        else
                        {
                            mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(UserID))
                                    {
                                        current_states = "friends";
                                        mSendRequest.setText("Unfriend");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }

                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSendRequest.setOnClickListener(new View.OnClickListener() {
            //@RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                mSendRequest.setEnabled(false);

                if(current_states.equals("not_friends")) {

                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(UserID).push();
                    String newNotificationKey = newNotificationRef.getKey();

                    HashMap<String, String> NotificationData = new HashMap<>();
                    NotificationData.put("From", mCurrentUser.getUid());
                    NotificationData.put("Type", "Request");

                    Map RequestMap = new HashMap();
                    RequestMap.put("Requests/" + mCurrentUser.getUid() + "/" + UserID + "/request_type", "Sent");
                    RequestMap.put("Requests/" + UserID + "/" + mCurrentUser.getUid() + "/request_type", "Received");
                    RequestMap.put("Notifications/" + UserID + "/" + newNotificationKey , NotificationData);

                    mRootRef.updateChildren(RequestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null)
                            {
                                Toast.makeText(ProfileActivity.this , "Error!!" , Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                current_states = "req_sent";
                                mSendRequest.setText("Cancel Friend Request");
                            }

                            mSendRequest.setEnabled(true);
                        }
                    });
                }

                if(current_states.equals("req_sent"))
                {
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(UserID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(UserID).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mSendRequest.setEnabled(true);
                                    current_states = "not_friends";
                                    mSendRequest.setText("Send Friend Request");

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }

                if(current_states.equals("req_received"))
                {
                    final String CurrentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map FriendsMap = new HashMap();

                    FriendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + UserID + "/date" , CurrentDate);
                    FriendsMap.put("Friends/" + UserID + "/" + mCurrentUser.getUid() + "/date" , CurrentDate);

                    FriendsMap.put("Requests/" + mCurrentUser.getUid() + "/" + UserID  , null);
                    FriendsMap.put("Requests/" + UserID + "/" + mCurrentUser.getUid() , null);

                    mRootRef.updateChildren(FriendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null)
                            {
                                mSendRequest.setEnabled(true);
                                current_states = "friends";
                                mSendRequest.setText("Unfriend");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            else
                            {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this , error , Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                if(current_states.equals("friends"))
                {
                    Map UnfriendMap = new HashMap();

                    UnfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + UserID  , null);
                    UnfriendMap.put("Friends/" + UserID + "/" + mCurrentUser.getUid() , null);

                    mRootRef.updateChildren(UnfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null)
                            {
                                current_states = "not_friends";
                                mSendRequest.setText("Send Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            else
                            {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this , error , Toast.LENGTH_LONG).show();
                            }

                            mSendRequest.setEnabled(true);
                        }
                    });
                }

                //if(current_states.equals("req_received"))
                //{
                   // Map DeclineMap = new HashMap();

                   // DeclineMap.put("Requests/" + mCurrentUser.getUid() + "/" + UserID  , null);
                    //DeclineMap.put("Requests/" + UserID + "/" + mCurrentUser.getUid() , null);

                   // mRootRef.updateChildren(DeclineMap, new DatabaseReference.CompletionListener() {
                       // @Override
                       // public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                           // if(databaseError == null)
                           // {
                             //   current_states = "not_friends";
                             //   mSendRequest.setText("Send Friend Request");

                             //   mDeclineBtn.setVisibility(View.INVISIBLE);
                             //   mDeclineBtn.setEnabled(false);
                            //}
                            //else
                           // {
                            //    String error = databaseError.getMessage();
                            //    Toast.makeText(ProfileActivity.this , error , Toast.LENGTH_LONG).show();
                           // }

                           // mSendRequest.setEnabled(true);
                      //  }
                   // });
                //}
            }
        });

    }
}
