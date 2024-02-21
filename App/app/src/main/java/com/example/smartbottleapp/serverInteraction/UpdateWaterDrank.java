package com.example.smartbottleapp.serverInteraction;

import android.widget.TextView;

import com.example.smartbottleapp.ElementRecycleView;
import com.example.smartbottleapp.MainActivity;
import com.example.smartbottleapp.RecyclerViewAdapter;

import io.grpc.Server;

public class UpdateWaterDrank implements Runnable{
    TextView waterDrankTextView;
    int userID;
    MainActivity mainActivity;

    public UpdateWaterDrank(TextView waterDrankTextView, int userID, MainActivity mainActivity) {
        this.waterDrankTextView = waterDrankTextView;
        this.userID = userID;
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        Double waterDrank = ServerAPI.getWaterDrankUser(userID);
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                waterDrankTextView.setText(String.valueOf(waterDrank) + "L");
            }
        });
    }
}
