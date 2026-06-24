package acquire.core.trans.impl.test_txn;

import acquire.base.chain.Chain;
import acquire.core.TransResultListener;
import acquire.core.bean.StepBean;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.PrintReceiptStep;

/**
 * Sale
 *
 * @author Janson
 * @date 2019/4/24 10:04
 */
public class TestTxn extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
        //pubBean.setAmount(0);


        Chain<StepBean> obj = chain.next(new PreCheckStep(true, true,  false));
        obj//.next(new ReadCardStep(new InputPinStep(), new PackSaleStep(), EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP | EntryMode.MANUAL))
                //.next(new SignatureStep())
                .next(new TestTxnStep())
                .next(new AddRecordStep())
                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
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
