package acquire.core.bean;

import java.util.List;

import acquire.core.model.SchemeGroup;
import acquire.database.bean.TransactionSummary;

/**
 * @author Janson
 * @date 2023/12/13 10:41
 */
public class SettleReceiptBean {

    private String printStartTitle;
    private String printEndTitle;
    private String merchantName;

    private String merchantType;
    private String mid;
    private String tid;

    private String batch;
    private String settleDate;
    private String settleTime;
    private boolean settleEqual;
    private List<TransactionSummary> transactionSummaries;


    private List<SchemeGroup> schemeGroupList;

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getPrintStartTitle() {
        return printStartTitle;
    }

    public void setPrintStartTitle(String printStartTitle) {
        this.printStartTitle = printStartTitle;
    }

    public String getPrintEndTitle() {
        return printEndTitle;
    }

    public void setPrintEndTitle(String printEndTitle) {
        this.printEndTitle = printEndTitle;
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

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
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

    public List<TransactionSummary> getTransactionSummaries() {
        return transactionSummaries;
    }

    public void setTransactionSummaries(List<TransactionSummary> transactionSummaries) {
        this.transactionSummaries = transactionSummaries;
    }

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }


    public List<SchemeGroup> getSchemeGroupList() {
        return schemeGroupList;
    }

    public void setSchemeGroupList(List<SchemeGroup> schemeGroupList) {
        this.schemeGroupList = schemeGroupList;
    }
}
