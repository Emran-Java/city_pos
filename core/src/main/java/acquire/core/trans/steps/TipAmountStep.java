package acquire.core.trans.steps;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.tip.TipFragment;
import acquire.core.trans.BaseStep;

/**
 * Input tip amount.
 *
 * @author Janson
 * @date 2021/9/1 10:23
 */
public class TipAmountStep extends BaseStep {
    @Override
    public void intercept(Callback callback) {
        /*if (pubBean.getTipAmount()!= 0 ){
            pubBean.setBaseAmount(pubBean.getAmount());
            pubBean.setAmount(pubBean.getAmount()+pubBean.getTipAmount());
            callback.onResult(true);
            return;
        }*/
        if (!ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_OTHER_TIP_INPUT)){
            callback.onResult(true);
            return;
        }
        if (pubBean.getAmount() == 0){
            LoggerUtils.e("skip tip,because amount is 0.");
            callback.onResult(true);
            return;
        }
        pubBean.setAmount(pubBean.getAmount()-pubBean.getTipAmount());
        mActivity.mSupportDelegate.switchContent(TipFragment.newInstance(pubBean.getCurrencyCode(), pubBean.getAmount(), new FragmentCallback<Long>() {
            @Override
            public void onSuccess(Long amount) {
                pubBean.setTipAmount(amount);
//                pubBean.setBaseAmount(pubBean.getAmount()-pubBean.getTipAmount());
                pubBean.setBaseAmount(pubBean.getAmount());
                pubBean.setAmount(pubBean.getAmount()+amount);
                callback.onResult(true);
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                switch (errorType) {
                    case FragmentCallback.CANCEL:
                        pubBean.setResultCode(ResultCode.UC);
                        break;
                    case FragmentCallback.TIMEOUT:
                    case FragmentCallback.FAIL:
                    default:
                        pubBean.setResultCode(ResultCode.FL);
                        break;
                }
                pubBean.setMessage(errorMsg);
                callback.onResult(false);
            }
        }));
    }
}
