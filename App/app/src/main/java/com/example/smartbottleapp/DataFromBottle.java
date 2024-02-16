package com.example.smartbottleapp;

import android.os.Build;

import java.time.LocalDateTime;

public class DataFromBottle {
    int battery;
    float weight;   // value from [0 - 100].
    int rawWeight;
    LocalDateTime receivedTime;

    final int lowerBoundaries = 150;     // Under this we consider it an error;
    final int upperBoundaries = 400;    // Over this we consider it an error;
    final int lowValue = 200;           // Value corresponding to 0%;
    final int highValue = 350;          // Value corresponding to 100%;

    public DataFromBottle(String messageReceived) throws Exception {
        String[] part = messageReceived.split("%");
        battery = Integer.parseInt(part[0].split(" ")[1]);
        rawWeight = Integer.parseInt(part[1].split(" ")[1].split("u")[0]);

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
