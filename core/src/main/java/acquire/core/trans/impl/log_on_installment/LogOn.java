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
 * @date 2026/2/17 19:18
 */
public class LogOn extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {

        Chain<StepBean> obj = chain.next(new PreCheckStep(true, true,  false));
        obj//.next(new ReadCardStep(new InputPinStep(), new PackSaleStep(), EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP | EntryMode.MANUAL))
                //.next(new SignatureStep())
                .next(new LogOnStep())
                .next(new AddRecordStep())
                //.next(new PrintReceiptStep())
                //.next(new FlyReceiptStep())
                .proceed(isSucc ->
                        showResult(isSucc,listener)
                );

      /*  chain.next(new PreCheckStep(true, true,  false))
                .next(new InputAmountStep())
                .next(new TipAmountStep())
                .next(new ReadCardStep(new InputPinStep(), new PackSaleStep(),
                        EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP | EntryMode.MANUAL))
                .next(new AddRecordStep())
                .next(new SignatureStep())
//                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc,listener));
    }*/
  }
}
