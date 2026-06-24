package acquire.core.trans.impl.log_on_installment;

import acquire.base.chain.Chain;
import acquire.core.TransResultListener;
import acquire.core.bean.StepBean;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.PreCheckStep;

/**
 * LogOn
 *
 * @author Emran
 * @date 2026/5/01 9:18
 */
public class InstallmentLogOn extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {

        Chain<StepBean> obj = chain.next(new PreCheckStep(true, true,  false));
        obj//.next(new ReadCardStep(new InputPinStep(), new PackSaleStep(), EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP | EntryMode.MANUAL))
                //.next(new SignatureStep())
                .next(new InstallmentLogOnStep())
                .next(new AddRecordStep())
                //.next(new PrintReceiptStep())
                //.next(new FlyReceiptStep())
                .proceed(isSucc ->
                        showResult(isSucc,listener)
                );
  }
}
