package com.example.smartbottleapp;

import com.example.smartbottleapp.serverInteraction.Dispenser;

public class ElementRecycleView {
    String name;
    String location;
    boolean is_busy;

    public ElementRecycleView(String name, String location, boolean is_busy) {
        this.name = name;
        this.location = location;
        this.is_busy = is_busy;
    }

    public ElementRecycleView(Dispenser d, boolean is_busy){
        this.name = d.name;
        this.location = d.location;
        this.is_busy = is_busy;
    }
}
