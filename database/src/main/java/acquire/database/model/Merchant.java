package acquire.database.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import acquire.base.utils.LoggerUtils;

/**
 * Merchant table entity
 *
 * @author Janson
 * @date 2021/3/12 11:02
 */
@Entity(tableName = "T_MERCHANT")
public class Merchant implements Cloneable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int id;
    /**
     * Type. Currently is same as card scheme
     */
    @ColumnInfo(name = "TYPE")
    private String type;

    /**
     * Merchant Name.
     */
    @ColumnInfo(name = "MERCHANT_NAME")
    private String merchantName;


    /**
     * Merchant id (MERCHANTID)
     */
    @ColumnInfo(name = "MID")
    private String mid;

    /**
     * Terminal id
     */
    @ColumnInfo(name = "TID")
    private String tid;
    /**
     * Batch num
     */
    @ColumnInfo(name = "BATCH_NO")
    private String batchNo;

    /**
     * Emi Batch num
     */
    @ColumnInfo(name = "EMI_BATCH_NO")
    private String emiBatchNo;

    @ColumnInfo(name = "COMM_TIMEOUT")
    private int commTimeout = 30;

    @ColumnInfo(name = "IP")
    private String ip;

    @ColumnInfo(name = "PORT")
    private int port;

    @ColumnInfo(name = "TPDU")
    private String tpdu;

    @ColumnInfo(name = "NII")
    private String nii;

    @ColumnInfo(name = "MASTER_KEY_INDEX")
    private int masterKeyIndex;

    @ColumnInfo(name = "ALGORITHM_TYPE")
    private int algorithm;

    /**
     * Settle balance
     */
    @ColumnInfo(name = "SETTLE_EQUAL")
    private boolean settleEqual;
    /**
     * The settle step
     */
    @ColumnInfo(name = "SETTLE_STEP")
    private int settleStep;
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

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
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

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }


    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }


    //*** Brac ***
    public String getEmiBatchNo() {
        return batchNo;
    }

    public void setEmiBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }
    //--------------

    public int getSettleStep() {
        return settleStep;
    }

    public void setSettleStep(int settleStep) {
        this.settleStep = settleStep;
    }

    public boolean isSettleEqual() {
        return settleEqual;
    }

    public void setSettleEqual(boolean settleEqual) {
        this.settleEqual = settleEqual;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getCommTimeout() {
        return commTimeout;
    }

    public void setCommTimeout(int commTimeout) {
        this.commTimeout = commTimeout;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTpdu() {
        return tpdu;
    }

    public void setTpdu(String tpdu) {
        this.tpdu = tpdu;
    }

    public String getNii() {
        return nii;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public int getMasterKeyIndex() {
        return masterKeyIndex;
    }

    public void setMasterKeyIndex(int masterKeyIndex) {
        this.masterKeyIndex = masterKeyIndex;
    }

    public int getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(int algorithm) {
        this.algorithm = algorithm;
    }

    @NonNull
    @Override
    public Merchant clone() {
        try {
            return (Merchant) super.clone();
        } catch (CloneNotSupportedException e) {
            LoggerUtils.e("Copy Merchant failed!",e);
            return new Merchant();
        }
    }

    @Override
    public String toString() {
        return "Merchant{" +
                "type='" + type + '\'' +
                ", mid='" + mid + '\'' +
                ", merchantName='" + merchantName + '\'' +
                ", tid='" + tid + '\'' +
                ", batchNo='" + batchNo + '\'' +
                ", commTimeout=" + commTimeout +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", tpdu='" + tpdu + '\'' +
                ", nii='" + nii + '\'' +
                ", masterKeyIndex=" + masterKeyIndex +
                ", algorithm=" + algorithm +
                '}';
    }
}
