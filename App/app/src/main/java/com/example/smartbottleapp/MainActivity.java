package com.example.smartbottleapp;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.Collection;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ConstraintLayout connectionButton;
    TextView connectionButtonText;
    TextView informationTextView;

    BluetoothManager bluetoothManager;
    SimpleBluetoothDeviceInterface deviceInterface;

    ArrayList<DataFromBottle> bottleData;

    boolean is_connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup our BluetoothManager
        bluetoothManager = BluetoothManager.getInstance();
        if (bluetoothManager == null) {
            // Bluetooth unavailable on this device :( tell the user
            Toast.makeText(getApplicationContext(), "Bluetooth not available.", Toast.LENGTH_LONG).show();
            finish();
        }

        connectionButton = (ConstraintLayout) findViewById(R.id.BackgroundButtonView);
        connectionButtonText = (TextView) findViewById(R.id.ButtonTextView);
        informationTextView = (TextView) findViewById(R.id.InformationTextView);

        bottleData = new ArrayList<>();

        is_connected = false;

        connectionButton.setOnClickListener(this);
    }

    public void connectToDevice(String mac) {
        // Connect to specified device.
        bluetoothManager.openSerialDevice(mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnected, this::onError);
    }

    private void onConnected(BluetoothSerialDevice connectedDevice) {
        // You are now connected to this device!
        // Here you may want to retain an instance to your device:
        deviceInterface = connectedDevice.toSimpleDeviceInterface();

        // Listen to bluetooth events
        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSent, this::onError);

        connectionButton.setBackground(getResources().getDrawable(R.drawable.button_background_green));
        connectionButtonText.setText("Connected");
        is_connected = true;

        // Let's send a message:
        //deviceInterface.sendMessage("Hello world!");
    }

    private void onMessageSent(String message) {
        // We sent a message! Handle it here.
    }

    private void onMessageReceived(String message) {
        // We received a message! Handle it here.

        try {
            DataFromBottle tmp = new DataFromBottle(message);
            bottleData.add(tmp);
            informationTextView.setText(tmp.toString());
        }
        catch(Exception e){
            informationTextView.setText("Error: message received not valid");
        }
    }

    private void onError(Throwable error) {
        // Handle the error
        disconnectDevice();
    }

    private void disconnectDevice(){
        bluetoothManager.closeDevice(deviceInterface);
        is_connected = false;

        connectionButton.setBackground(getResources().getDrawable(R.drawable.button_background_red));
        connectionButtonText.setText("Reconnect");
    }


    @Override
    public void onClick(View view) {
        // Try to connect.
        Collection<BluetoothDevice> pairedDevices = bluetoothManager.getPairedDevicesList();

        ArrayList<String> devicesName = new ArrayList<>();
        ArrayList<String> devicesMac = new ArrayList<>();
        for (BluetoothDevice device : pairedDevices) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {}
            else {
                devicesName.add(device.getName());
                devicesMac.add(device.getAddress());
            }
        }

        CharSequence[] cs = devicesName.toArray(new CharSequence[devicesName.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Device")
                .setItems(cs, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        connectToDevice(devicesMac.get(which));
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

        builder.show();

    }
}