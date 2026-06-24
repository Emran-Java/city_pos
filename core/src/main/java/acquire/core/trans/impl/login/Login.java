package acquire.core.trans.impl.login;

import acquire.core.TransResultListener;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.PreCheckStep;


/**
 * Login
 *
 * @author Janson
 * @date 2022/10/8 14:37
 */
public class Login extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(false,false,false))
                .next(new PackLoginStep())
                .proceed(isSucc -> showResult(isSucc,listener));
    }
}
