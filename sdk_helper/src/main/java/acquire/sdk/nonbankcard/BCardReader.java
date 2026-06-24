package acquire.sdk.nonbankcard;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.card.contactless.ContactlessCardInfo;
import com.newland.nsdk.core.api.common.card.contactless.ContactlessCardType;
import com.newland.nsdk.core.api.common.card.magcard.MagCardInfo;
import com.newland.nsdk.core.api.common.cardreader.CardReaderListener;
import com.newland.nsdk.core.api.common.cardreader.CardReaderParameters;
import com.newland.nsdk.core.api.common.cardreader.CardType;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.internal.cardreader.CardReader;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import acquire.base.utils.LoggerUtils;
import acquire.sdk.nonbankcard.listener.ReaderFindListener;

/**
 * Card Reader tool.
 * <p>It can identify the card reading method.
 * <p>If swiping, it can directly obtain the card information; </p>
 * <p>If ard insertion or book card, you can call the specified card reader interface to read.</p>
 *
 * @author Janson
 * @date 2022/11/23 14:48
 * @since 3.7
 */
public class BCardReader {

    private final CardReader mCardReader;

    public BCardReader() {
        mCardReader = (CardReader) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.CARD_READER);
    }

    public void start(int timeoutSec,ReaderFindListener listener) {
        CardType[] cardTypes = new CardType[]{CardType.MAG_CARD, CardType.CONTACT_CARD, CardType.CONTACTLESS_CARD};
        CardReaderParameters parameter = new CardReaderParameters();
        parameter.setContactlessCardTypes(new ContactlessCardType[]{ContactlessCardType.TYPE_F, ContactlessCardType.TYPE_A, ContactlessCardType.TYPE_B});

        try {
            mCardReader.openCardReader(cardTypes, timeoutSec, parameter, new CardReaderListener() {
                @Override
                public void onTimeout() {
                    listener.onTimeout();
                }

                @Override
                public void onCancel() {
                    listener.onCancel();
                }

                @Override
                public void onError(int code, String message) {
                    listener.onError(code, message);
                }

                @Override
                public void onFindMagCard(MagCardInfo magCardInfo) {
                    listener.onFindMagCard(magCardInfo.getTrack1Data(), magCardInfo.getTrack2Data(), magCardInfo.getTrack3Data());
                }

                @Override
                public void onFindContactCard() {
                    listener.onFindContactCard();
                }

                @Override
                public void onFindContactlessCard(ContactlessCardType contactlessCardType, ContactlessCardInfo contactlessCardInfo) {
                    listener.onFindContactlessCard();
                }
            });
        } catch (NSDKException e) {
            LoggerUtils.e("BCardReader openCardReader failed!",e);
            listener.onError(Integer.MAX_VALUE, e.getMessage());
        }
    }

    public void close() {
        try {
            mCardReader.cancelCardReader();
        } catch (NSDKException e) {
            LoggerUtils.e("BCardReader close failed!",e);
        }
    }

    public boolean contactCardExist() {
        try {
            return mCardReader.isCardInserted();
        } catch (NSDKException e) {
            LoggerUtils.e("BCardReader contactCardExist failed!",e);
            return false;
        }
    }

    public boolean contactlessCardExist() {
        try {
            return mCardReader.isCardPresent();
        } catch (NSDKException e) {
            LoggerUtils.e("BCardReader contactlessCardExist failed!",e);
            return false;
        }

    }
}
