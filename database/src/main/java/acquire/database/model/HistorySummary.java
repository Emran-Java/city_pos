package acquire.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


/**
 * History Transactions that have been settled.
 *
 * @author Janson
 * @date 2023/12/12 16:23
 */
@Entity(tableName = "T_HISTORY_SUMMARY")
public class HistorySummary {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int id;

    /**
     * Merchant id
     */
    @ColumnInfo(name = "MID")
    private String mid;
    /**
     * Terminal id
     */
    @ColumnInfo(name = "TID")
    private String tid;

    @ColumnInfo(name = "TXN_TYPE")
    private String txnType;
    /**
     * Amount
     */
    @ColumnInfo(name = "AMOUNT")
    private long amount;
    @ColumnInfo(name = "COUNT")
    private int count;

    /**
     * Batch num
     */
    @ColumnInfo(name = "BATCH_NO")
    private String batchNo;

    /**
     * The date of settlement,yyyyMMdd
     */
    @ColumnInfo(name = "SETTLE_DATE")
    private String settleDate;
    /**
     * The time of settlement,HHmmss
     */
    @ColumnInfo(name = "SETTLE_TIME")
    private String settleTime;

    @ColumnInfo(name = "SETTLE_EQUAL")
    private boolean settleEqual;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }


    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }


    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }


    public String getSettleDate() {
        return settleDate;
    }

    public void setSettleDate(String settleDate) {
        this.settleDate = settleDate;
    }

    public String getSettleTime() {
        return settleTime;
    }

    public void setSettleTime(String settleTime) {
        this.settleTime = settleTime;
    }

    public boolean isSettleEqual() {
        return settleEqual;
    }

    public void setSettleEqual(boolean settleEqual) {
        this.settleEqual = settleEqual;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
