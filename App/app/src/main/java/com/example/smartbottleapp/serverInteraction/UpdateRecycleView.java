package com.example.smartbottleapp.serverInteraction;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartbottleapp.ElementRecycleView;
import com.example.smartbottleapp.RecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class UpdateRecycleView implements Runnable{
    RecyclerView recyclerView;
    int userID;
    Context applicationContext;

    public UpdateRecycleView(RecyclerView recyclerView, int userID, Context applicationContext) {
        this.recyclerView = recyclerView;
        this.userID = userID;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run() {
        List<Dispenser> dispenserList = ServerAPI.getRecommendations(userID);
        if(dispenserList == null){
            dispenserList = new ArrayList<>();
        }

        ArrayList<ElementRecycleView> elementRecycleViewArrayList = new ArrayList<>();

        for(Dispenser d : dispenserList){
            elementRecycleViewArrayList.add(new ElementRecycleView(d, ServerAPI.isBusy(d.id)));
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(elementRecycleViewArrayList);
                recyclerView.setAdapter(recyclerViewAdapter);
                /* I need to use LinearLayout because doesn't exits any Manager for ConstraintLayout. */
                recyclerView.setLayoutManager(new LinearLayoutManager(applicationContext));
            }
        });
    }
}
