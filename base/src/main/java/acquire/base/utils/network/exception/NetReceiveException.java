package acquire.base.utils.network.exception;


/**
 * Receive data failed.
 *
 * @author Janson
 * @date 2020/2/12 14:56
 */
public class NetReceiveException extends Exception {

    public NetReceiveException(Exception e) {
        super(e);
    }

    public NetReceiveException(String s) {
        super(s);
    }
}
