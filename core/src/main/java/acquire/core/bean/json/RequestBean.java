package acquire.core.bean.json;

/**
 * A base request bean of JSON or XML.
 *
 * @author Janson
 * @date 2020/12/21 16:42
 */
public class RequestBean {
    private String merchantId;
    private String terminalId;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }
}
