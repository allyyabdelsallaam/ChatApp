package com.example.lenovo.chatapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;


public class UsersViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public UsersViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
    }

    public void setName(String Name)
    {
        TextView UsernameView = (TextView) mView.findViewById(R.id.UsernameView);
        UsernameView.setText(Name);
    }

    public void setStatus(String Status)
    {
        TextView StatusView = (TextView) mView.findViewById(R.id.StatusView);
        StatusView.setText(Status);
    }

    public void setImage(String Thumb_Image , Context ctx)
    {
        CircleImageView UserImage = (CircleImageView) mView.findViewById(R.id.UsersImage);
        Picasso.with(ctx).load(Thumb_Image).placeholder(R.drawable.avatar).into(UserImage);
    }
}
