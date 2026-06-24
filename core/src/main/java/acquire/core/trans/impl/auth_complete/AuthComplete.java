package acquire.core.trans.impl.auth_complete;

import acquire.core.TransResultListener;
import acquire.core.constant.TransStatus;
import acquire.core.constant.TransType;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.FindOrigTraceStep;
import acquire.core.trans.steps.InputAmountStep;
import acquire.core.trans.steps.InputOrigAuthCodeStep;
import acquire.core.trans.steps.InputOrigDateStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.PrintReceiptStep;
import acquire.core.trans.steps.ReadCardStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Auth Complete
 *
 * @author Janson
 * @date 2019/5/21 14:34
 */
public class AuthComplete extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true,true,false))
                .next(new FindOrigTraceStep(
                        new String[]{TransType.TRANS_PRE_AUTH},new int[]{TransStatus.SUCCESS}
                ))
                .next(new InputAmountStep())
//                .next(new InputOrigAuthCodeStep())
//                .next(new InputOrigDateStep())
                .next(new ReadCardStep(new InputPinStep(), new PackAuthCompleteStep(),
                        EntryMode.MAG|EntryMode.INSERT|EntryMode.TAP))
                .next(new AddRecordStep(TransStatus.COMPLETED))
                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }

    /*@Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true,true,false))
                .next(new InputAmountStep())
                .next(new InputOrigAuthCodeStep())
                .next(new InputOrigDateStep())
                .next(new ReadCardStep(new InputPinStep(), new PackAuthCompleteStep(),
                        EntryMode.MAG|EntryMode.INSERT|EntryMode.TAP))
                .next(new AddRecordStep(TransStatus.COMPLETED))
                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }*/
}
