package com.example.lenovo.chatapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class ChatsFragment extends Fragment {

    private RecyclerView mConversationList;

    private DatabaseReference mConversationDatabase;
    private DatabaseReference mMessagesDatabase;
    private DatabaseReference mUserDatabase;

    private FirebaseAuth mAuth;
    private String CurrentUserID;
    private View mMainView;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mConversationList = (RecyclerView) mMainView.findViewById(R.id.ConversationList);
        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        mConversationDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(CurrentUserID);
        mConversationDatabase.keepSynced(true);
        mMessagesDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(CurrentUserID);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConversationList.setHasFixedSize(true);
        mConversationList.setLayoutManager(linearLayoutManager);


        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConversationDatabase.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Conversation , ConversationViewHolder> ConverAdapter = new FirebaseRecyclerAdapter<Conversation, ConversationViewHolder>(
                Conversation.class,
                R.layout.users_single_layout,
                ConversationViewHolder.class,
                conversationQuery
        ) {
            @Override
            protected void populateViewHolder(final ConversationViewHolder converHolder, final Conversation conver, int i) {

                final String ListUserID = getRef(i).getKey();

                Query LastMessageQuery = mMessagesDatabase.child(ListUserID).limitToLast(1);

                LastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String data = dataSnapshot.child("Messages").getValue().toString();
                        converHolder.setMessage(data , conver.isSeen());
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

                mUserDatabase.child(ListUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String UserName = dataSnapshot.child("Name").getValue().toString();
                        String userThumb = dataSnapshot.child("Thumb_Image").getValue().toString();

                        if(dataSnapshot.hasChild("online"))
                        {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            converHolder.setUserOnline(userOnline);
                        }

                        converHolder.setName(UserName);
                        converHolder.setUserImage(userThumb , getContext());

                        converHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent chatIntent = new Intent(getContext() , ChatActivity.class);
                                chatIntent.putExtra("UserID" , ListUserID);
                                chatIntent.putExtra("UserName" , UserName);
                                startActivity(chatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mConversationList.setAdapter(ConverAdapter);
    }
}
