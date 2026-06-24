package acquire.core.trans.impl.qr_code;

import acquire.core.TransResultListener;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.InputAmountStep;
import acquire.core.trans.steps.PreCheckStep;

/**
 * Qr code transaction
 *
 * @author Janson
 * @date 2021/9/13 15:37
 */
public class QrCode extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true, true,  false))
                .next(new InputAmountStep())
                .next(new PackQrCodeStep())
                .next(new AddRecordStep())
//                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc,listener));
    }
}
