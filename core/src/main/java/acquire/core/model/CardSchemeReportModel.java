package acquire.core.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class CardSchemeReportModel {

    @SerializedName("scheme")
    private String scheme;

    @SerializedName("saleCount")
    private int saleCount;

    @SerializedName("saleAmount")
    private long saleAmount;

    @SerializedName("voidCount")
    private int voidCount;

    @SerializedName("voidAmount")
    private long voidAmount;

    @SerializedName("transType")
    private String transType;

    @SerializedName("tranSaleTitle")
    private String tranSaleTitle = "SALE";

    @SerializedName("tranVoidTitle")
    private String tranVoidTitle = "VOID";

    @SerializedName("tranPreAuthCmpltTitle")
    private String tranPreAuthCmpltTitle = "SALE COMPLETION";

    @SerializedName("preAuthCount")
    private int preAuthCount;

    @SerializedName("preAuthAmount")
    private long preAuthAmount;

    @SerializedName("tranPreAuthVoidCmpltTitle")
    private String tranPreAuthVoidCmpltTitle = "VOID SALE COMPLETION";

    @SerializedName("voidPreAuthCount")
    private int voidPreAuthCount;

    @SerializedName("voidPreAuthAmount")
    private long voidPreAuthAmount;

    @SerializedName("tranTipAdjustTitle")
    private String tranTipAdjustTitle = "TIP ADJUST";

    @SerializedName("tranTipAdjistVoidTitle")
    private String tranTipAdjistVoidTitle = "VOID TIP ADJUST";

    @SerializedName("preTipCount")
    private int preTipCount;

    @SerializedName("voidTipAmount")
    private long voidTipAmount;

    // Empty constructor required for Gson/R8 safety
    public CardSchemeReportModel() {
    }

    public int getVoidPreAuthCount() {
        return voidPreAuthCount;
    }

    public void setVoidPreAuthCount(int voidPreAuthCount) {
        this.voidPreAuthCount = voidPreAuthCount;
    }

    public long getVoidPreAuthAmount() {
        return voidPreAuthAmount;
    }

    public void setVoidPreAuthAmount(long voidPreAuthAmount) {
        this.voidPreAuthAmount = voidPreAuthAmount;
    }

    public int getPreAuthCount() {
        return preAuthCount;
    }

    public void setPreAuthCount(int preAuthCount) {
        this.preAuthCount = preAuthCount;
    }

    public long getPreAuthAmount() {
        return preAuthAmount;
    }

    public void setPreAuthAmount(long preAuthAmount) {
        this.preAuthAmount = preAuthAmount;
    }

    public String getTranTipAdjustTitle() {
        return tranTipAdjustTitle;
    }

    public void setTranTipAdjustTitle(String tranTipAdjustTitle) {
        this.tranTipAdjustTitle = tranTipAdjustTitle;
    }

    public String getTranTipAdjistVoidTitle() {
        return tranTipAdjistVoidTitle;
    }

    public void setTranTipAdjistVoidTitle(String tranTipAdjistVoidTitle) {
        this.tranTipAdjistVoidTitle = tranTipAdjistVoidTitle;
    }

    public int getPreTipCount() {
        return preTipCount;
    }

    public void setPreTipCount(int preTipCount) {
        this.preTipCount = preTipCount;
    }

    public long getVoidTipAmount() {
        return voidTipAmount;
    }

    public void setVoidTipAmount(long voidTipAmount) {
        this.voidTipAmount = voidTipAmount;
    }

    public String getTranPreAuthCmpltTitle() {
        return tranPreAuthCmpltTitle;
    }

    public void setTranPreAuthCmpltTitle(String tranPreAuthCmpltTitle) {
        this.tranPreAuthCmpltTitle = tranPreAuthCmpltTitle;
    }

    public String getTranPreAuthVoidCmpltTitle() {
        return tranPreAuthVoidCmpltTitle;
    }

    public void setTranPreAuthVoidCmpltTitle(String tranPreAuthVoidCmpltTitle) {
        this.tranPreAuthVoidCmpltTitle = tranPreAuthVoidCmpltTitle;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getTranVoidTitle() {
        return tranVoidTitle;
    }

    public void setTranVoidTitle(String tranVoidTitle) {
        this.tranVoidTitle = tranVoidTitle;
    }

    public String getTranSaleTitle() {
        return tranSaleTitle;
    }

    public void setTranSaleTitle(String tranSaleTitle) {
        this.tranSaleTitle = tranSaleTitle;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public int getSaleCount() {
        return saleCount;
    }

    public void setSaleCount(int saleCount) {
        this.saleCount = saleCount;
    }

    public long getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(long saleAmount) {
        this.saleAmount = saleAmount;
    }

    public int getVoidCount() {
        return voidCount;
    }

    public void setVoidCount(int voidCount) {
        this.voidCount = voidCount;
    }

    public long getVoidAmount() {
        return voidAmount;
    }

    public void setVoidAmount(long voidAmount) {
        this.voidAmount = voidAmount;
    }
}