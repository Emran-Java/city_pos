package acquire.core.trans.pack.json.npi_sale;

public class HCESaleResp {
    public static final int STATE_PIAD = 1 ;

    int code ;
    String msg ;
    int state ;

    String url ;

    public HCESaleResp(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getState(){
        return state ;
    }

    public int getCode(){
        return code ;
    }

    public String getMsg(){
        return msg ;
    }

}
