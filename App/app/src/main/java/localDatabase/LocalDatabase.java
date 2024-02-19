package localDatabase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import localDatabase.tables.User;

/* Room serialize async transaction queries, so Room will not use more than one thread for executing
 * database transactions.
 * https://developer.android.com/jetpack/androidx/releases/room#2.1.0-alpha06
 *
 * Other source:
 * https://stackoverflow.com/questions/47525082/can-i-use-the-same-roomdatabase-object-from-multiple-threads-on-android*/

@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class LocalDatabase extends RoomDatabase {
    public abstract LocalDatabaseDao localDatabaseDao();
}