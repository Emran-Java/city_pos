package acquire.sdk.scan;


import androidx.annotation.NonNull;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.internal.barcodedecoder.DecodingByteCallback;
import com.newland.nsdk.core.api.internal.barcodedecoder.DecodingCallback;
import com.newland.nsdk.core.api.internal.barcodescanner.BarcodeScanner;
import com.newland.nsdk.core.api.internal.barcodescanner.ScanParameters;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import acquire.base.utils.LoggerUtils;

/**
 * Hard scanner
 *
 * @author Janson
 * @date 2023/7/4 10:37
 */
public class BHardScanner {
    private final BarcodeScanner barcodeScanner;
    public BHardScanner() {
        barcodeScanner = (BarcodeScanner) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.BARCODE_SCANNER);
    }

    /**
     * scan a QR/Barcode with hard scanner.
     */
    public void startScan(@NonNull HardScannerListener listener) {
        try {
            ScanParameters scanParameters = new ScanParameters();
            scanParameters.setTimeout(25400);
            scanParameters.setSoundSwitcher(false);
            barcodeScanner.initScan(scanParameters);
            barcodeScanner.setDecodingCallback(new DecodingByteCallback() {
                private boolean done;
                @Override
                public void onDecodingByteCallback(int eventCode, byte[] result) {
                    if (done){
                        return;
                    }
                    done = true;
                    stopScan();
                    listener.onDecoded(new String(result));
                }
            });
            barcodeScanner.startScan();
        } catch (NSDKException e) {
            LoggerUtils.e("BHardScanner startScan failed!",e);
            listener.onError(e.getMessage());
        }
    }

    /**
     * Scan the code continuously until the {@link #stopScan} is executed
     */
    public void startContScan(@NonNull HardScannerListener listener) {
        try {
            ScanParameters scanParameters = new ScanParameters();
            scanParameters.setSoundSwitcher(false);
            barcodeScanner.initScan(scanParameters);
            barcodeScanner.setDecodingCallback((DecodingCallback) (eventCode, result) -> {
                listener.onDecoded(result);
            });
            barcodeScanner.startScan();
        } catch (NSDKException e) {
            LoggerUtils.e("BHardScanner startContScan failed!",e);
            listener.onError(e.getMessage());
        }
    }

    /**
     * stop scanner
     */
    public void stopScan() {
        try {
            barcodeScanner.stopScan();
            barcodeScanner.releaseScan();
        } catch (NSDKException e) {
            LoggerUtils.e("BHardScanner stopScan failed!",e);
        }
    }
    /**
     * A simple callback that can receive QR/Barcode.
     *
     * @author Janson
     * @date 2022/8/4 16:51
     */
    public interface HardScannerListener {
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
