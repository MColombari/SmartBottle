package com.example.smartbottleapp.serverInteraction;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartbottleapp.ElementRecycleView;
import com.example.smartbottleapp.MainActivity;
import com.example.smartbottleapp.RecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class UpdateRecycleView implements Runnable {
    RecyclerViewAdapter recyclerViewAdapter;
    int userID;
    MainActivity mainActivity;

    public UpdateRecycleView(RecyclerViewAdapter recyclerViewAdapter, int userID, MainActivity mainActivity) {
        this.recyclerViewAdapter = recyclerViewAdapter;
        this.userID = userID;
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        List<Dispenser> dispenserList = ServerAPI.getRecommendations(userID);
        if(dispenserList == null){
            dispenserList = new ArrayList<>();
        }

        int index = 0;
        for(Dispenser d : dispenserList){
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerViewAdapter.addElement(new ElementRecycleView(d, false));
                }
            });
            new Thread(new IsDispenserBusy(d, index, mainActivity, recyclerViewAdapter)).start();
            index += 1;
        }
    }
}
