package com.example.smartbottleapp;

import android.os.Build;
import android.util.Log;

import java.time.LocalDateTime;

public class DataFromBottle {
    int id;
    int battery;
    float weight;   // value from [0 - 100].
    int rawWeight;
    LocalDateTime receivedTime;

    final int lowerBoundaries = 100;     // Under this we consider it an error;
    final int upperBoundaries = 350;    // Over this we consider it an error;
    final int lowValue = 170;           // Value corresponding to 0%;
    final int highValue = 290;          // Value corresponding to 100%;

    public DataFromBottle(String messageReceived) throws Exception {
        // Package structure: <ID>;<RawWeight>;<Battery>
        Log.v("ServerDebug", messageReceived);
        String[] part = messageReceived.split(";");
        Log.v("ServerDebug", part[0]);
        Log.v("ServerDebug", part[1]);
        Log.v("ServerDebug", part[2]);
        id = Integer.parseInt(part[0]);
        rawWeight = Integer.parseInt(part[1]);
        battery = Integer.parseInt(part[2]);

        Log.v("ServerDebug", "Parsing complete");

        // Calculate final weight
        // Range from raw data: [100 - 300]

        if ((rawWeight < lowerBoundaries) || (rawWeight > upperBoundaries)){
            // Value outOfBound.
            throw new Exception("Value of weight out of bound");
        }
        else if(rawWeight < lowValue){
            weight = 0;
        }
        else if(rawWeight > highValue){
            weight = 100;
        }
        else {
            weight = (rawWeight - lowValue) * (100 - 0) / (highValue - lowValue) + 0;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            receivedTime = LocalDateTime.now();
        }
        else{
            receivedTime = null;
        }
    }

    public int getId() {
        return id;
    }

    public float getWeight() {
        return weight;
    }

    public LocalDateTime getReceivedTime() {
        return receivedTime;
    }

    @Override
    public String toString() {
        return "DataFromBottle{" +
                "weight=" + weight +
                ", rawWeight" + rawWeight +
                ", battery=" + battery +
                ", receivedTime=" + receivedTime +
                '}';
    }
}
