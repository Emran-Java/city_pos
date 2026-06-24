package acquire.core.trans.steps;

import acquire.base.activity.callback.FragmentCallback;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.amount.CashBackAmountFragment;
import acquire.core.trans.BaseStep;

/**
 * Input amount with cash and goods amount.
 *
 * @author Janson
 * @date 2018/11/22 23:12
 */
public class InputCashbackAmountStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        if (pubBean.getAmount() > 0) {
            callback.onResult(true);
            return;
        }
        mActivity.mSupportDelegate.switchContent(CashBackAmountFragment.newInstance(pubBean.getCurrencyCode(), new FragmentCallback<Long[]>() {
            @Override
            public void onSuccess(Long[] amount) {
                pubBean.setAmount(amount[0]+amount[1]);
                pubBean.setBaseAmount(amount[0]);
                pubBean.setCashAmount(amount[1]);
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
