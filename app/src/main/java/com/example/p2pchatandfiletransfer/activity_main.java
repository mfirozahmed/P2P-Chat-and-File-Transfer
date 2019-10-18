package com.example.p2pchatandfiletransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class activity_main extends AppCompatActivity {

    private EditText ipReceiver;
    private EditText portReceiver;
    private EditText portSender;
    private EditText userName;
    private Button connectButton;
    private TextView ipSender;
    private String senderIP;
    private String TAG = "MAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        portSender = findViewById(R.id.portSender);
        portReceiver = findViewById(R.id.portReceiver);
        ipSender = findViewById(R.id.ipSender);
        ipReceiver = findViewById(R.id.ipReceiver);
        userName = findViewById(R.id.userName);
        connectButton = findViewById(R.id.connectButton);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        senderIP = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        ipSender.setText(senderIP);

        connectButton.setOnClickListener(view -> {

            boolean storagePermission = StoragePermission.checkPermission(activity_main.this);

            if(ipReceiver.length()==0 || portReceiver.length()==0 || portSender.length()==0){
//                if(ipReceiver.length()==0){
//                    ipReceiver.setError("Enter Receiver's ip address");
//                }
//                if(portReceiver.length()==0){
//                    portReceiver.setError("Enter Receiver's port No.");
//                }
//                if(portSender.length()==0){
//                    portSender.setError("Enter your port No.");
//                }
//                if(ipReceiver.length()==0 && portReceiver.length()==0 && portSender.length()==0){
                    showToast();
//                }
            }else {
                if (storagePermission) {
                    String info = getInfo();
                    Intent intent = new Intent(activity_main.this, activity_client_chat.class);
                    intent.putExtra("ip & port", info);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            }
        });
    }

    void showToast(){
        Toast toast = new Toast(getApplicationContext());
        View view = LayoutInflater.from(this).inflate(R.layout.toast_layout, null);
        TextView toastTextView = view.findViewById(R.id.toast_error);
        toastTextView.setText("You need to fill all the fields!!");
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);

        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        //Toast.makeText(this, "You need to fill all the fields!!", Toast.LENGTH_LONG).show();
    }

     String getInfo() {

        String name = this.userName.getText().toString();
        if (name.length() == 0)
            name = "User";

        String info = this.ipReceiver.getText().toString() + " " +
                    this.portReceiver.getText().toString() + " " +
                    this.portSender.getText().toString() + " " +
                    name;

        Log.i(TAG, "info => " + info);

        return info;
    }
}
