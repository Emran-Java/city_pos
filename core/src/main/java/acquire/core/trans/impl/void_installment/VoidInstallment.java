package acquire.core.trans.impl.void_installment;

import acquire.core.TransResultListener;
import acquire.core.constant.TransStatus;
import acquire.core.constant.TransType;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.FindOrigTraceStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.PrintReceiptStep;
import acquire.core.trans.steps.ReadCardStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Void installment
 *
 * @author Janson
 * @date 2021/8/3 17:43
 */
public class VoidInstallment extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true, true, true))
                .next(new FindOrigTraceStep(new String[]{TransType.TRANS_INSTALLMENT}, new int[]{TransStatus.SUCCESS}))
                //.next(new ReadCardStep(null,null, EntryMode.MAG|EntryMode.INSERT|EntryMode.TAP))
                //.next(new InputPinStep())
                .next(new PackVoidInstallmentStep())
                .next(new AddRecordStep(TransStatus.CANCELLED))
                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }
}
