package acquire.base.utils.network.exception;


import acquire.base.BaseApplication;
import acquire.base.R;

/**
 * HTTP failed, such as 404
 *
 * @author Janson
 * @date 2020/2/12 14:56
 */
public class NetHttpCodeException extends Exception {
    private final int httpCode;

    public NetHttpCodeException(int httpCode, String message) {
        super(BaseApplication.getAppString(R.string.base_http_code_error)+httpCode+","+message);
        this.httpCode = httpCode;
    }

    public int getHttpCode() {
        return httpCode;
    }

}
