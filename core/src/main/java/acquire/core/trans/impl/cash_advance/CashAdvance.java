package acquire.core.trans.impl.cash_advance;

import acquire.core.TransResultListener;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.InputAmountStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.ReadCardStep;
import acquire.core.trans.steps.SignatureStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Cash Advance.
 * <p>
 *   A manual cash disbursement.
 *   Note some publications identify all Cash transactions involving a Credit card as ‘Cash Advance’ transactions
 * </p>
 *
 * @author Janson
 * @date 2024/4/23 10:25
 */
public class CashAdvance extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true, true, false))
                .next(new InputAmountStep())
                .next(new ReadCardStep(new InputPinStep(), new PackCashAdvanceStep(),
                        EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP | EntryMode.MANUAL))
                .next(new AddRecordStep())
                .next(new SignatureStep())
//                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }
}
