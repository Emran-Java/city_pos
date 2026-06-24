package acquire.database.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import acquire.database.model.HistorySummary;

/**
 * Access to history data
 *
 * @author Janson
 * @date 2023/12/12 16:24
 */
@Dao
public interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(HistorySummary historySummary);


    @Query("DELETE FROM T_HISTORY_SUMMARY WHERE MID = :mid and TID =:tid")
    int delete( String mid, String tid);


    @Query("SELECT COUNT (*) FROM T_HISTORY_SUMMARY")
    int getCount();


    /**
     * get the summary data
     */
    @Query("SELECT * FROM T_HISTORY_SUMMARY WHERE mid = :mid AND tid = :tid ")
    List<HistorySummary> getHistorySummary( String mid, String tid);
}
