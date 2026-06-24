package acquire.database.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

import acquire.database.bean.TransactionSummary;
import acquire.database.model.Record;

/**
 * Access to transaction record
 *
 * @author Janson
 * @date 2021/1/5 17:16
 */
@Dao
public interface RecordDao {
    /**
     * Inserts a transaction record
     *
     * @param record transaction record
     * @return the ID of the new record. If -1,failed.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Record record);
    /**
     * Deletes all transaction records
     *
     * @return the count of deleted records
     */
    @Query("DELETE FROM t_record ")
    int deleteAll();
    /**
     * Deletes all transaction records of a merchant
     *
     * @param mid merchant ID
     * @param tid terminal ID
     * @return the count of deleted records
     */
    @Query("DELETE FROM t_record WHERE MID = (:mid) and TID=(:tid)")
    int deleteAll(String mid, String tid);

    @Query("DELETE FROM t_record WHERE MID = (:mid) and TID=(:tid) AND TRANS_TYPE IN ('Sale','VoidSale','AuthComplete','VoidAuthComplete','Installment','VoidInstallment','TipSale','VoidTipSale')")
    int deleteBtacSettleByMidTid(String mid, String tid);
    /**
     * Deletes a transaction record by ID
     *
     * @param id record ID
     * @return the count of deleted records
     */
    @Query("DELETE FROM t_record WHERE ID = (:id)")
    int deleteById(long id);
    /**
     * finds all transaction records
     *
     * @return all transaction records
     */
    @Query("SELECT * FROM t_record")
    List<Record> findAll();

//    @Query("SELECT * FROM t_record WHERE TRANS_TYPE='Sale' OR TRANS_TYPE='VoidSale' OR TRANS_TYPE='AuthComplete' OR TRANS_TYPE='VoidAuthComplete' OR TRANS_TYPE='PreAuth' OR TRANS_TYPE='VoidPreAuth' ")
//    @Query("SELECT * FROM t_record WHERE TRANS_TYPE IN ('Sale','VoidSale','AuthComplete','VoidAuthComplete','PreAuth','VoidPreAuth')")
    @Query("SELECT * FROM t_record WHERE TRANS_TYPE IN ('Sale','VoidSale','AuthComplete','VoidAuthComplete','TipSale','VoidTipSale') AND BATCH_UP_FLAG = 0")
    List<Record> findSchemeWiseOnOffUs();

    @Query("SELECT * FROM t_record WHERE TRANS_TYPE IN ('Installment','VoidInstallment') AND BATCH_UP_FLAG = 0")
    List<Record> findEmiSchemeWiseOnOffUs();

    @Query("SELECT * FROM t_record WHERE TRANS_TYPE IN ('Installment','VoidInstallment')")
    List<Record> findEmiSchemeWiseOnOffUs2();


    // DAO Query
    @Query("SELECT * FROM T_RECORD " +
            "WHERE TRANS_TYPE IN ('Sale','VoidSale','AuthComplete','VoidAuthComplete','PreAuth','VoidPreAuth')")
    List<Record> findSchemeWiseOnOffUs2();

    @Query("SELECT * FROM T_RECORD " +
            "WHERE TRANS_TYPE IN ('Sale','VoidSale','AuthComplete','VoidAuthComplete','VoidTipSale','TipSale')")
    List<Record> findSettleSchemeWiseOnOffUs2();


    //@Query("SELECT * FROM t_record WHERE TRANS_TYPE!='TestTxn'")
    @Query("SELECT * FROM t_record WHERE TRANS_TYPE!='TestTxn'")
    List<Record> findAllReport();


    @Query("UPDATE t_record SET IS_ON_US = 1 WHERE TRANS_TYPE != 'TestTxn'")
    int updateIsOnUs();

    //TRANS_TYPE!='TestTxn' AND TRANS_TYPE!='VoidPreAuth' AND
    @Query("SELECT * FROM t_record WHERE TRANS_TYPE='Sale' OR TRANS_TYPE='VoidSale'")
    List<Record> findAllSaleAndVoid();

    @Query("SELECT * FROM t_record WHERE TRANS_TYPE='Sale' OR TRANS_TYPE='VoidSale' OR TRANS_TYPE='AuthComplete' OR TRANS_TYPE='VoidAuthComplete'")
    List<Record> findAllSalePreAuthAndVoid();

    @Query("SELECT * FROM t_record WHERE TRANS_TYPE='Sale' OR TRANS_TYPE='AuthComplete'")
    List<Record> findAllSalePreAuthComplete();



//    @Query("SELECT * FROM t_record WHERE TRANS_TYPE!='TestTxn' AND TRANS_TYPE='PreAuth'")
    @Query("SELECT * FROM t_record WHERE TRANS_TYPE='VoidPreAuth' OR TRANS_TYPE='PreAuth'")
    List<Record> findAllPreAuth();

    @Query("SELECT * FROM t_record WHERE TRANS_TYPE='VoidPreAuth'")
    List<Record> findAllVoidPreAuth();

//    @Query("SELECT * FROM t_record WHERE TRANS_TYPE LIKE '%' || 'Void' || '%'")
//    @Query("SELECT * FROM t_record WHERE (TRANS_TYPE='VoidSale' OR TRANS_TYPE='VoidAuthComplete') ")
    @Query("SELECT * FROM t_record WHERE TRANS_TYPE IN ('VoidSale','VoidAuthComplete','VoidTipSale') ")
    List<Record> findAllVoidReport();
    /**
     * finds the transaction records by MID
     *
     * @param mid merchant ID
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record WHERE MID = :mid and TID =:tid")
    List<Record> findByMidTid(String mid, String tid);

    /**
     * finds a range of transaction records by desc.
     * <p>e.g. <code>findByRangeDesc(3,10)</code>, find 10 records from the bottom 4th</p>
     *
     * @param firstIndex Starting index. The min is 0
     * @param size       the count to find
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record ORDER BY ID DESC LIMIT :firstIndex, :size ")
    List<Record> findByRangeDesc(int firstIndex, int size);

     @Query("SELECT * FROM t_record WHERE TRANS_TYPE IN ('Sale','AuthComplete','Installment','TipSale') ORDER BY ID DESC LIMIT :firstIndex, :size")
    List<Record> findByRangeDescBtacTran(int firstIndex, int size);

    /**
     * finds a range of transaction records by acs.
     * <p>e.g. <code>findByRange(3,10)</code>, find 10 records from the 4th</p>
     *
     * @param firstIndex Starting index. The min is 0
     * @param size       the count to find
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record ORDER BY ID LIMIT :firstIndex, :size ")
    List<Record> findByRange(int firstIndex, int size);
    /**
     * finds a range of a merchant's transaction records by acs.
     */
    @Query("SELECT * FROM t_record WHERE MID = :mid and TID =:tid ORDER BY ID LIMIT :firstIndex,:size ")
    List<Record> findByRange(String mid, String tid,int firstIndex,  int size);

    @Query("SELECT * FROM t_record WHERE MID = :mid and TID =:tid AND TRANS_TYPE IN ('Sale','AuthComplete','Installment','TipSale') ORDER BY ID LIMIT :firstIndex,:size ")
    List<Record> findByIndexBracSettle(String mid, String tid,int firstIndex,  int size);


    /**
     * finds the transaction records by trace NO
     *
     * @param traceNo trace
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record WHERE TRACE_NO = :traceNo")
    List<Record> findByTraceNo(String traceNo);

    @Query("SELECT * FROM t_record WHERE AUTH_CODE = :authCode AND (TRANS_TYPE = 'Sale' OR TRANS_TYPE = 'PreAuth')")
    List<Record> findSaleByApprovalCode(String authCode);
    @RawQuery()
    List<Record> findBySql(SupportSQLiteQuery sqlQuery);

    /**
     * finds the transaction records by refer number
     *
     * @param referNum efer number
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record WHERE REFER_NO = :referNum")
    List<Record> findByReferNum(String referNum);

    /**
     * finds the transaction records by auth code
     *
     * @param authCode auth code
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record WHERE AUTH_CODE = :authCode")
    List<Record> findByAuthCode(String authCode);

    /**
     * finds the transaction records by out order
     *
     * @param outOrderNo out order
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record WHERE OUT_ORDER_NO = :outOrderNo")
    List<Record> findByOutOrderNo(String outOrderNo);

    /**
     * finds the transaction records by out order
     *
     * @param transType old trans type
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record WHERE TRANS_TYPE = :transType")
    List<Record> findByTransType(String transType);

    /**
     * finds the transaction records by bizOrder
     *
     * @param bizOrderNo business order
     * @return the found transaction record
     */
    @Query("SELECT * FROM t_record WHERE BIZ_ORDER_NO = :bizOrderNo")
    List<Record> findByOrder(String bizOrderNo);

    /**
     * updates a transaction record
     *
     * @param record transaction record
     * @return the count of updated records
     */
    @Update()
    int update(Record record);

    /**
     * Get the records count of a merchant
     *
     * @return records count
     */
    @Query("SELECT COUNT (*) FROM t_record WHERE MID = :mid and TID =:tid")
    int getCountByMidTid(String mid, String tid);

    @Query("SELECT COUNT (*) FROM t_record WHERE MID = :mid and TID =:tid AND TRANS_TYPE IN ('Sale','AuthComplete','Installment','TipSale')")
    int getCountByMidTidBracSettle(String mid, String tid);

    /**
     * Get the records count
     *
     * @return records count
     */
    @Query("SELECT COUNT (*) FROM t_record")
    int getCount();
    @Query("SELECT COUNT (*) FROM t_record WHERE TRANS_TYPE IN ('Installment','VoidInstallment')")
    int getBracSettleCountEmi();

    @Query("SELECT COUNT (*) FROM t_record WHERE TRANS_TYPE IN ('Sale','VoidSale','AuthComplete','VoidAuthComplete','TipSale','VoidTipSale')")
    int getBracSettleCount();

    /**
     * get the transaction summary data
     */
    @Query("SELECT TRANS_TYPE, COUNT(*) AS COUNT, SUM(amount) AS AMOUNT  FROM t_record WHERE mid = :mid AND tid = :tid GROUP BY TRANS_TYPE ")
    List<TransactionSummary> getTransactionSummary(String mid, String tid);

    @Query("SELECT TRANS_TYPE, COUNT(*) AS COUNT, SUM(amount) AS AMOUNT  FROM t_record WHERE mid = :mid AND tid = :tid AND TRANS_TYPE IN ('Sale','AuthComplete','Installment','TipSale') GROUP BY TRANS_TYPE ")
    List<TransactionSummary> getBracTransactionSummary(String mid, String tid);

    /**
     * Get the records count with sql statement
     *
     * @param sqlQuery sql statement
     * @return records count
     */
    @RawQuery()
    int getCountBySql(SupportSQLiteQuery sqlQuery);
}
