package acquire.core.trans.impl.cash_back;

import acquire.core.TransResultListener;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.InputCashbackAmountStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.ReadCardStep;
import acquire.core.trans.steps.SignatureStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Cash back.
 * <p>
 *   A Purchase transaction where the amount of the transaction represents both the value of the goods (or services)
 *   and of a Cash Amount requested by the Cardholder. The amount of the cash portion is identified in the transaction data as a separate item.
 *    Note ‘Cash Back’ is sometimes used in conjunction with a Credit Card program
 *    to indicate the Issuer will credit the card account based on card use (some sort of Loyalty program)。
 * </p>
 *
 * @author Janson
 * @date 2024/4/23 10:25
 */
public class CashBack extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true, true, false))
                .next(new InputCashbackAmountStep())
                .next(new ReadCardStep(new InputPinStep(), new PackCashBackStep(),
                        EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP | EntryMode.MANUAL))
                .next(new AddRecordStep())
                .next(new SignatureStep())
//                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }
}
