package localDatabase;

import android.database.sqlite.SQLiteException;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import localDatabase.tables.User;

/* Query:
*   insert into User(nikName, name, surname, bYear, bMonth, bDay, comments, type) values ("Mela", "Sinisa", "Melarosa", 2001, 1, 13, "Compleanno del mela.", 0)
*    */

/* DAO: Database Access Object. */
@Dao
public interface LocalDatabaseDao {
    @Transaction
    @Query("SELECT * FROM User")
    List<User> getAllUserData() throws SQLiteException;

    @Transaction
    @Insert
    void insertUser(User friendsData) throws SQLiteException;

    @Transaction
    @Query("DELETE FROM User")
    void deleteEveryUser();
}