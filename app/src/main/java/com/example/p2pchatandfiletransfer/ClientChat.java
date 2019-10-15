package com.example.p2pchatandfiletransfer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ClientChat extends Activity {

    private EditText chatboxMessage;
    private ImageButton sendMessage;
    private ImageButton sendFile;
    private int myPort;
    private int receiverPort;
    private ServerSocket serverSocket;
    private Handler handler = new Handler();
    private String receiverIPAddress = "";
    private String TAG = "CLIENT ACTIVITY";
    private String tempS;
    private ListView messageList;
    private ArrayList<Message> messageArray;


    public static ChatAdapter messageAdapter;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox);

        chatboxMessage = findViewById(R.id.chatBoxMessage); // message typing box
        messageList = findViewById(R.id.messageList); // list of messages
        sendMessage = findViewById(R.id.sendMessage); // message button
        sendFile = findViewById(R.id.sendFile); // file button

        messageArray = new ArrayList<>();
        messageAdapter = new ChatAdapter(this, messageArray);
        messageList.setAdapter(messageAdapter);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {

            String info = bundle.getString("ip & port");
            String[] individualInfo = info.split(" ");

            receiverIPAddress = individualInfo[0];
            receiverPort = Integer.parseInt(individualInfo[1]);
            myPort = Integer.parseInt(individualInfo[2]);
        }

        if (!receiverIPAddress.equals("")) {

            ServerChat serverChat = new ServerChat(getApplicationContext(), messageAdapter, messageList, messageArray, myPort);
            serverChat.start();

            ServerFile serverFile = new ServerFile (getApplicationContext(), messageAdapter, messageList, messageArray, myPort);
            serverFile.start();
        }

        sendMessage.setOnClickListener(v -> {

            if (!chatboxMessage.getText().toString().isEmpty()) {
                User user = new User();
                user.execute();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Please write something", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        sendFile.setOnClickListener(v -> {

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Select file"), 1);
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        String path;

        if (requestCode == 1) {

            Uri txtUri = data.getData();
            path = txtUri.getPath();

            Log.d(TAG, "onActivityResult: " + path);

            String[] arrOfStr = path.split(":");

            Log.d(TAG, "onActivityResult: " + arrOfStr.length);

            if (arrOfStr.length > 1)
                new fileTransfer(arrOfStr[1]).execute();
            else
                new fileTransfer(arrOfStr[0]).execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class User extends AsyncTask<Void, Void, String> {

        String sentMessage = chatboxMessage.getText().toString();
        String message = "1:" + sentMessage;

        @Override
        protected String doInBackground(Void... voids) {
            try {

                String receiverIPAddress = ClientChat.this.receiverIPAddress;
                int receiverPort = ClientChat.this.receiverPort;

                Socket clientSocket = new Socket(receiverIPAddress, receiverPort);
                OutputStream outToServer = clientSocket.getOutputStream();

                PrintWriter output = new PrintWriter(outToServer);

                output.println(message);

                output.flush();
                clientSocket.close();

                runOnUiThread(() -> sendMessage.setEnabled(false)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
            return message;
        }

        protected void onPostExecute(String result) {

            runOnUiThread(() -> sendMessage.setEnabled(true));

            Log.i(TAG, "on post execution result => " + result);

            StringBuilder stringBuilder = new StringBuilder(result);
            stringBuilder.deleteCharAt(0);
            stringBuilder.deleteCharAt(0);

            result = stringBuilder.toString();

            messageArray.add(new Message(result, 0));
            messageList.setAdapter(messageAdapter);
            chatboxMessage.setText("");
        }
    }

    class fileTransfer extends AsyncTask<Void, Integer, Integer> {
        String path;

        fileTransfer(String path) {
            this.path = path;
        }

        @Override
        protected Integer doInBackground(Void... voids) {

            String receiverIPAddress = ClientChat.this.receiverIPAddress;
            int receiverPort = ClientChat.this.receiverPort + 1;

            try {

                Socket clientSocket = new Socket(receiverIPAddress, receiverPort);

                if (path.charAt(0) != '/') {
                    path = "/storage/emulated/0/" + path;
                }

                File file = new File(path);

                if (path.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Path is empty", Toast.LENGTH_SHORT);
                    toast.show();
                }

                Log.d(TAG, "doInBackground: " + path);

                FileInputStream fileInputStream = new FileInputStream(file);

                long fileSize = file.length();
                byte[] byteArray = new byte[(int) fileSize];

                DataInputStream dataInputStream = new DataInputStream(fileInputStream);
                dataInputStream.readFully(byteArray, 0, byteArray.length);

                OutputStream outputStream = clientSocket.getOutputStream();

                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                dataOutputStream.writeUTF(file.getName());
                dataOutputStream.writeLong(byteArray.length);

                dataOutputStream.write(byteArray, 0, byteArray.length);
                dataOutputStream.flush();

                outputStream.write(byteArray, 0, byteArray.length);
                outputStream.flush();

                outputStream.close();
                dataOutputStream.close();

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
