package com.example.p2pchatandfiletransfer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ServerChat extends Thread {

    private String TAG = "CHATSERVER";

    private Context context;
    private ListView messageList;
    private ArrayList<Message> messageArray;
    private ChatAdapter messageAdapter;
    private int myPort;
    private String receiverIPAddress;
    private Activity activity;
    private String myIPAddress;

    ServerChat(Context context, ChatAdapter messageAdapter, ListView messageList,
               ArrayList<Message> messageArray, int myPort, String myIPAddress, Activity activity, String receiverIPAddress) {

        this.context = context;
        this.messageAdapter = messageAdapter;
        this.messageList = messageList;
        this.messageArray = messageArray;
        this.myPort = myPort;
        this.myIPAddress = myIPAddress;
        this.activity = activity;
        this.receiverIPAddress = receiverIPAddress;
    }

    public void run() {
        try {

            ServerSocket mySocket = new ServerSocket(myPort);
            mySocket.setReuseAddress(true);

            TextView textView;
            textView = activity.findViewById(R.id.textView);
            textView.setText("Server Socket Started at IP: " + myIPAddress + " and Port: " + myPort);
            textView.setBackgroundColor(Color.parseColor("#0a7e07"));

            while (!Thread.interrupted()) {

                Socket connectSocket = mySocket.accept();
                ReadFromClient readFromClient = new ReadFromClient();
                readFromClient.execute(connectSocket);
            }
        } catch (IOException e) {
            TextView textView;
            textView = activity.findViewById(R.id.textView);
            textView.setText("Server Socket initialization failed. Port is already in use.");
            textView.setBackgroundColor(Color.parseColor("#FF0800"));
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class ReadFromClient extends AsyncTask<Socket, Void, String> {

        private String text;

        @Override
        protected String doInBackground(Socket... sockets) {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(sockets[0].getInputStream()));
                text = input.readLine();

                Log.i(TAG, "Received => " + text);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return text;
        }

        protected void onPostExecute(String result) {

            Log.d(TAG, "onPostExecute: Result" + result);

            if (result.charAt(0) == '1' && result.charAt(1) == ':') {

                StringBuilder stringBuilder = new StringBuilder(result);
                stringBuilder.deleteCharAt(0);
                stringBuilder.deleteCharAt(0);
                result = stringBuilder.toString();

                File path = context.getObbDir();
                Log.i(TAG,"FilesDir =>" + path + "\n");
                String fileName =  new SimpleDateFormat("yyyyMMdd").format(new Date()) +"-" + receiverIPAddress + ".txt";
                File file = new File(path, fileName);

                try {

                    FileOutputStream fileOutputStream = new FileOutputStream(file,true);
                    String history = "server: " + result + "\n";
                    fileOutputStream.write(history.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                messageArray.add(new Message(result, 1));
                messageList.setAdapter(messageAdapter);
            } else {

                StringBuilder stringBuilder = new StringBuilder(result);
                stringBuilder.deleteCharAt(0);
                stringBuilder.deleteCharAt(0);
                result = stringBuilder.toString();

                ListView message_List;
                message_List = activity.findViewById(R.id.messageList);

                LayerDrawable layerDrawable = (LayerDrawable) message_List.getBackground();
                GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.shapeColor);
                gradientDrawable.setColor(Color.parseColor("#" + result));

                Toast.makeText(context, "Changed Background Color", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
