package com.example.p2pchatandfiletransfer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class ChatAdapter extends ArrayAdapter {

    ChatAdapter(Activity context, ArrayList<Message> messageArrayList) {
        super(context, 0, messageArrayList);
    }

    @Override
    public View getView(int position, View converView, ViewGroup parent) {

        if (converView == null) {
            converView = LayoutInflater.from(getContext()).inflate(R.layout.messagelist, parent, false);
        }

        Message currentMessage = (Message) getItem(position);
        assert currentMessage != null;

        TextView sentMessage = converView.findViewById(R.id.list_sent);
        TextView receivedMessage = converView.findViewById(R.id.list_received);

        sentMessage.setText("");
        sentMessage.setVisibility(View.GONE);
        receivedMessage.setText("");
        receivedMessage.setVisibility(View.GONE);

        String message = currentMessage.getMessage();

        if (currentMessage.isSent()) {
            sentMessage.setText(message);
            sentMessage.setVisibility(View.VISIBLE);
        } else {
            receivedMessage.setText(message);
            receivedMessage.setVisibility(View.VISIBLE);
        }
        return converView;
    }
}
