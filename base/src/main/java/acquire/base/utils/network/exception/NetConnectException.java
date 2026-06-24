package acquire.base.utils.network.exception;


/**
 * Connect failed
 *
 * @author Janson
 * @date 2020/2/12 14:56
 */
public class NetConnectException extends Exception {

    public NetConnectException(Exception e) {
        super(e);
    }

    public NetConnectException(String s) {
        super(s);
    }
}
