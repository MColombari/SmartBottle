package com.example.smartbottleapp.serverInteraction;

public class Reading {
    int bottleId;
    String datetime;
    int value;

    public Reading(int bottleId, String datetime, int value) {
        this.bottleId = bottleId;
        this.datetime = datetime;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Reading{" +
                "bottleId=" + bottleId +
                ", datetime='" + datetime + '\'' +
                ", value=" + value +
                '}';
    }
}
