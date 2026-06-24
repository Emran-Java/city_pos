package acquire.core.trans.impl.settle;

import acquire.core.TransResultListener;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.PreCheckStep;

/**
 * Settle the transaction total data in a batch
 *
 * @author Janson
 * @date 2019/7/29 14:54
 */
public class Settle extends AbstractTrans {
    /**
     * Currently ready to perform the settlement upload step
     */
    public final static int STEP_SETTLEMENT_SENT = 0x01;
    /**
     * Currently ready to perform the batch upload step
     */
    public final static int STEP_BATCH_UP = STEP_SETTLEMENT_SENT +1;
    /**
     * Currently ready to perform the print step
     */
    public final static int STEP_PRINT = STEP_BATCH_UP +1;

    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true,false,  true))
                .next(new HaltRecoverStep())
                .next(new SelectMerchantStep())
                .next(new PackSettleStep())
                .next(new PrintSettleStep())
                .next(new ClearSettleStep())
                .proceed(success -> showResult(success, listener));
    }
}
