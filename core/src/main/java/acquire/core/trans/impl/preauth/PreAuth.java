package acquire.core.trans.impl.preauth;

import acquire.core.TransResultListener;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.InputAmountStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.PrintReceiptStep;
import acquire.core.trans.steps.ReadCardStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Pre-Auth
 *
 * @author Janson
 * @date 2019/7/31 10:26
 */
public class PreAuth extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
                chain.next(new PreCheckStep(true,true,false))
                .next(new InputAmountStep())
                .next(new ReadCardStep(new InputPinStep(), new PackPreAuthStep(), EntryMode.MAG|EntryMode.INSERT|EntryMode.TAP))
                .next(new AddRecordStep())
                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }
}
