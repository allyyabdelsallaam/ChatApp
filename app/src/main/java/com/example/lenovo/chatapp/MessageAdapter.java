package com.example.lenovo.chatapp;



import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessageList)
    {
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout , parent ,false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView MessageText;
        public CircleImageView ProfileImage;
        public TextView DisplayName;
        public ImageView MessageImage;

        public MessageViewHolder(View view) {
            super(view);

            MessageText = (TextView) view.findViewById(R.id.MessageTextLayout);
            ProfileImage = (CircleImageView) view.findViewById(R.id.UserImageView);
            DisplayName = (TextView) view.findViewById(R.id.NameTextLayout);
            MessageImage = (ImageView) view.findViewById(R.id.MessageImage);
        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        //String CurrentUserID = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(i);

        String FromUser = c.getFrom();
        String MessageType = c.getType();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(FromUser);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("Name").getValue().toString();
                String image = dataSnapshot.child("Thumb_Image").getValue().toString();

                viewHolder.DisplayName.setText(name);

                Picasso.with(viewHolder.ProfileImage.getContext()).load(image)
                        .placeholder(R.drawable.avatar).into(viewHolder.ProfileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(MessageType.equals("text"))
        {
            viewHolder.MessageText.setText(c.getMessages());
            viewHolder.MessageImage.setVisibility(View.INVISIBLE);
        }
        else
        {
            viewHolder.MessageText.setVisibility(View.INVISIBLE);
            Picasso.with(viewHolder.ProfileImage.getContext()).load(c.getMessages())
                    .placeholder(R.drawable.avatar).into(viewHolder.MessageImage);
        }

        //if(FromUser.equals(CurrentUserID))
        //{
            //viewHolder.MessageText.setBackgroundColor(Color.WHITE);
            //viewHolder.MessageText.setTextColor(Color.BLACK);
        //}
        //else
        //{
            //viewHolder.MessageText.setBackgroundResource(R.drawable.message_text_background);
            //viewHolder.MessageText.setTextColor(Color.WHITE);
        //}
        //viewHolder.MessageText.setText(C.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
