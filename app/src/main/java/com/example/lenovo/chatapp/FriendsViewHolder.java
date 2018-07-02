package com.example.lenovo.chatapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendsViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public FriendsViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
    }

    public void setDate(String date)
    {
        TextView userStatusView = (TextView) mView.findViewById(R.id.StatusView);
        userStatusView.setText(date);
    }

    public void setName(String name)
    {
        TextView userNameView = (TextView) mView.findViewById(R.id.UsernameView);
        userNameView.setText(name);
    }

    public void setImage(String Thumb_Image , Context ctx)
    {
        CircleImageView UserImage = (CircleImageView) mView.findViewById(R.id.UsersImage);
        Picasso.with(ctx).load(Thumb_Image).placeholder(R.drawable.avatar).into(UserImage);
    }

    public void setUserOnline(String OnlineStatus)
    {
        ImageView userOnlineView = (ImageView) mView.findViewById(R.id.SingleUserOnline);

        if(OnlineStatus.equals("true"))
        {
            userOnlineView.setVisibility(View.VISIBLE);
        }
        else
        {
            userOnlineView.setVisibility(View.INVISIBLE);
        }
    }
}
