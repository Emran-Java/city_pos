package acquire.core.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class ConnectivityModel {

    @SerializedName("operatorCode")
    private String operatorCode;

    @SerializedName("operatorName")
    private String operatorName;

    @SerializedName("serviceIp1")
    private String serviceIp1;

    @SerializedName("port1")
    private String port1;

    @SerializedName("serviceIp2")
    private String serviceIp2;

    @SerializedName("port2")
    private String port2;

    @SerializedName("apn")
    private String apn;

    @SerializedName("userName")
    private String userName;

    @SerializedName("userPwd")
    private String userPwd;

    @SerializedName("mode")
    private String mode;

    @SerializedName("ussd")
    private String ussd;

    @SerializedName("localIp")
    private String localIp;

    @SerializedName("mask")
    private String mask;

    @SerializedName("gateway")
    private String gateway;

    // Required empty constructor for Gson/R8 safety
    public ConnectivityModel() {
    }

    public String getOperatorCode() {
        return operatorCode;
    }

    public void setOperatorCode(String operatorCode) {
        this.operatorCode = operatorCode;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getServiceIp1() {
        return serviceIp1;
    }

    public void setServiceIp1(String serviceIp1) {
        this.serviceIp1 = serviceIp1;
    }

    public String getPort1() {
        return port1;
    }

    public void setPort1(String port1) {
        this.port1 = port1;
    }

    public String getServiceIp2() {
        return serviceIp2;
    }

    public void setServiceIp2(String serviceIp2) {
        this.serviceIp2 = serviceIp2;
    }

    public String getPort2() {
        return port2;
    }

    public void setPort2(String port2) {
        this.port2 = port2;
    }

    public String getApn() {
        return apn;
    }

    public void setApn(String apn) {
        this.apn = apn;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getUssd() {
        return ussd;
    }

    public void setUssd(String ussd) {
        this.ussd = ussd;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    @Override
    public String toString() {
        return "ConnectivityModel{" +
                "operatorCode='" + operatorCode + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", serviceIp1='" + serviceIp1 + '\'' +
                ", port1='" + port1 + '\'' +
                ", serviceIp2='" + serviceIp2 + '\'' +
                ", port2='" + port2 + '\'' +
                ", apn='" + apn + '\'' +
                ", userName='" + userName + '\'' +
                ", userPwd='" + userPwd + '\'' +
                ", mode='" + mode + '\'' +
                ", ussd='" + ussd + '\'' +
                ", localIp='" + localIp + '\'' +
                ", mask='" + mask + '\'' +
                ", gateway='" + gateway + '\'' +
                '}';
    }
}