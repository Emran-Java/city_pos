package acquire.sdk.emv.bean;

import com.newland.sdk.emvl3.api.common.configuration.DRLAmexExpress;
import com.newland.sdk.emvl3.api.common.configuration.DRLPaywave;

/**
 * Pack EMV Drl tag data
 *
 * @author Janson
 * @date 2025/2/25 11:16
 */
public class EmvDrlTagBean {
    private boolean amex;
    private byte[] clTransLimit;
    private byte[] clOfflineLimit;
    private byte[] cvmLimit;

    //AMEX
    private int drlExist;
    private int drlId;
    private int limitExist;
    private byte[] reserve;

    //Paywave
    private byte[] appId;
    private int clLimitExist;

    public void setAmex(boolean amex) {
        this.amex = amex;
    }

    public void setClTransLimit(byte[] clTransLimit) {
        this.clTransLimit = clTransLimit;
    }

    public void setClOfflineLimit(byte[] clOfflineLimit) {
        this.clOfflineLimit = clOfflineLimit;
    }

    public void setCvmLimit(byte[] cvmLimit) {
        this.cvmLimit = cvmLimit;
    }

    public void setDrlExist(int drlExist) {
        this.drlExist = drlExist;
    }

    public void setDrlId(int drlId) {
        this.drlId = drlId;
    }

    public void setLimitExist(int limitExist) {
        this.limitExist = limitExist;
    }

    public void setReserve(byte[] reserve) {
        this.reserve = reserve;
    }

    public void setAppId(byte[] appId) {
        this.appId = appId;
    }

    public void setClLimitExist(int clLimitExist) {
        this.clLimitExist = clLimitExist;
    }

    public byte[] toByteArray() {
        if (amex) {
            DRLAmexExpress drlAmexExpress = new DRLAmexExpress();
            drlAmexExpress.setDrlEnable((byte) drlExist);
            drlAmexExpress.setDrlID((byte) drlId);
            drlAmexExpress.setLimitExist((byte) limitExist);
            drlAmexExpress.setClTransLimit(clTransLimit);
            drlAmexExpress.setClFloorLimit(clOfflineLimit);
            drlAmexExpress.setClCvmLimit(cvmLimit);
            return drlAmexExpress.toByteArray();
        } else {
            DRLPaywave drlPaywave = new DRLPaywave();
            drlPaywave.setApplicationID(appId);
            drlPaywave.setLimitExist((byte) clLimitExist);
            drlPaywave.setClTransLimit(clTransLimit);
            drlPaywave.setClFloorLimit(clOfflineLimit);
            drlPaywave.setClCvmLimit(cvmLimit);
            return drlPaywave.toByteArray();
        }
    }

}
