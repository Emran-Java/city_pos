package acquire.database.model;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.newland.sensitive_guard.convert.CryptoConverter;

/**
 * Reversal table entity.
 * <p>Note: there is only one reversal data in this table</p>
 *
 * @author Janson
 * @date 2021/1/5 17:20
 */
@Entity(tableName = "T_REVERSAL_DATA")
public class ReversalData {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int id;

    /**
     * Times that reversal was sent
     */
    @ColumnInfo(name = "HAS_SEND")
    private int hasSend;


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


    /**
     * Transaction type
     */
    @ColumnInfo(name = "TRANS_TYPE")
    private String transType;

    /**
     * Card num
     */
    @TypeConverters(CryptoConverter.class)
    @ColumnInfo(name = "CARD_NO", typeAffinity = ColumnInfo.BLOB)
    private String cardNo;

    /**
     * entry mode
     */
    @ColumnInfo(name = "ENTRY_MODE")
    private int entryMode;

    /**
     * Process code
     */
    @ColumnInfo(name = "PROCESS_CODE")
    private String processCode;

    /**
     * Amount
     */
    @ColumnInfo(name = "AMOUNT")
    private long amount;

    /**
     * Trace num
     */
    @ColumnInfo(name = "TRACE_NO")
    private String traceNo;

    /**
     * expiration  date
     */
    @TypeConverters(CryptoConverter.class)
    @ColumnInfo(name = "EXP_DATE", typeAffinity = ColumnInfo.BLOB)
    private String expDate;

    /**
     * field22
     */
    @ColumnInfo(name = "FIELD_22")
    private String field22;

    /**
     * Card serial num
     */
    @ColumnInfo(name = "CARD_SERIAL_NO")
    private String cardSn;

    /**
     * Server code
     */
    @ColumnInfo(name = "SERVER_CODE")
    private String serverCode;

    /**
     * Original auth code
     */
    @ColumnInfo(name = "ORIGINAL_AUTH_CODE")
    private String origAuthCode;

    /**
     * Currency numeric code
     */
    @ColumnInfo(name = "CURRENCY_CODE")
    private String currencyCode;

    /**
     * emv field 55
     */
    @TypeConverters(CryptoConverter.class)
    @ColumnInfo(name = "FIELD_55", typeAffinity = ColumnInfo.BLOB)
    private String field55;

    /**
     * network id
     */
    @ColumnInfo(name = "NII")
    private String nii;

    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getTraceNo() {
        return traceNo;
    }

    public void setTraceNo(String traceNo) {
        this.traceNo = traceNo;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getField22() {
        return field22;
    }

    public void setField22(String field22) {
        this.field22 = field22;
    }

    public String getCardSn() {
        return cardSn;
    }

    public void setCardSn(String cardSn) {
        this.cardSn = cardSn;
    }

    public String getServerCode() {
        return serverCode;
    }

    public void setServerCode(String serverCode) {
        this.serverCode = serverCode;
    }

    public String getOrigAuthCode() {
        return origAuthCode;
    }

    public void setOrigAuthCode(String origAuthCode) {
        this.origAuthCode = origAuthCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getField55() {
        return field55;
    }

    public void setField55(String field55) {
        this.field55 = field55;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public int getHasSend() {
        return hasSend;
    }

    public void setHasSend(int hasSend) {
        this.hasSend = hasSend;
    }

    public int getEntryMode() {
        return entryMode;
    }

    public void setEntryMode(int entryMode) {
        this.entryMode = entryMode;
    }

    public String getNii() {
        return nii;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }
}
