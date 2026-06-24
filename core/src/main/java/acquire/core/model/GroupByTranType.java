package acquire.core.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class GroupByTranType {

    @SerializedName("title")
    private String title;

    @SerializedName("count")
    private int count;

    @SerializedName("tranType")
    private String tranType;

    @SerializedName("amount")
    private long amount;

    @SerializedName("tipAmount")
    private long tipAmount;

    @SerializedName("isSubtract")
    private boolean isSubtract;

    @SerializedName("isCountable")
    private boolean isCountable;

    @SerializedName("isShowTip")
    private boolean isShowTip;

    @SerializedName("isCalculate")
    private boolean isCalculate;

    // Required empty constructor for Gson/R8 safety
    public GroupByTranType() {
    }

    // Getters & Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTranType() {
        return tranType;
    }

    public void setTranType(String tranType) {
        this.tranType = tranType;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(long tipAmount) {
        this.tipAmount = tipAmount;
    }

    public boolean isSubtract() {
        return isSubtract;
    }

    public void setSubtract(boolean subtract) {
        isSubtract = subtract;
    }

    public boolean isCountable() {
        return isCountable;
    }

    public void setCountable(boolean countable) {
        isCountable = countable;
    }

    public boolean isShowTip() {
        return isShowTip;
    }

    public void setShowTip(boolean showTip) {
        isShowTip = showTip;
    }

    public boolean isCalculate() {
        return isCalculate;
    }

    public void setCalculate(boolean calculate) {
        isCalculate = calculate;
    }
}