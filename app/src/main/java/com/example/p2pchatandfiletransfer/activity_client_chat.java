package com.example.p2pchatandfiletransfer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class activity_client_chat extends AppCompatActivity {

    private EditText chatboxMessage;
    private ImageButton sendMessage;
    private ImageButton sendFile;
    private int myPort;
    private int receiverPort;
    private ServerSocket serverSocket;
    private Handler handler = new Handler();
    private String myIPAddress = "";
    private String receiverIPAddress = "";
    private String userName = "";
    private String TAG = "CLIENT ACTIVITY";
    private ListView messageList;
    private ArrayList<Message> messageArray;
    private Boolean exit = false;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
            userName = individualInfo[3];
        }

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        myIPAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

        getSupportActionBar().setTitle("P2P Chat and File Transfer");

        if (!receiverIPAddress.equals("")) {

            ServerChat serverChat = new ServerChat(getApplicationContext(), messageAdapter, messageList, messageArray,
                    myPort, myIPAddress, this, receiverIPAddress);
            serverChat.start();

            ServerFile serverFile = new ServerFile (getApplicationContext(), messageAdapter, messageList,
                    messageArray, myPort, receiverIPAddress);
            serverFile.start();
        }

        sendMessage.setOnClickListener(v -> {

            if (!chatboxMessage.getText().toString().isEmpty()) {
                User user = new User("1:" + chatboxMessage.getText().toString());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle change background
        switch (item.getItemId()) {
            case R.id.save_message: {
                Toast.makeText(this, "Message saved ", Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.change_background: {
                final Context context = activity_client_chat.this;
                ColorPickerDialogBuilder
                        .with(context)
                        .setTitle("Choose color")
                        .initialColor(0xffffffff)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(selectedColor -> {
                        })
                        .setPositiveButton("ok", (dialog, selectedColor, allColors) -> {
                            changeBackgroundColor(selectedColor);
                            User user = new User("2:" + Integer.toHexString(selectedColor));
                            user.execute();
                            Log.d("ColorPicker", "onColorChanged: 0x" + Integer.toHexString(selectedColor));
                        })
                        .setNegativeButton("cancel", (dialog, which) -> {
                        })
                        .build()
                        .show();
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        String path, path2;

        if (requestCode == 1) {

            Uri txtUri = data.getData();
            path = txtUri.getPath();

            //path2 = data.getData().

            Log.d(TAG, "onActivityResult: " + path);

            String[] arrOfStr = path.split(":");

            Log.d(TAG, "onActivityResult: " + arrOfStr.length);

            if (arrOfStr.length > 1)
                new FileTransfer(arrOfStr[1]).execute();
            else
                new FileTransfer(arrOfStr[0]).execute();
        }
    }

    public final void changeBackgroundColor(Integer selectedColor) {
        LayerDrawable layerDrawable = (LayerDrawable) messageList.getBackground();

        Log.d(TAG, "BG color: got");

        GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.shapeColor);
        gradientDrawable.setColor(selectedColor);

        Toast.makeText(this, "Background Color changed", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("StaticFieldLeak")
    public class User extends AsyncTask<Void, Void, String> {

        String message;

        User(String message) {
            this.message = message;
            Log.d(TAG,"message:"+message);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {

                String receiverIPAddress = activity_client_chat.this.receiverIPAddress;
                int receiverPort = activity_client_chat.this.receiverPort;

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

            if (stringBuilder.charAt(0) == '1' && stringBuilder.charAt(1) == ':') {

                stringBuilder.deleteCharAt(0);
                stringBuilder.deleteCharAt(0);

                result = stringBuilder.toString();

                File path = getApplicationContext().getObbDir();

                Log.i(TAG, "FilesDir =>" + path + "\n");

                String fileName = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" + receiverIPAddress + ".txt";

                File file = new File(path, fileName);
                try {

                    FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                    String history = "client: " + result + "\n";
                    fileOutputStream.write(history.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                messageArray.add(new Message(result, 0));
                messageList.setAdapter(messageAdapter);
                chatboxMessage.setText("");
            }
        }
    }

    class FileTransfer extends AsyncTask<Void, Integer, String> {
        String path;

        FileTransfer(String path) {
            this.path = path;
        }

        @Override
        protected String doInBackground(Void... voids) {

            String fileName = "";
            String receiverIPAddress = activity_client_chat.this.receiverIPAddress;
            int receiverPort = activity_client_chat.this.receiverPort + 1;

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
                fileName = file.getName();

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
            return fileName;
        }

        @Override
        protected void onPostExecute(String name) {
            Log.d(TAG, "onPostExecute: " + name);
            File filepath = getApplicationContext().getObbDir();
            Log.i(TAG, "FilesDir =>" + filepath + "\n");
            String fileName = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" + receiverIPAddress + ".txt";
            File file = new File(filepath, fileName);
            try {
                FileOutputStream fos = new FileOutputStream(file, true);
                String history = "client sent a file from => " + path + "\n";
                fos.write(history.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!name.isEmpty()) {
                messageArray.add(new Message("New File Sent: " + name, 0));
                messageList.setAdapter(messageAdapter);
                chatboxMessage.setText("");
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "File cannot be sent. No Internet Connection", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {

            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(() -> exit = false, 3 * 1000);
        }
    }
}
