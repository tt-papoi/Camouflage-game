package com.example.camouflagegame.Message;

public class ChatMessage {
    private String msg;
    public ChatMessage()
    {
        this.msg = "";
    }
    public ChatMessage(String msg)
    {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
