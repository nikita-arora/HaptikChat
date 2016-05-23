package com.nikitaarora.haptikchat;

public class ChatMessages {
    private String imageUrl;
    private String name;
    private String userName;
    private String messageBody;
    private String messageInfo;

    public String getImageUrl()    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)   {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name)    {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName)    {
        this.userName = userName;
    }

    public String getMessageBody()  {
        return messageBody;
    }

    public void setMessageBody(String messageBody)  {
        this.messageBody = messageBody;
    }

    public String getMessageInfo()    {
        return messageInfo;
    }

    public void setMessageInfo(String messageInfo)    {
        this.messageInfo = messageInfo;
    }
}
