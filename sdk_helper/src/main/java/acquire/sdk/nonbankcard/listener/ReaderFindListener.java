package acquire.sdk.nonbankcard.listener;

import acquire.sdk.nonbankcard.BCardReader;

/**
 * Listener of {@link BCardReader}
 *
 * @author Janson
 * @date 2018/5/24 1:12
 */
public interface ReaderFindListener{
    void onTimeout();

    void onCancel();

    void onError(int code, String message);

    void onFindMagCard(byte[] track1,byte[] track2,byte[] track3);

    void onFindContactCard();

    void onFindContactlessCard();
}
