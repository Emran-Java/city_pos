package acquire.sdk.nonbankcard.contactless;

import androidx.annotation.IntRange;

import com.newland.nsdk.rfic.RFICCardException;
import com.newland.nsdk.rfic.card.MifareClassicCard;
import com.newland.nsdk.rfic.enums.ContactlessKeyMode;

import acquire.base.utils.LoggerUtils;
import acquire.sdk.nonbankcard.BasicContactlessReader;

/**
 * A Mifare Classic contactless card reader.
 * <p>e.g.</p>
 * <pre>
 *         MifareClassicReader mifareClassicReader = new MifareClassicReader();
 *         int result = mifareClassicReader.connect(0);
 *         if (result != BasicContactlessReader.CONNECT_SUCCESS){
 *             return false;
 *         }
 *         mifareClassicReader.authenticateWithKeyA(blockIndex,key);
 *         byte[] data = mifareClassicReader.readBlock(blockIndex);
 *         mifareClassicReader.writeBlock(blockIndex,writtenData);
 *         //finish
 *         mifareClassicReader.close();
 * </pre>
 *
 * @author Janson
 * @date 2023/1/18 11:34
 * @since 3.7
 */
public class MifareClassicReader extends BasicContactlessReader<MifareClassicCard> {

    @Override
    protected Class<MifareClassicCard> getRfCardClass() {
        return MifareClassicCard.class;
    }

    /**
     * Authenticate a block with key A.
     *
     * @param blockIndex index of block to authenticate, starting from 0
     * @param key        6-byte authentication key
     * @return true on success, false on authentication failure
     */
    public boolean authenticateWithKeyA(@IntRange(from = 0, to = 255) int blockIndex, byte[] key) {
        if (rfCard != null) {
            try {
                byte[] uid = getUid();
                rfCard.authenticate(ContactlessKeyMode.KEYA_0X60, uid, (byte) blockIndex, key);
                return true;
            } catch (RFICCardException e) {
                LoggerUtils.e("MifareClassicReader authenticateWithKeyA failed!",e);
            }
        }
        return false;
    }

    /**
     * Authenticate a block with key B.
     *
     * @param blockIndex index of block to authenticate, starting from 0
     * @param key        6-byte authentication key
     * @return true on success, false on authentication failure
     */
    public boolean authenticateWithKeyB(@IntRange(from = 0, to = 255) int blockIndex, byte[] key) {
        if (rfCard != null) {
            try {
                byte[] uid = getUid();
                rfCard.authenticate(ContactlessKeyMode.KEYB_0X61, uid, (byte) blockIndex, key);
                return true;
            } catch (RFICCardException e) {
                LoggerUtils.e("MifareClassicReader authenticateWithKeyB failed!",e);
            }
        }
        return false;
    }


    /**
     * Read 16-byte block.
     * <p>This is an I/O operation and will block until complete.
     * It must not be called from the main application thread.
     * A blocked call will be canceled and it will return null, if {@link #close()} is called from another thread.</p>
     *
     * @param blockIndex index of block to read, starting from 0.
     * @return 16 byte block or null.
     */
    public byte[] readBlock(@IntRange(from = 0, to = 255) int blockIndex) {
        if (rfCard != null) {
            try {
                return rfCard.readBlockData((byte) blockIndex);
            } catch (RFICCardException e) {
                LoggerUtils.e("MifareClassicReader readBlock failed!",e);
            }
        }
        return null;
    }

    /**
     * Write 16-byte block.
     * <p>
     * This is an I/O operation and will block until complete.
     * It must not be called from the main application thread.
     * A blocked call will be canceled and it will return false, if {@link #close()} is called from another thread.
     * </p>
     *
     * @param blockIndex index of block to write, starting from 0.
     * @param data       16 bytes of data to write
     * @return true on success, false on I/O failure
     */
    public boolean writeBlock(@IntRange(from = 0, to = 255) int blockIndex, byte[] data) {
        if (rfCard != null) {
            try {
                rfCard.writeBlockData((byte) blockIndex, data);
                return true;
            } catch (RFICCardException e) {
                LoggerUtils.e("MifareClassicReader writeBlock failed!",e);
            }
        }
        return false;
    }

    /**
     * Increment a value block, storing the result in the temporary block on the tag.
     * <p>This is an I/O operation and will block until complete.
     * It must not be called from the main application thread.
     * A blocked call will be canceled and it will return false, if {@link #close()} is called from another thread.
     * </p>
     *
     * @param blockIndex index of block to increment, starting from 0
     * @param value      non-negative to increment by
     * @return true on success, false on I/O failure
     */
    public boolean increment(@IntRange(from = 0, to = 255) int blockIndex, byte[] value) {
        if (rfCard != null) {
            try {
                rfCard.increment((byte) blockIndex, value);
                return true;
            } catch (RFICCardException e) {
                LoggerUtils.e("MifareClassicReader increment failed!",e);
            }
        }
        return false;
    }

    /**
     * Decrement a value block, storing the result in the temporary block on the tag.
     * <p>This is an I/O operation and will block until complete.
     * It must not be called from the main application thread.
     * A blocked call will be canceled and it will return false, if {@link #close()} is called from another thread.
     * </p>
     *
     * @param blockIndex index of block to decrement, starting from 0
     * @param value      non-negative to decrement by
     * @return true on success, false on I/O failure
     */
    public boolean decrement(@IntRange(from = 0, to = 255) int blockIndex, byte[] value) {
        if (rfCard != null) {
            try {
                rfCard.decrement((byte) blockIndex, value);
                return true;
            } catch (RFICCardException e) {
                LoggerUtils.e("MifareClassicReader decrement failed!",e);
            }
        }
        return false;
    }

    /**
     * Copy from the temporary block to a value block.
     * <p>This is an I/O operation and will block until complete.
     * It must not be called from the main application thread.
     * A blocked call will be canceled and it will return false, if {@link #close()} is called from another thread.
     * </p>
     *
     * @param blockIndex index of block to copy to, starting from 0
     * @return true on success, false on I/O failure
     */
    public boolean transfer(@IntRange(from = 0, to = 255) int blockIndex) {
        if (rfCard != null) {
            try {
                rfCard.transfer((byte) blockIndex);
                return true;
            } catch (RFICCardException e) {
                LoggerUtils.e("MifareClassicReader transfer failed!",e);
            }
        }
        return false;
    }

    /**
     * Copy from a value block to the temporary block.
     * <p>This is an I/O operation and will block until complete.
     * It must not be called from the main application thread.
     * A blocked call will be canceled and it will return false, if {@link #close()} is called from another thread.
     * </p>
     *
     * @param blockIndex index of block to copy from, starting from 0
     * @return true on success, false on I/O failure
     */
    public boolean restore(@IntRange(from = 0, to = 255) int blockIndex) {
        if (rfCard != null) {
            try {
                rfCard.restore((byte) blockIndex);
                return true;
            } catch (RFICCardException e) {
                LoggerUtils.e("MifareClassicReader restore failed!",e);
            }
        }
        return false;
    }

}
