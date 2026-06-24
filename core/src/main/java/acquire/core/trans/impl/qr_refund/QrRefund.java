package acquire.core.trans.impl.qr_refund;

import acquire.core.TransResultListener;
import acquire.core.constant.TransStatus;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.InputAmountStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.ScanStep;

/**
 * Scan qr code to refund
 *
 * @author Janson
 * @date 2021/9/13 15:32
 */
public class QrRefund extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true, true,  false))
                .next(new InputAmountStep())
                .next(new ScanStep(true))
                .next(new PackQrRefundStep())
                .next(new AddRecordStep(TransStatus.REFUNDED))
//                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }
}
