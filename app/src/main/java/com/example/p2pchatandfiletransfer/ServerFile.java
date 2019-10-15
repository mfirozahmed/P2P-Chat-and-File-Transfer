package com.example.p2pchatandfiletransfer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerFile extends Thread{

    Context context;
    private String TAG = "FILE SERVER";
    private ListView messageList;
    private ArrayList<Message> messageArray;
    private ChatAdapter messageAdapter;
    private int myPort;

    ServerFile(Context context, ChatAdapter messageAdapter, ListView messageList,
               ArrayList<Message> messageArray, int myPort) {

        this.context = context;
        this.messageAdapter = messageAdapter;
        this.messageList = messageList;
        this.messageArray = messageArray;
        this.myPort = myPort;
    }

    public void run() {
        try {
            ServerSocket fileSocket = new ServerSocket(myPort + 1);

            Log.d(TAG, "run: " + fileSocket.getLocalPort());

            fileSocket.setReuseAddress(true);

            System.out.println(TAG + "started");

            while (true) {

                Socket connectFileSocket = fileSocket.accept();

                Log.d(TAG, "run: File Opened");

                FileFromClient fileFromClient = new FileFromClient();
                fileFromClient.execute(connectFileSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class FileFromClient extends AsyncTask<Socket, Void, String> {

        String text;

        @Override
        protected String doInBackground(Socket... sockets) {
            try {

                File testDirectory = new File(context.getObbDir(), "Received");

                if (!testDirectory.exists())
                    testDirectory.mkdirs();

                try {

                    InputStream inputStream = sockets[0].getInputStream();
                    DataInputStream dataInputStream = new DataInputStream(inputStream);

                    String fileName = dataInputStream.readUTF();
                    File outputFile = new File(testDirectory, fileName);

                    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
                    long fileSize = dataInputStream.readLong();
                    int bytesRead;
                    byte[] byteArray = new byte[8192 * 16];

                    while (fileSize > 0 && (bytesRead = dataInputStream.read(byteArray, 0,
                            (int) Math.min(byteArray.length, fileSize))) != -1) {

                        outputStream.write(byteArray, 0, bytesRead);
                        fileSize -= bytesRead;
                    }

                    inputStream.close();
                    dataInputStream.close();
                    outputStream.flush();
                    outputStream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return text;
        }

        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute: Result" + result);
        }
    }
}
