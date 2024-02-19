package com.example.smartbottleapp.localDatabaseInteraction;

import android.content.Context;

import androidx.room.Room;

import localDatabase.LocalDatabaseDao;
import localDatabase.LocalDatabase;
import localDatabase.tables.User;


public class NewID implements Runnable{
    int newID;
    private LocalDatabaseDao localDatabaseDao;

    public NewID(Context contextDatabase, int newID) {
        LocalDatabase localDatabase = Room.databaseBuilder(contextDatabase, LocalDatabase.class, "LocalDatabase")
                .fallbackToDestructiveMigration()
                .build();
        localDatabaseDao = localDatabase.localDatabaseDao();
        this.newID = newID;
    }

    public void run() {
        localDatabaseDao.deleteEveryUser();
        localDatabaseDao.insertUser(new User(newID));
    }
}
