package com.example.lenovo.chatapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private String UserName;
    private Toolbar mToolbar;

    private DatabaseReference mRootRef;

    private TextView mCustomName;
    private TextView mLastSeen;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageEdit;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mMessageAdapter;

    private static final int TOTAL_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private static final int GALLERY_PICK = 1;

    private StorageReference mImageStorage;

    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("UserID");
        UserName = getIntent().getStringExtra("UserName");

        //getSupportActionBar().setTitle(UserName);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ActionBarView = inflater.inflate(R.layout.chat_custom_bar , null);
        actionBar.setCustomView(ActionBarView);

        mCustomName = (TextView) findViewById(R.id.CustomNameView);
        mLastSeen = (TextView) findViewById(R.id.CustomLastSeen);
        mProfileImage = (CircleImageView) findViewById(R.id.CustomBarImage);
        mChatAddBtn = (ImageButton) findViewById(R.id.AddBtn);
        mChatSendBtn = (ImageButton) findViewById(R.id.SendBtn);
        mChatMessageEdit = (EditText) findViewById(R.id.MessageEdit);
        mMessageAdapter = new MessageAdapter(messagesList);
        mMessagesList = (RecyclerView) findViewById(R.id.MessagesList);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.MessageSwipe);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mMessageAdapter);
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mRootRef.child("Chat").child(mCurrentUserID).child(mChatUser).child("seen").setValue(true);
        
        loadMessages();

        mCustomName.setText(UserName);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("Image").getValue().toString();

                if(online.equals("true"))
                {
                    mLastSeen.setText("Online");
                }
                else
                {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long LastTime = Long.parseLong(online);
                    String LastSeenTime = getTimeAgo.getTimeAgo(LastTime , getApplicationContext());

                    mLastSeen.setText("Last seen " + LastSeenTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser))
                {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen" , false);
                    chatAddMap.put("timestamp" , ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserID + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserID , chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
            }
        });

        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent , "SELECT IMAGE") , GALLERY_PICK);
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();
            }


        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();

            final String CurrentUserRef = "Messages/" + mCurrentUserID + "/" + mChatUser;
            final String ChatUserRef = "Messages/" + mChatUser + "/" + mCurrentUserID;

            DatabaseReference UserMessagesPush = mRootRef.child("Messages").child(mCurrentUserID).child(mChatUser).push();

            final String PushID = UserMessagesPush.getKey();

            StorageReference FilePath = mImageStorage.child("message_images").child(PushID + ".jpg");

            FilePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful())
                    {
                        String DownloadUrl = task.getResult().getDownloadUrl().toString();

                        Map messageMap = new HashMap();
                        messageMap.put("Messages" , DownloadUrl);
                        messageMap.put("Type" , "image");
                        messageMap.put("from" , mCurrentUserID);
                        messageMap.put("seen" , false);
                        messageMap.put("timestamp" , ServerValue.TIMESTAMP);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(CurrentUserRef + "/" + PushID , messageMap);
                        messageMap.put(ChatUserRef + "/" + PushID , messageMap);

                        mChatMessageEdit.setText("");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null){

                                    Log.d("Chat Log", databaseError.getMessage().toString());

                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void loadMoreMessages()
    {
        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserID).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey))
                {
                    messagesList.add(itemPos++ , message);
                }
                else
                {
                    mPrevKey = mLastKey;
                }

                if(itemPos == 1)
                {
                    mLastKey = messageKey;
                }

                mMessageAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserID).child(mChatUser);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if(itemPos == 1)
                {
                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messagesList.add(message);
                mMessageAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size() - 1);
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {

        String message = mChatMessageEdit.getText().toString();

        if(!TextUtils.isEmpty(message))
        {
            String CurrentUserRef = "Messages/" + mCurrentUserID + "/" + mChatUser;
            String ChatUserRef = "Messages/" + mChatUser + "/" + mCurrentUserID;

            DatabaseReference UserMessagePush = mRootRef.child("Messages").child(mCurrentUserID).child(mChatUser).push();
            String PushID = UserMessagePush.getKey();

            Map messageMap = new HashMap();
            messageMap.put("Messages" , message);
            messageMap.put("seen" , false);
            messageMap.put("Type" , "text");
            messageMap.put("timestamp" , ServerValue.TIMESTAMP);
            messageMap.put("from" , mCurrentUserID);

            Map messageUserMap = new HashMap();
            messageUserMap.put(CurrentUserRef + "/" + PushID , messageMap);
            messageUserMap.put(ChatUserRef + "/" + PushID , messageMap);

            mChatMessageEdit.setText("");

            mRootRef.child("Chat").child(mCurrentUserID).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("Chat").child(mCurrentUserID).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserID).child("seen").setValue(false);
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserID).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null)
                    {
                        Log.d("ChatLog" , databaseError.getMessage().toString());
                    }
                }
            });
        }
    }
}
