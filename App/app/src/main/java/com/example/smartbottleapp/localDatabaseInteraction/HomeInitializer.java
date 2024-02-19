package com.example.smartbottleapp.localDatabaseInteraction;

import android.content.Context;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.smartbottleapp.serverInteraction.UpdateRecycleView;

import java.util.List;

import localDatabase.LocalDatabaseDao;
import localDatabase.LocalDatabase;
import localDatabase.tables.User;

public class HomeInitializer implements Runnable{
    private LocalDatabaseDao localDatabaseDao;
    TextView idTextView;
    Context context;
    RecyclerView recyclerView;

    public HomeInitializer(Context contextDatabase, TextView idTextView, RecyclerView recyclerView) {
        context= contextDatabase;
        LocalDatabase localDatabase = Room.databaseBuilder(contextDatabase, LocalDatabase.class, "LocalDatabase")
                .fallbackToDestructiveMigration()
                .build();
        localDatabaseDao = localDatabase.localDatabaseDao();
        this.idTextView =idTextView;
        this.recyclerView = recyclerView;
    }

    public void run() {
        List<User> users = localDatabaseDao.getAllUserData();
        if(users.isEmpty()){
            idTextView.setText("NaN");
        }
        else {
            User user = users.get(0);
            idTextView.setText(String.valueOf(user.id));
            Thread t_RV = new Thread(new UpdateRecycleView(recyclerView, user.id, context));
            t_RV.start();
        }
    }


}
