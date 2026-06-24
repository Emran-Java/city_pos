package acquire.core.trans.impl.refund;

import acquire.core.TransResultListener;
import acquire.core.constant.TransStatus;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.InputAmountStep;
import acquire.core.trans.steps.InputOrigDateStep;
import acquire.core.trans.steps.InputOrigRefNumStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.ReadCardStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Card Refund
 *
 * @author Janson
 * @date 2019/7/25 9:31
 */
public class Refund extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true, true,  true))
                .next(new InputOrigRefNumStep())
                .next(new InputOrigDateStep())
                .next(new InputAmountStep(true))
                .next(new ReadCardStep(new InputPinStep(),new PackRefundStep(), EntryMode.MAG|EntryMode.INSERT|EntryMode.TAP|EntryMode.MANUAL))
                .next(new AddRecordStep(TransStatus.REFUNDED))
//                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }
}
