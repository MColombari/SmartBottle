package com.example.smartbottleapp;

public class Reading {
    int bottleId;
    String datetime;
    int value;

    public Reading(int bottleId, String datetime, int value) {
        this.bottleId = bottleId;
        this.datetime = datetime;
        this.value = value;
    }
}
