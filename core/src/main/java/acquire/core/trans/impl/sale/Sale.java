package acquire.core.trans.impl.sale;

import acquire.base.chain.Chain;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.core.TransResultListener;
import acquire.core.bean.StepBean;
import acquire.core.constant.ParamsConst;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.InputAmountStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.PrintReceiptStep;
import acquire.core.trans.steps.ReadCardStep;
import acquire.core.trans.steps.SignatureStep;
import acquire.core.trans.steps.TipAmountStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Sale
 *
 * @author Janson
 * @date 2019/4/24 10:04
 */
public class Sale extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
        //pubBean.setAmount(234);
        /*
        Note:
        Md. Emran Hossain
        Date: 20260120
        We modify this class because, we pass amount from home screen. so we skip InputAmountStep(), TipAmountStep()
        But, when we come here from menu item we must want  InputAmountStep(), TipAmountStep().
        so, inthe present situation we set logic based on 'Amount'.
        * */

        String countryCode = ParamsUtils.getString(ParamsConst.PARAMS_KEY_COUNTRY_CODE, "050");
        String tipEnable = ParamsUtils.getString(ParamsConst.PARAMS_KEY_SALE_TIP_FLAG, "0");
        //tipEnable = "0";
        pubBean.setCurrencyCode(countryCode);

        long longAmount = pubBean.getAmount();
        Chain<StepBean> obj = chain.next(new PreCheckStep(true, true, false));

        if (longAmount <= 0) {
            obj.next(new InputAmountStep());
        }

        //for TIP
        if (tipEnable.equals("1")) {
            ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_OTHER_TIP_INPUT, true);
            obj.next(new TipAmountStep());
        } else {
            ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_OTHER_TIP_INPUT, false);
        }


        obj.next(new ReadCardStep(new InputPinStep(), new PackSaleStep(),
                        EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP | EntryMode.MANUAL))
                .next(new AddRecordStep())
                .next(new SignatureStep())
                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));

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
