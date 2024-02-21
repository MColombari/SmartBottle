package com.example.smartbottleapp.serverInteraction;

import androidx.recyclerview.widget.RecyclerView;

import com.example.smartbottleapp.ElementRecycleView;
import com.example.smartbottleapp.MainActivity;
import com.example.smartbottleapp.RecyclerViewAdapter;

public class IsDispenserBusy implements Runnable{
    Dispenser dispenser;
    int position;
    MainActivity mainActivity;
    RecyclerViewAdapter recyclerViewAdapter;

    public IsDispenserBusy(Dispenser dispenser, final int position, MainActivity mainActivity, RecyclerViewAdapter recyclerViewAdapter) {
        this.dispenser = dispenser;
        this.position = position;
        this.mainActivity = mainActivity;
        this.recyclerViewAdapter = recyclerViewAdapter;
    }

    @Override
    public void run() {
        boolean isBusy = ServerAPI.isBusy(dispenser.id);
        if(isBusy) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerViewAdapter.setElementBusy(position);
                }
            });
        }
    }
}
