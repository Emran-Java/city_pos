package acquire.core.trans.impl.balance;

import acquire.core.TransResultListener;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.InputPinStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.ReadCardStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Query balance
 *
 * @author Janson
 * @date 2019/7/24 14:48
 */
public class Balance extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true,false,false))
                .next(new ReadCardStep(null, null, EntryMode.MAG|EntryMode.INSERT|EntryMode.TAP))
                .next(new InputPinStep())
                .next(new PackBalanceStep())
                .proceed(isSucc -> showResult(isSucc,listener));
    }
}
