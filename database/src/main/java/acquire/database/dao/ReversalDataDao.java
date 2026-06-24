package acquire.database.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import acquire.database.model.ReversalData;


/**
 * Access to reversal data
 *
 * @author Janson
 * @date 2021/1/5 17:16
 */
@Dao
public interface ReversalDataDao {
    /**
     * Inserts a reversal data to the reversal table
     *
     * @param rw reversal data
     * @return the ID of the new record. If -1,failed.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ReversalData rw);

    /**
     * Deletes all reversal data
     *
     * @return the count of deleted records
     */
    @Query("DELETE FROM T_REVERSAL_DATA")
    int deleteAll();

    /**
     * update a reversal data
     *
     * @param rw new reversal data
     * @return the count of updated data
     */
    @Update
    int update(ReversalData rw);

    /**
     * find all reversal data
     *
     * @return all reversal data
     */
    @Query("SELECT * FROM T_REVERSAL_DATA")
    List<ReversalData> findAll();
}
