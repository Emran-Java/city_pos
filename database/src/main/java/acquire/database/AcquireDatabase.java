package acquire.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;

import java.util.ArrayList;
import java.util.List;

import acquire.base.BaseApplication;
import acquire.database.dao.HistoryDao;
import acquire.database.dao.MerchantDao;
import acquire.database.dao.RecordDao;
import acquire.database.dao.ReversalDataDao;
import acquire.database.model.HistorySummary;
import acquire.database.model.Merchant;
import acquire.database.model.Record;
import acquire.database.model.ReversalData;

/**
 * The database of this app.
 *
 * @author Janson
 * @date 2019/6/24 18:30
 */
@Database(version = 1, entities = {Record.class, Merchant.class, ReversalData.class, HistorySummary.class})
public abstract class AcquireDatabase extends RoomDatabase {

    /**
     * Database name
     */
    private final static String DB_NAME = "acquire.db";

    /**
     * Database single instance
     */
    private static volatile AcquireDatabase instance;

    public static AcquireDatabase getInstance() {
        if (instance == null) {
            synchronized (AcquireDatabase.class) {
                if (instance == null) {
                    Context context = BaseApplication.getAppContext();
                    RoomDatabase.Builder<AcquireDatabase> builder = Room.databaseBuilder(context, AcquireDatabase.class, DB_NAME)
                            //allow to access to the database on the main thread.
                            .allowMainThreadQueries()
                            // If it is not set, the WAL mechanism  on the machine with high storage disk
                            // will lead to the loss of database information in case of power failure
                            .setJournalMode(JournalMode.TRUNCATE);
                    //update database.
                    List<Migration> migrations = updateDatabaseVersion();
                    if (!migrations.isEmpty()){
                        builder.addMigrations(migrations.toArray(new Migration[0]));
                    }
                    instance = builder.build();
                }
            }
        }
        return instance;
    }

    /**
     * update database version.
     */
    private static List<Migration> updateDatabaseVersion() {
        List<Migration> migrations = new ArrayList<>();
//        //database version 1->2
//        migrations.add(new Migration(1, 2) {
//            @Override
//            public void migrate(@NonNull SupportSQLiteDatabase database) {
//                //note: add goods and price variable in Record
//                database.execSQL("ALTER TABLE t_record ADD COLUMN goods TEXT");
//                database.execSQL("ALTER TABLE t_record ADD COLUMN price INTEGER");
//            }
//        });
//        //database version 2->3
//        migrations.add(new Migration(2, 3) {
//            @Override
//            public void migrate(@NonNull SupportSQLiteDatabase database) {
//                database.execSQL("ALTER TABLE t_record ADD COLUMN total INTEGER");
//            }
//        });
        return migrations;
    }

    //==============Must create a abstract method here when writing a new Dao in the project code.

    public abstract RecordDao recordDao();

    public abstract MerchantDao merchantDao();

    public abstract ReversalDataDao reverseRecordDao();

    public abstract HistoryDao historyDao();

}
