package com.example.lenovo.chatapp;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ConversationViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public ConversationViewHolder(View itemView) {
        super(itemView);

        mView = itemView;

    }

    public void setMessage(String Messages , boolean isSeen)
    {
        TextView messageView = (TextView) mView.findViewById(R.id.StatusView);
        messageView.setText(Messages);

        if(!isSeen)
        {
            messageView.setTypeface(messageView.getTypeface() , Typeface.BOLD);
        }
        else
        {
            messageView.setTypeface(messageView.getTypeface() , Typeface.NORMAL);
        }
    }

    public void setName(String name)
    {
        TextView UserNameView = (TextView) mView.findViewById(R.id.UsernameView);
        UserNameView.setText(name);
    }

    public void setUserImage(String thumb_image, Context ctx){

        CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.UsersImage);
        Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.avatar).into(userImageView);

    }

    public void setUserOnline(String online_status) {

        ImageView userOnlineView = (ImageView) mView.findViewById(R.id.SingleUserOnline);

        if(online_status.equals("true")){

            userOnlineView.setVisibility(View.VISIBLE);

        } else {

            userOnlineView.setVisibility(View.INVISIBLE);

        }

    }
}
