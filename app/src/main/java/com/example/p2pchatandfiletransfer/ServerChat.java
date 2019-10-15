package com.example.p2pchatandfiletransfer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerChat extends Thread {

    private String TAG = "CHATSERVER";

    private Context context;
    private ListView messageList;
    private ArrayList<Message> messageArray;
    private ChatAdapter messageAdapter;

    private int myPort;

    ServerChat(Context context, ChatAdapter messageAdapter, ListView messageList,
               ArrayList<Message> messageArray, int myPort) {

        this.context = context;
        this.messageAdapter = messageAdapter;
        this.messageList = messageList;
        this.messageArray = messageArray;
        this.myPort = myPort;
    }

    public void run() {
        try {

            ServerSocket mySocket = new ServerSocket(myPort);
            mySocket.setReuseAddress(true);

            System.out.println(TAG + "started");

            while (true) {

                Socket connectSocket = mySocket.accept();
                ReadFromClient readFromClient = new ReadFromClient();
                readFromClient.execute(connectSocket);
            }
        } catch (IOException e) {
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

                messageArray.add(new Message(result, 1));
                messageList.setAdapter(messageAdapter);
            } else {
                try {
                    Log.i(TAG, "else cause");

                    File file = new File(context.getObbDir(), "testfile.txt");

                    Log.i(TAG, "FIle dir => " + file);

                    FileWriter writer = new FileWriter(file);
                    writer.append(result);
                    writer.flush();
                    writer.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
