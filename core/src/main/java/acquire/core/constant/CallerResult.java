package acquire.core.constant;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The result status of communication
 *
 * @author Janson
 * @date 2020/7/28 15:15
 */
public class CallerResult {
    /**
     * Success
     */
    public final static int OK = 0;
    /**
     * Handle request data failed before sending data
     */
    public final static int FAIL_REQUEST_DATA_ERROR = -1;
    /**
     * Connection failed before sending data
     */
    public final static int FAIL_NET_CONNECT = -2;
    /**
     * Receive failed after sending data
     */
    public final static int FAIL_NET_RECV = -3;
    /**
     * Handle response data failed after receiving data
     */
    public final static int FAIL_RESPONSE_DATA_ERROR = -4;
    
    @IntDef(value = {OK,FAIL_REQUEST_DATA_ERROR, FAIL_NET_CONNECT, FAIL_NET_RECV,FAIL_RESPONSE_DATA_ERROR})
    @Target({ElementType.PARAMETER,ElementType.METHOD,ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CallerResultDef{}
} 