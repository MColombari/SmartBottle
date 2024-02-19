package com.example.smartbottleapp.localDatabaseInteraction;

import android.content.Context;
import android.widget.TextView;

import androidx.room.Room;

import java.util.List;

import localDatabase.LocalDatabaseDao;
import localDatabase.LocalDatabase;
import localDatabase.tables.User;

public class UpdateID implements Runnable{
    private LocalDatabaseDao localDatabaseDao;
    TextView idTextView;

    public UpdateID(Context contextDatabase, TextView idTextView) {
        LocalDatabase localDatabase = Room.databaseBuilder(contextDatabase, LocalDatabase.class, "LocalDatabase")
                .fallbackToDestructiveMigration()
                .build();
        localDatabaseDao = localDatabase.localDatabaseDao();
        this.idTextView =idTextView;
    }

    public void run() {
        List<User> users = localDatabaseDao.getAllUserData();
        if(users.isEmpty()){
            idTextView.setText("NaN");
        }
        else {
            User user = users.get(0);
            idTextView.setText(String.valueOf(user.id));
        }
    }


}
