package acquire.core.trans.impl.scanpay;

import acquire.core.TransResultListener;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.InputAmountStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.ScanStep;

/**
 * Scan qr pay
 *
 * @author Janson
 * @date 2021/9/13 15:27
 */
public class ScanPay extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true, true,  false))
                .next(new InputAmountStep())
                .next(new ScanStep(false))
                .next(new PackScanPayStep())
                .next(new AddRecordStep())
//                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }
}
