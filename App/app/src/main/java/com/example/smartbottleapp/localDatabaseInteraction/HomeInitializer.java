package com.example.smartbottleapp.localDatabaseInteraction;

import android.content.Context;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.smartbottleapp.MainActivity;
import com.example.smartbottleapp.RecyclerViewAdapter;
import com.example.smartbottleapp.serverInteraction.UpdateRecycleView;

import java.util.List;

import localDatabase.LocalDatabaseDao;
import localDatabase.LocalDatabase;
import localDatabase.tables.User;

public class HomeInitializer implements Runnable{
    private LocalDatabaseDao localDatabaseDao;
    TextView idTextView;
    Context context;
    RecyclerViewAdapter recyclerViewAdapter;
    MainActivity mainActivity;

    public HomeInitializer(Context contextDatabase, TextView idTextView, RecyclerViewAdapter recyclerViewAdapter, MainActivity mainActivity) {
        context= contextDatabase;
        LocalDatabase localDatabase = Room.databaseBuilder(contextDatabase, LocalDatabase.class, "LocalDatabase")
                .fallbackToDestructiveMigration()
                .build();
        localDatabaseDao = localDatabase.localDatabaseDao();
        this.idTextView =idTextView;
        this.recyclerViewAdapter = recyclerViewAdapter;
        this.mainActivity = mainActivity;
    }

    public void run() {
        List<User> users = localDatabaseDao.getAllUserData();
        if(users.isEmpty()){
            idTextView.setText("No ID saved");
        }
        else {
            User user = users.get(0);
            mainActivity.setUserID(user.id);
            idTextView.setText(String.valueOf(user.id));
            Thread t_RV = new Thread(new UpdateRecycleView(recyclerViewAdapter, user.id, mainActivity));
            t_RV.start();
        }
    }


}
