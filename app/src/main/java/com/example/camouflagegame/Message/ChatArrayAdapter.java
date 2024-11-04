package com.example.camouflagegame.Message;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.camouflagegame.Message.ChatMessage;
import com.example.camouflagegame.R;

import java.util.ArrayList;

public class ChatArrayAdapter extends ArrayAdapter<ChatMessage>
{
    private Context context;
    private ArrayList<ChatMessage> chatMessages;

    public ChatArrayAdapter(Context context, int layoutToBeInflated, ArrayList<ChatMessage> chatMessages)
    {
        super(context, layoutToBeInflated, chatMessages);
        this.context = context;
        this.chatMessages = chatMessages;

    }
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(R.layout.chat_msg_row, null);
        TextView msg = row.findViewById(R.id.txtViewMsg);
        msg.setText(chatMessages.get(position).getMsg());
        return (row);
    }
}