package acquire.sdk.nonbankcard.contactless;


import com.newland.nsdk.rfic.RFICCardException;
import com.newland.nsdk.rfic.card.NTAGCard;

import acquire.base.utils.LoggerUtils;
import acquire.sdk.nonbankcard.BasicContactlessReader;


/**
 * A NTAG contactless card reader.
 * <p>e.g.</p>
 * <pre>
 *     NtagReader ntagReader = new NtagReader();
 *     int result = ntagReader.connect(0);
 *     if (result != BasicContactlessReader.CONNECT_SUCCESS){
 *          return false;
 *     }
 *     ntagReader.readPages(pageId);
 *     ntagReader.close();
 *     byte[] uid = ntagReader.getUid();
 *     byte[] data = ntagReader.read4Pages(pageId);
 *     ntagReader.close();
 * </pre>
 *
 * @author Janson
 * @date 2022/11/22 15:34
 * @since 3.7
 */
public class NtagReader extends BasicContactlessReader<NTAGCard> {

    @Override
    protected Class<NTAGCard> getRfCardClass() {
        return NTAGCard.class;
    }

    /**
     * Read 4 pages (16 bytes).
     * <p>The NTAG protocol usually reads 4 pages at a time, to reduce the number of commands required to read an entire tag.</p>
     * <p>This is an I/O operation and will block until complete. It must not be called from the main application thread.
     * It will return null if {@link #close()} is called from another thread.</p>
     *
     * @param pageIndex index of page to read, starting from 0.
     * @return 4 pages (16 bytes).
     * If the number of pages remaining is less than 4, only the remaining pages will be returned
     */
    public byte[] readPages(int pageIndex) {
        if (rfCard != null) {
            try {
                return rfCard.read(pageIndex);
            } catch (RFICCardException e) {
                LoggerUtils.e("Ntag readPages["+pageIndex+"] failed!",e);
            }
        }
        return null;
    }

    /**
     * Read pages in a range
     * <p>This is an I/O operation and will block until complete. It must not be called from the main application thread.
     * It will return null if {@link #close()} is called from another thread.</p>
     *
     * @param startPageIndex start index of page to read, starting from 0.
     * @param endPageIndex   end index of page to read, starting from 0.
     * @return the data from startPage to endPage,4 bytes per page
     */
    public byte[] readRange(int startPageIndex, int endPageIndex) {
        if (rfCard != null) {
            try {
                return rfCard.fastRead(startPageIndex, endPageIndex);
            } catch (RFICCardException e) {
                LoggerUtils.e("Ntag readRange["+startPageIndex+","+endPageIndex+"] failed!",e);
            }
        }
        return null;
    }

    /**
     * Write 1 page (4 bytes).
     * <p>The NTAG protocol always writes 1 page at a time, to minimize EEPROM write cycles.</p>
     * <p>This is an I/O operation and will block until complete. It must not be called from the main application thread.
     * It will return false if {@link #close()} is called from another thread.</p>
     *
     * @param pageIndex index of page to write, starting from 0
     * @param data      4 bytes to write
     * @return true on success， false on writing failure.
     */
    public boolean writePage(int pageIndex, byte[] data) {
        if (rfCard != null) {
            try {
                rfCard.write(pageIndex, data);
                return true;
            } catch (RFICCardException e) {
                LoggerUtils.e("Ntag writePage["+pageIndex+"] failed!",e);
            }
        }
        return false;
    }

    /**
     * read Cnt data
     */
    public byte[] readCnt() {
        if (rfCard != null) {
            try {
                return rfCard.readCNT();
            } catch (RFICCardException e) {
                LoggerUtils.e("Ntag readCnt failed!",e);
            }
        }
        return null;
    }

    /**
     * Authorize Ntag with a
     *
     * @param pwd password bytes
     * @return authorization result bytes. If null, grant failed.
     */
    public byte[] pwdAuth(byte[] pwd) {
        if (rfCard != null) {
            try {
                return rfCard.pwdAuth(pwd);
            } catch (RFICCardException e) {
                LoggerUtils.e("Ntag pwdAuth failed!",e);
            }
        }
        return null;
    }


    /**
     * get the card version
     */
    public byte[] getVersion() {
        if (rfCard != null) {
            try {
                return rfCard.getVersion();
            } catch (RFICCardException e) {
                LoggerUtils.e("Ntag getVersion failed!",e);
            }
        }
        return null;
    }

    /**
     * read NTAG signature data.
     */
    public byte[] readSign() {
        if (rfCard != null) {
            try {
                return rfCard.readSign();
            } catch (RFICCardException e) {
                LoggerUtils.e("Ntag readSign failed!",e);
            }
        }
        return null;
    }
}
