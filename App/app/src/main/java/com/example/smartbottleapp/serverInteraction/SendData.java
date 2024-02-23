package com.example.smartbottleapp.serverInteraction;

import com.example.smartbottleapp.DataFromBottle;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.grpc.Server;

public class SendData implements Runnable{
    ArrayList<DataFromBottle> dataFromBottleArrayList;
    int user_id;

    int bottle_capacity = 600;

    public SendData(ArrayList<DataFromBottle> dataFromBottleArrayList, int user_id, int bottle_capacity) {
        this.dataFromBottleArrayList = dataFromBottleArrayList;
        this.user_id = user_id;
        this.bottle_capacity = bottle_capacity;
    }

    @Override
    public void run() {
        try {
            ArrayList<Reading> readingArrayList = new ArrayList<>();
            Set<Integer> bottle_ids = new HashSet<>();
            for (DataFromBottle d : dataFromBottleArrayList) {
                readingArrayList.add(new Reading(d.getId(), d.getReceivedTime().toString(), (int) d.getWeight()));
                bottle_ids.add(d.getId());
            }
            for (Iterator<Integer> it = bottle_ids.iterator(); it.hasNext(); ) {
                ServerAPI.registerBottle(it.next(), user_id, bottle_capacity);
            }
            ServerAPI.addReadings(readingArrayList);
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }
}
