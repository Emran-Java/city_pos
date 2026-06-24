package acquire.core.model;

import java.util.List;

public class SchemeGroup {
    private String schemeTitle;
    private int totalSchemeWiseTranCount;
    private long totalSchemeWiseTranAmount;
    private long totalSchemeWiseTranTipAmount;
    private List<GroupByTranType> groupByTranType;


    // Getters & Setters

    public long getTotalSchemeWiseTranTipAmount() {
        return totalSchemeWiseTranTipAmount;
    }

    public void setTotalSchemeWiseTranTipAmount(long totalSchemeWiseTranTipAmount) {
        this.totalSchemeWiseTranTipAmount = totalSchemeWiseTranTipAmount;
    }

    public String getSchemeTitle() {
        return schemeTitle;
    }

    public void setSchemeTitle(String schemeTitle) {
        this.schemeTitle = schemeTitle;
    }

    public int getTotalSchemeWiseTranCount() {
        return totalSchemeWiseTranCount;
    }

    public void setTotalSchemeWiseTranCount(int totalSchemeWiseTranCount) {
        this.totalSchemeWiseTranCount = totalSchemeWiseTranCount;
    }

    public long getTotalSchemeWiseTranAmount() {
        return totalSchemeWiseTranAmount;
    }

    public void setTotalSchemeWiseTranAmount(long totalSchemeWiseTranAmount) {
        this.totalSchemeWiseTranAmount = totalSchemeWiseTranAmount;
    }

    public List<GroupByTranType> getGroupByTranType() {
        return groupByTranType;
    }

    public void setGroupByTranType(List<GroupByTranType> groupByTranType) {
        this.groupByTranType = groupByTranType;
    }
}