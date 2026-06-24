package acquire.core.trans.impl.reversal;

import acquire.core.TransResultListener;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.PreCheckStep;

/**
 * Reversal
 *
 * @author Janson
 * @date 2021/9/10 9:33
 */
public class Reversal extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(false,false,false))
                .next(new PackReversalStep())
                .proceed(listener::onTransResult);
    }
}
