package acquire.core.trans.impl.installment;

import acquire.base.chain.Chain;
import acquire.base.utils.ParamsUtils;
import acquire.core.TransResultListener;
import acquire.core.bean.StepBean;
import acquire.core.constant.ParamsConst;
import acquire.core.trans.AbstractTrans;
import acquire.core.trans.steps.AddRecordStep;
import acquire.core.trans.steps.InputAmountStep;
import acquire.core.trans.steps.InputBracInstallmentStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.core.trans.steps.PreCheckStep;
import acquire.core.trans.steps.PrintReceiptStep;
import acquire.core.trans.steps.ReadCardStep;
import acquire.core.trans.steps.SignatureStep;
import acquire.core.trans.steps.TipAmountStep;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Installment pay
 *
 * @author Janson
 * @date 2021/8/3 17:42
 */
public class Installment extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {

        String countryCode = ParamsUtils.getString(ParamsConst.PARAMS_KEY_COUNTRY_CODE,"050");
        pubBean.setCurrencyCode(countryCode);

        chain.next(new PreCheckStep(true, true,  false))
                .next(new InputAmountStep())
                .next(new ReadCardStep(
                        new InputPinStep(),
                        null,
                        EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP))
                .next(new InputBracInstallmentStep())
                .next(new PackInstallmentStep())
                .next(new AddRecordStep())
                .next(new SignatureStep())
                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }


    //try structural flow change
/*    @Override
    public void transact(TransResultListener listener) {

        String countryCode = ParamsUtils.getString(ParamsConst.PARAMS_KEY_COUNTRY_CODE,"050");
        pubBean.setCurrencyCode(countryCode);

        chain.next(new PreCheckStep(true, true,  false))
                .next(new InputAmountStep())
                .next(new ReadCardStep(new InputPinStep(),
                        new InputBracInstallmentStep(),
                        EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP))
                .next(new PackInstallmentStep())
                .next(new AddRecordStep())
                .next(new SignatureStep())
                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }*/
}
