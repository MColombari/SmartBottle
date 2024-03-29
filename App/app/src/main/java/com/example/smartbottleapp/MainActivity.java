package com.example.smartbottleapp;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartbottleapp.localDatabaseInteraction.NewID;
import com.example.smartbottleapp.localDatabaseInteraction.HomeInitializer;
import com.example.smartbottleapp.serverInteraction.SendData;
import com.example.smartbottleapp.serverInteraction.UpdateRecycleView;
import com.example.smartbottleapp.serverInteraction.UpdateWaterDrank;
import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;

import java.util.ArrayList;
import java.util.Collection;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final int DIM_PACKAGE_TO_SEND = 4;
    final Float bottle_capacity = 0.6f;    // We could get this form user, but for now it's hard coded.
    ConstraintLayout connectionButton;
    TextView connectionButtonText;

    ImageView settingsImageView;
    ImageView refreshImageView;

    TextView idView;
    TextView waterDrankTextView;
    TextView latestInfoTextView;

    EditText newID;
    PopupWindow popupWindow;

    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;

    BluetoothManager bluetoothManager;
    SimpleBluetoothDeviceInterface deviceInterface;

    ArrayList<DataFromBottle> bottleData;

    boolean is_connected;

    Integer userID;

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

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
        settingsImageView = (ImageView) findViewById(R.id.SettingsImageView);
        refreshImageView = (ImageView) findViewById(R.id.RefreshImageView);
        idView = (TextView) findViewById(R.id.idTextView);
        waterDrankTextView = (TextView) findViewById(R.id.WaterDrankTextView);
        latestInfoTextView = (TextView) findViewById(R.id.LatestInfoTextView);
        recyclerView = (RecyclerView) findViewById(R.id.RecycleViewMain);


        settingsImageView.setOnClickListener(this);
        refreshImageView.setOnClickListener(this);


        bottleData = new ArrayList<>();

        is_connected = false;

        connectionButton.setOnClickListener(this);

        userID = null;

        recyclerViewAdapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(recyclerViewAdapter);
        /* I need to use LinearLayout because doesn't exits any Manager for ConstraintLayout. */
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        Thread t_HI = new Thread(new HomeInitializer(getApplicationContext(), idView, waterDrankTextView, recyclerViewAdapter, this));
        t_HI.start();
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
    }

    private void onMessageSent(String message) {
        // We sent a message! Handle it here.
    }

    private void onMessageReceived(String message) {
        // We received a message! Handle it here.
        Log.v("MessageFromBottleDebug", message);
        try {
            Log.v("ServerDebug", "Message Received");
            DataFromBottle dataFromBottle = new DataFromBottle(message);
            Log.v("ServerDebug", "Message Parsed");
            bottleData.add(dataFromBottle);
            StringBuilder sb = new StringBuilder();
            sb.append("ID: " + dataFromBottle.id + "\n");
            sb.append("Weight: " + dataFromBottle.weight + "%\n");
            sb.append("RawWeight: " + dataFromBottle.rawWeight + " [0-1024]\n");
            sb.append("Battery: " + dataFromBottle.battery + "%\n");
            sb.append("Time: " + dataFromBottle.receivedTime.toString());
            latestInfoTextView.setTextColor(getColor(R.color.black));
            latestInfoTextView.setText(sb.toString());
            if((bottleData.size() >= DIM_PACKAGE_TO_SEND) && (userID != null) && (bottle_capacity != null)){
                Log.v("ServerDebugSend", "Send Data");
                Log.v("ServerDebugSend", bottleData.toString());
                // Send data;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    new Thread(new SendData((ArrayList<DataFromBottle>) bottleData.clone(), userID, bottle_capacity)).start();
                }
                bottleData = new ArrayList<>();
            }
        }
        catch(Exception e){
            Log.v("AppException", e.toString());
            latestInfoTextView.setTextColor(getColor(R.color.red));
            latestInfoTextView.setText(e.getMessage());
        }
    }

    private void onError(Throwable error) {
        // Handle the error
        disconnectDevice();
    }

    private void disconnectDevice(){
        if(deviceInterface != null) {
            bluetoothManager.closeDevice(deviceInterface);
        }
        is_connected = false;

        connectionButton.setBackground(getResources().getDrawable(R.drawable.button_background_red));
        connectionButtonText.setText("Reconnect");
    }


    @Override
    public void onClick(View view) {
        if(view.getId() == settingsImageView.getId()){
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = layoutInflater.inflate(R.layout.popup_window, null);

            popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT, true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.setElevation(20);
            }
            popupWindow.setAnimationStyle(R.style.AnimationGenericPopupWindow);
            popupWindow.update();
            /* "v" is used as a parent view to get the View.getWindowToken() token from. */
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

            ConstraintLayout button = (ConstraintLayout) popupView.findViewById(R.id.ChangeIdButtonView);
            TextView buttonText = (TextView) popupView.findViewById(R.id.ChangeIdTextView);
            newID = (EditText) popupView.findViewById(R.id.editTextID);
            buttonText.setOnClickListener(this);
            button.setOnClickListener(this);

            // TODO finish the anime
        }
        else if((view.getId() == connectionButton.getId()) || (view.getId() == connectionButtonText.getId())){

            // Try to connect.
            Collection<BluetoothDevice> pairedDevices = bluetoothManager.getPairedDevicesList();

            ArrayList<String> devicesName = new ArrayList<>();
            ArrayList<String> devicesMac = new ArrayList<>();
            for (BluetoothDevice device : pairedDevices) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                } else {
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
        else if(view.getId() == refreshImageView.getId()){
            if(userID != null) {
                recyclerViewAdapter.removeAllElement();
                new Thread(new UpdateRecycleView(recyclerViewAdapter, userID, this)).start();
                waterDrankTextView.setText("NaN");
                new Thread(new UpdateWaterDrank(waterDrankTextView, userID, this)).start();
            }
        }
        else{
            try {
                int id = Integer.valueOf(String.valueOf(newID.getText()));
                userID = id;
                Thread t = new Thread(new NewID(getApplicationContext(), id));
                t.start();
                idView.setText(String.valueOf(id));
                recyclerViewAdapter.removeAllElement();
                new Thread(new UpdateRecycleView(recyclerViewAdapter, userID, this)).start();
                waterDrankTextView.setText("NaN");
                new Thread(new UpdateWaterDrank(waterDrankTextView, userID, this)).start();
            } catch (Exception e){
                e.printStackTrace();
            }
            popupWindow.dismiss();
        }
    }
}