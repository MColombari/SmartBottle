package com.example.smartbottleapp;

import android.os.Build;

import java.time.LocalDateTime;

public class DataFromBottle {
    int battery;
    float weight;
    LocalDateTime receivedTime;

    public DataFromBottle(int battery, float weight, LocalDateTime receivedTime) {
        this.battery = battery;
        this.weight = weight;
        this.receivedTime = receivedTime;
    }

    public DataFromBottle(String messageReceived) {
        String[] part = messageReceived.split("%");
        battery = Integer.parseInt(part[0].split(" ")[1]);
        int tmp_weight = Integer.parseInt(part[1].split(" ")[1].split("u")[0]);

        // Calculate final weight
        // Range from raw data: [100 - 300]

        weight = tmp_weight;

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
                ", battery=" + battery +
                ", receivedTime=" + receivedTime +
                '}';
    }
}
