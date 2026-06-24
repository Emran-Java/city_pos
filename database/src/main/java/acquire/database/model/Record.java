package acquire.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.newland.sensitive_guard.convert.CryptoConverter;


/**
 * Transaction record entity.
 *
 * @author Janson
 * @date 2021/1/5 17:18
 */
@Entity(tableName = "T_RECORD")
public class Record {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int id;

    /**
     * Merchant id
     */
    @ColumnInfo(name = "MID")
    private String mid;

    //Brac Add
    /**
     * Is OnUs
     */
    @ColumnInfo(name = "IS_ON_US")
    private boolean isOnUs;

    /**
     * Is delete: After soft delete the record first time we set true.
     *
     */
    @ColumnInfo(name = "IS_DELETE")
    private boolean isDelete;

    /**
     * DisplayTitle: We set title here to show in "ReportDetails" card section and print card section
     */
    @ColumnInfo(name = "DISPLAY_TITLE")
    private String displayTitle = "";

    /**
     * Card Title: this title will be come from INI param file, when the bean is in range as call the card is UN-US card
     * default this value is empty string
     */
    @ColumnInfo(name = "CARD_TITLE")
    private String cardTitle = "";
    //--------------

    /**
     * Date: 20260504 1228
     * Card holder name : We add this field to record Card Holder name, because some time we cant get
     * card holder name after SALE transection in field55. Card holder name required for Print Footer area.
     *
     */
    @ColumnInfo(name = "CARD_HOLDER_NAME")
    private String cardHolderName = "";
    //--------------


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
     * Process code
     */
    @ColumnInfo(name = "PROCESS_CODE")
    private String processCode;

    /**
     * Status
     */
    @ColumnInfo(name = "STATUS")
    private int status;

    /**
     * Card number
     */
    @TypeConverters(CryptoConverter.class)
    @ColumnInfo(name = "CARD_NO", typeAffinity = ColumnInfo.BLOB)
    private String cardNo;

    /**
     * Entry mode
     */
    @ColumnInfo(name = "ENTRY_MODE")
    private int entryMode;

    /**
     * Amount
     */
    @ColumnInfo(name = "AMOUNT")
    private long amount;

    /**
     * Tips Amount.(12)
     */
    @ColumnInfo(name = "TIP_AMOUNT")
    private long tipAmount;

    /**
     * Cash Amount.(12)
     */
    @ColumnInfo(name = "CASH_AMOUNT")
    private long cashAmount;
    /**
     * Base Amount.If there is a tip or cash, it indicates the amount excluding the tip and cash.
     */
    @ColumnInfo(name = "BASE_AMOUNT")
    private long baseAmount;

    /**
     * Trace No
     */
    @ColumnInfo(name = "TRACE_NO")
    private String traceNo;


    /**
     * Transaction time
     * <P>HHmmss</P>
     */
    @ColumnInfo(name = "TIME")
    private String time;


    /**
     * Transaction date
     * <P>yyyyMMdd</P>
     */
    @ColumnInfo(name = "DATE")
    private String date;

    /**
     * expiry  date
     * <P>yyMM</P>
     */
    @TypeConverters(CryptoConverter.class)
    @ColumnInfo(name = "EXP_DATE", typeAffinity = ColumnInfo.BLOB)
    private String expDate;

    /**
     * field 22
     */
    @ColumnInfo(name = "FIELD_22")
    private String field22;


    /**
     * Reference No
     */
    @ColumnInfo(name = "REFER_NO")
    private String referNo;

    /**
     * Auth code
     */
    @ColumnInfo(name = "AUTH_CODE")
    private String authCode;

    /**
     * Response code
     */
    @ColumnInfo(name = "RESPONSE_CODE")
    private String responseCode;

    /**
     * Batch num
     */
    @ColumnInfo(name = "BATCH_NO")
    private String batchNo;

    /**
     * Original batch num
     */
    @ColumnInfo(name = "ORIGINAL_BATCH")
    private String origBatch;

    /**
     * Original trace num
     */
    @ColumnInfo(name = "ORIGINAL_TRACE_NO")
    private String origTraceNo;

    /**
     * Original auth code
     */
    @ColumnInfo(name = "ORIGINAL_AUTH_CODE")
    private String origAuthCode;

    /**
     * Original Reference No
     */
    @ColumnInfo(name = "ORIGINAL_REFER_NO")
    private String origReferNo;

    /**
     * Card scheme
     */
    @ColumnInfo(name = "CARD_SCHEME")
    private String cardScheme;

    /**
     * Batch up flag.
     */
    @ColumnInfo(name = "BATCH_UP_FLAG")
    private boolean batchUpFlag;

    /**
     * Original  date,yyyyMMdd
     */
    @ColumnInfo(name = "ORIGINAL_DATE")
    private String origDate;

    /**
     * Currency numeric code
     */
    @ColumnInfo(name = "CURRENCY_CODE")
    private String currencyCode;

    /**
     * Qr pay code of scanning payment
     */
    @ColumnInfo(name = "QR_PAY_CODE")
    private String qrPayCode;
    /**
     * Business order number
     */
    @ColumnInfo(name = "BIZ_ORDER_NO")
    private String bizOrderNo;
    /**
     * field55
     */
    @TypeConverters(CryptoConverter.class)
    @ColumnInfo(name = "FIELD_55", typeAffinity = ColumnInfo.BLOB)
    private String field55;

    /**
     * Sign bitmap path
     */
    @ColumnInfo(name = "SIGN_PATH")
    private String signPath;
    /**
     * free sign
     */
    @ColumnInfo(name = "FREE_SIGN")
    private boolean freeSign;
    /**
     * free PIN
     */
    @ColumnInfo(name = "FREE_PIN")
    private boolean freePin;

    /**
     * Out order num
     */
    @ColumnInfo(name = "OUT_ORDER_NO")
    private String outOrderNo;

    /**
     * Card serial（0x5F34）
     */
    @ColumnInfo(name = "CARD_SN")
    private String cardSn;

    /**
     * EMV data
     */
    @ColumnInfo(name = "EMV_PRINT_DATA")
    private String emvPrintData;
    /**
     * Print remarks
     */
    @ColumnInfo(name = "REMARKS")
    private String remarks;

    /**
     * DCC foreign Currency Numeric code
     */
    @ColumnInfo(name = "FOREIGN_CURRENCY")
    private String foreignCurrency;
    /**
     * DCC foreign amount
     */
    @ColumnInfo(name = "FOREIGN_AMOUNT")
    private long foreignAmount;
    /**
     * DCC conversion rate
     */
    @ColumnInfo(name = "CONVERSION_RATE")
    private String conversionRate;

    /**
     * DCC markup rate
     */
    @ColumnInfo(name = "MARKUP_RATE")
    private String markupRate;

    /**
     * Digital Receipt No
     */
    @ColumnInfo(name = "DIGITAL_RECEIPT_NO")
    private String digitalReceiptNo;

    /**
     * Digital Receipt URL
     */
    @ColumnInfo(name = "DIGITAL_RECEIPT_URL")
    private String digitalReceiptUrl;

    /**
     * NPI Digital Receipt URL （For Demo）
     */
    @ColumnInfo(name = "NPI_DIGITAL_RECEIPT_URL")
    private String npiDigitalReceiptUrl;

    /**
     * Fly Receipt Digital Receipt URL （For Demo）
     */
    @ColumnInfo(name = "TOMS_DIGITAL_RECEIPT_URL")
    private String tomsDigitalReceiptUrl;

    @ColumnInfo(name = "UUID")
    private String uuid;

    @ColumnInfo(name = "FIELD_63")
    private String field63;

    public String getField63() {
        return field63;
    }

    public void setField63(String field63) {
        this.field63 = field63;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public String getCardTitle() {
        return cardTitle;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public boolean isOnUs() {
        return isOnUs;
    }

    public void setOnUs(boolean onUs) {
        isOnUs = onUs;
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }

//    public String getDisplayTitle() {
//        return displayTitle;
//    }
//
//    public String isCardTitle() {
//        return cardTitle;
//    }

    public void setCardTitle(String cardTitle) {
        if(cardTitle!=null)
            this.cardTitle = cardTitle;
    }


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

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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


    public String getReferNo() {
        return referNo;
    }

    public void setReferNo(String referNo) {
        this.referNo = referNo;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getOrigBatch() {
        return origBatch;
    }

    public void setOrigBatch(String origBatch) {
        this.origBatch = origBatch;
    }

    public String getOrigTraceNo() {
        return origTraceNo;
    }

    public void setOrigTraceNo(String origTraceNo) {
        this.origTraceNo = origTraceNo;
    }

    public String getOrigAuthCode() {
        return origAuthCode;
    }

    public void setOrigAuthCode(String origAuthCode) {
        this.origAuthCode = origAuthCode;
    }

    public String getOrigReferNo() {
        return origReferNo;
    }

    public void setOrigReferNo(String origReferNo) {
        this.origReferNo = origReferNo;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public void setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
    }

    public boolean isBatchUpFlag() {
        return batchUpFlag;
    }

    public void setBatchUpFlag(boolean batchUpFlag) {
        this.batchUpFlag = batchUpFlag;
    }

    public String getOrigDate() {
        return origDate;
    }

    public void setOrigDate(String origDate) {
        this.origDate = origDate;
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

    public String getOutOrderNo() {
        return outOrderNo;
    }

    public void setOutOrderNo(String outOrderNo) {
        this.outOrderNo = outOrderNo;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getQrPayCode() {
        return qrPayCode;
    }

    public void setQrPayCode(String qrPayCode) {
        this.qrPayCode = qrPayCode;
    }

    public String getBizOrderNo() {
        return bizOrderNo;
    }

    public void setBizOrderNo(String bizOrderNo) {
        this.bizOrderNo = bizOrderNo;
    }

    public String getSignPath() {
        return signPath;
    }

    public void setSignPath(String signPath) {
        this.signPath = signPath;
    }

    public boolean isFreeSign() {
        return freeSign;
    }

    public void setFreeSign(boolean freeSign) {
        this.freeSign = freeSign;
    }

    public boolean isFreePin() {
        return freePin;
    }

    public void setFreePin(boolean freePin) {
        this.freePin = freePin;
    }

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public int getEntryMode() {
        return entryMode;
    }

    public void setEntryMode(int entryMode) {
        this.entryMode = entryMode;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getCardSn() {
        return cardSn;
    }

    public void setCardSn(String cardSn) {
        this.cardSn = cardSn;
    }

    public long getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(long tipAmount) {
        this.tipAmount = tipAmount;
    }

    public long getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(long baseAmount) {
        this.baseAmount = baseAmount;
    }

    public String getEmvPrintData() {
        return emvPrintData;
    }

    public void setEmvPrintData(String emvPrintData) {
        this.emvPrintData = emvPrintData;
    }

    public long getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(long cashAmount) {
        this.cashAmount = cashAmount;
    }

    public String getForeignCurrency() {
        return foreignCurrency;
    }

    public void setForeignCurrency(String foreignCurrency) {
        this.foreignCurrency = foreignCurrency;
    }

    public long getForeignAmount() {
        return foreignAmount;
    }

    public void setForeignAmount(long foreignAmount) {
        this.foreignAmount = foreignAmount;
    }

    public String getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(String conversionRate) {
        this.conversionRate = conversionRate;
    }

    public String getMarkupRate() {
        return markupRate;
    }

    public void setMarkupRate(String markupRate) {
        this.markupRate = markupRate;
    }

    public String getNpiDigitalReceiptUrl() {
        return npiDigitalReceiptUrl;
    }

    public void setNpiDigitalReceiptUrl(String npiReceiptUrl) {
        this.npiDigitalReceiptUrl = npiReceiptUrl;
    }

    public String getDigitalReceiptUrl() {
        return digitalReceiptUrl;
    }

    public void setDigitalReceiptUrl(String digitalReceiptUrl) {
        this.digitalReceiptUrl = digitalReceiptUrl;
    }

    public String getDigitalReceiptNo() {
        return digitalReceiptNo;
    }

    public void setDigitalReceiptNo(String digitalReceiptNo) {
        this.digitalReceiptNo = digitalReceiptNo;
    }

    public String getTomsDigitalReceiptUrl() {
        return tomsDigitalReceiptUrl;
    }

    public void setTomsDigitalReceiptUrl(String tomsDigitalReceiptUrl) {
        this.tomsDigitalReceiptUrl = tomsDigitalReceiptUrl;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
