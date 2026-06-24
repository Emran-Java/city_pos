package acquire.core.trans.impl.void_auth_complete;

import acquire.core.TransResultListener;
import acquire.core.constant.TransStatus;
import acquire.core.constant.TransType;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.FindOrigTraceStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.ReadCardStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Void Auth Compelte
 *
 * @author Janson
 * @date 2019/7/31 13:44
 */
public class VoidAuthComplete extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true, true,  true))
                .next(new FindOrigTraceStep(new String[]{TransType.TRANS_AUTH_COMPLETE},new int[]{TransStatus.SUCCESS}))
                .next(new ReadCardStep(null,null, EntryMode.MAG|EntryMode.INSERT|EntryMode.TAP))
                .next(new InputPinStep())
                .next(new PackVoidAuthCompleteStep())
                .next(new AddRecordStep(TransStatus.CANCELLED))
//                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }
}
