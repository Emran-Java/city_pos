package acquire.core.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * POJO class representing Installment Program Details
 * R8/Gson safe model
 */
@Keep
public class PayFlexField63ResponseModel implements Serializable {

    @SerializedName("programId")
    private String programId;

    @SerializedName("installmentPeriod")
    private String installmentPeriod;

    @SerializedName("interestRate")
    private String interestRate;

    @SerializedName("totalInstallmentAmount")
    private String totalInstallmentAmount;

    @SerializedName("totalInterestAmount")
    private String totalInterestAmount;

    @SerializedName("firstInstallmentAmount")
    private String firstInstallmentAmount;

    @SerializedName("downpaymentAmount")
    private String downpaymentAmount;

    @SerializedName("monthlyInstallmentAmount")
    private String monthlyInstallmentAmount;

    @SerializedName("monthlyInstallmentInterest")
    private String monthlyInstallmentInterest;

    @SerializedName("vendorId")
    private String vendorId;

    @SerializedName("productId")
    private String productId;

    // Required empty constructor for Gson/R8 safety
    public PayFlexField63ResponseModel() {
    }

    // Parameterized Constructor
    public PayFlexField63ResponseModel(
            String programId,
            String installmentPeriod,
            String interestRate,
            String totalInstallmentAmount,
            String totalInterestAmount,
            String firstInstallmentAmount,
            String downpaymentAmount,
            String monthlyInstallmentAmount,
            String monthlyInstallmentInterest,
            String vendorId,
            String productId
    ) {
        this.programId = programId;
        this.installmentPeriod = installmentPeriod;
        this.interestRate = interestRate;
        this.totalInstallmentAmount = totalInstallmentAmount;
        this.totalInterestAmount = totalInterestAmount;
        this.firstInstallmentAmount = firstInstallmentAmount;
        this.downpaymentAmount = downpaymentAmount;
        this.monthlyInstallmentAmount = monthlyInstallmentAmount;
        this.monthlyInstallmentInterest = monthlyInstallmentInterest;
        this.vendorId = vendorId;
        this.productId = productId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getInstallmentPeriod() {
        return installmentPeriod;
    }

    public void setInstallmentPeriod(String installmentPeriod) {
        this.installmentPeriod = installmentPeriod;
    }

    public String getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(String interestRate) {
        this.interestRate = interestRate;
    }

    public String getTotalInstallmentAmount() {
        return totalInstallmentAmount;
    }

    public void setTotalInstallmentAmount(String totalInstallmentAmount) {
        this.totalInstallmentAmount = totalInstallmentAmount;
    }

    public String getTotalInterestAmount() {
        return totalInterestAmount;
    }

    public void setTotalInterestAmount(String totalInterestAmount) {
        this.totalInterestAmount = totalInterestAmount;
    }

    public String getFirstInstallmentAmount() {
        return firstInstallmentAmount;
    }

    public void setFirstInstallmentAmount(String firstInstallmentAmount) {
        this.firstInstallmentAmount = firstInstallmentAmount;
    }

    public String getDownpaymentAmount() {
        return downpaymentAmount;
    }

    public void setDownpaymentAmount(String downpaymentAmount) {
        this.downpaymentAmount = downpaymentAmount;
    }

    public String getMonthlyInstallmentAmount() {
        return monthlyInstallmentAmount;
    }

    public void setMonthlyInstallmentAmount(String monthlyInstallmentAmount) {
        this.monthlyInstallmentAmount = monthlyInstallmentAmount;
    }

    public String getMonthlyInstallmentInterest() {
        return monthlyInstallmentInterest;
    }

    public void setMonthlyInstallmentInterest(String monthlyInstallmentInterest) {
        this.monthlyInstallmentInterest = monthlyInstallmentInterest;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "PayFlexField63ResponseModel{" +
                "programId='" + programId + '\'' +
                ", installmentPeriod='" + installmentPeriod + '\'' +
                ", interestRate='" + interestRate + '\'' +
                ", totalInstallmentAmount='" + totalInstallmentAmount + '\'' +
                ", totalInterestAmount='" + totalInterestAmount + '\'' +
                ", firstInstallmentAmount='" + firstInstallmentAmount + '\'' +
                ", downpaymentAmount='" + downpaymentAmount + '\'' +
                ", monthlyInstallmentAmount='" + monthlyInstallmentAmount + '\'' +
                ", monthlyInstallmentInterest='" + monthlyInstallmentInterest + '\'' +
                ", vendorId='" + vendorId + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}