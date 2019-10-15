package com.example.p2pchatandfiletransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText ipReceiver;
    private EditText portReceiver;
    private EditText portSender;
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
        connectButton = findViewById(R.id.connectButton);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        senderIP = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        ipSender.setText(senderIP);

        connectButton.setOnClickListener(view -> {

            String info = getInfo();
            Intent intent = new Intent(MainActivity.this, ClientChat.class);
            intent.putExtra("ip & port", info);
            startActivity(intent);
            finish();
        });
    }

    String getInfo() {

        String info = this.ipReceiver.getText().toString() + " " +
                    this.portReceiver.getText().toString() + " " +
                    this.portSender.getText().toString();

        Log.i(TAG, "info => " + info);

        return info;
    }
}
