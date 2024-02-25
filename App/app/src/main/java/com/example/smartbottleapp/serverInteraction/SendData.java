package com.example.smartbottleapp.serverInteraction;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.smartbottleapp.DataFromBottle;

import org.json.JSONException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.grpc.Server;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SendData implements Runnable{
    ArrayList<DataFromBottle> dataFromBottleArrayList;
    int user_id;

    int bottle_capacity = 600;

    final static DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SendData(ArrayList<DataFromBottle> dataFromBottleArrayList, int user_id, int bottle_capacity) {
        this.dataFromBottleArrayList = dataFromBottleArrayList;
        this.user_id = user_id;
        this.bottle_capacity = bottle_capacity;
    }

    @Override
    public void run() {
        try {
            Log.v("SendDataDebug", "Start SendData");
            ArrayList<Reading> readingArrayList = new ArrayList<>();
            Set<Integer> bottle_ids = new HashSet<>();
            for (DataFromBottle d : dataFromBottleArrayList) {
                readingArrayList.add(new Reading(d.getId(), d.getReceivedTime().format(CUSTOM_FORMATTER), (int) d.getWeight()));
                bottle_ids.add(d.getId());
            }
            Log.v("SendDataDebug", "Start register bottle to user");
            for (Iterator<Integer> it = bottle_ids.iterator(); it.hasNext(); ) {
                ServerAPI.registerBottle(it.next(), user_id, bottle_capacity);
            }
            Log.v("SendDataDebug", readingArrayList.toString());
            ServerAPI.addReadings(readingArrayList);
            Log.v("SendDataDebug", "Data sent");
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }
}
