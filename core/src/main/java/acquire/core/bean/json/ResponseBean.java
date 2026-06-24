package acquire.core.bean.json;

/**
 * A base response bean of JSON or XML.
 *
 * @author Janson
 * @date 2020/12/21 16:42
 */
public class ResponseBean {
    private String rspCode;
    private String rspMsg;

    public String getRspCode() {
        return rspCode;
    }

    public void setRspCode(String rspCode) {
        this.rspCode = rspCode;
    }

    public String getRspMsg() {
        return rspMsg;
    }

    public void setRspMsg(String rspMsg) {
        this.rspMsg = rspMsg;
    }
}
