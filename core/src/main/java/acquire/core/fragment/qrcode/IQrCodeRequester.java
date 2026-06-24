package acquire.core.fragment.qrcode;

/**
 * A request interface for {@link QrCodeFragment}
 *
 * @author Janson
 * @date 2022/12/21 9:20
 */
public interface IQrCodeRequester {
    String requestQrCode();
    boolean queryResult();
}
