package acquire.database.bean;

import androidx.room.ColumnInfo;

/**
 * transaction summary for{@link Record}
 *
 * @author Janson
 * @date 2025/3/6 9:36
 */
public class TransactionSummary {
    /**
     * Transaction type. It must be same as {@link Record}'s transType
     */
    @ColumnInfo(name = "TRANS_TYPE")
    private String transType;
    /**
     * Amount. It must be same as {@link Record}'s amount
     */
    @ColumnInfo(name = "AMOUNT")
    private long amount;

    /**
     * Transaction count
     */
    @ColumnInfo(name = "COUNT")
    private int count;


    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
