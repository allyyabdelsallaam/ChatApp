package com.example.lenovo.chatapp;

/**
 * Created by Lenovo on 6/28/2018.
 */

public class Messages {

    private String Messages , Type , from;
    private long timestamp;
    private boolean seen;

    public Messages(String Messages , boolean seen , long timestamp , String Type , String from)
    {
        this.Messages = Messages;
        this.seen = seen;
        this.timestamp = timestamp;
        this.Type = Type;
        this.from = from;
    }

    public Messages()
    {

    }

    public String getMessages()
    {
        return Messages;
    }

    public void setMessages(String Messages)
    {
        this.Messages = Messages;
    }

    public boolean isSeen()
    {
        return  seen;
    }

    public void setSeen(boolean seen)
    {
        this.seen = seen;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public String getType()
    {
        return Type;
    }

    public void setType(String Type)
    {
        this.Type = Type;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }
}
