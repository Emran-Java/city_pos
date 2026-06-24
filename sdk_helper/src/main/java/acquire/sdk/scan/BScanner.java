package acquire.sdk.scan;


import androidx.annotation.NonNull;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.internal.barcodedecoder.BarcodeDecoder;
import com.newland.nsdk.core.api.internal.barcodedecoder.DecodingByteCallback;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import acquire.base.utils.LoggerUtils;

/**
 * Scanner Module
 *
 * @author Janson
 * @date 2018/3/6
 */
public class BScanner {
    private final BarcodeDecoder barcodeDecoder;
    private ScannerListener listener;
    public BScanner() {
        barcodeDecoder = (BarcodeDecoder) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.BARCODE_DECODER);
    }

    /**
     * receive the result of {@link #startDecode(byte[], int, int)}
     *
     * @param listener the result listener
     */
    public void observeScanner(@NonNull ScannerListener listener) {
        try {
            this.listener = listener;
            //set scan result callback
            barcodeDecoder.setDecodingCallback((DecodingByteCallback) (eventCode, result) ->{
                listener.onDecoded(new String(result));
            } );
        } catch (NSDKException e) {
            LoggerUtils.e("BScanner observeScanner failed!",e);
            listener.onError(e.getMessage());
        }
    }

    /**
     * decode a QR/Barcode bitmap
     *
     * @param yuvData a bitmap data in YUV format
     * @param width   bitmap width
     * @param height  bitmap height
     */
    public void startDecode(byte[] yuvData, int width, int height) {
        try {
            barcodeDecoder.startDecode(yuvData, width, height);
        } catch (NSDKException e) {
            LoggerUtils.e("BScanner startDecode failed!",e);
            if (listener != null){
                listener.onError(e.getMessage());
            }
        }
    }

    /**
     * stop decoding
     */
    public void stopDecode() {
        try {
            barcodeDecoder.stopDecode();
        } catch (NSDKException e) {
            LoggerUtils.e("BScanner stopDecode failed!",e);
        }
    }

    /**
     * A simple callback that can receive QR/Barcode.
     *
     * @author Janson
     * @date 2022/8/4 16:51
     */
    public interface ScannerListener {
        /**
         * Called when scanner decoded the QR/Barcode successfully.
         *
         * @param qrCode QR/Barcode value
         */
        void onDecoded(String qrCode);

        /**
         * scanner  error
         *
         * @param msg error message
         */
        void onError(String msg);
    }
}
