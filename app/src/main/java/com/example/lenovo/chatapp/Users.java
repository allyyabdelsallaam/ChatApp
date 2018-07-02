package com.example.lenovo.chatapp;

/**
 * Created by Lenovo on 3/5/2018.
 */

public class Users {

    private String Name;
    private String Image;
    private String Status;
    private String Thumb_Image;

    public Users()
    {

    }

    public Users(String name, String image, String status , String thmub_image) {
        this.Name = name;
        this.Image = image;
        this.Status = status;
        this.Thumb_Image = thmub_image;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        this.Image = image;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        this.Status = status;
    }

    public String getThumb_Image()
    {
        return Thumb_Image;
    }

    public void setThumb_Image(String thumb_image)
    {
        this.Thumb_Image = thumb_image;
    }
}
