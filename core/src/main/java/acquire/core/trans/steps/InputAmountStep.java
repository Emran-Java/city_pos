package acquire.core.trans.steps;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.R;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.amount.AmountFragment;
import acquire.core.trans.BaseStep;

/**
 * Input amount.
 *
 * @author Janson
 * @date 2018/11/22 23:12
 */
public class InputAmountStep extends BaseStep {
    private boolean isRefund;

    public InputAmountStep() {
    }

    public InputAmountStep(boolean isRefund) {
        this.isRefund = isRefund;
    }

    @Override
    public void intercept(Callback callback) {
        if (pubBean.getAmount() > 0) {
            callback.onResult(true);
            return;
        }
        mActivity.mSupportDelegate.switchContent(AmountFragment.newInstance(pubBean.getCurrencyCode(), new FragmentCallback<Long>() {
            @Override
            public void onSuccess(Long amount) {
                if (isRefund) {
                    long maxRefundAmount = ParamsUtils.getLong(ParamsConst.PARAMS_KEY_BASE_MAX_REFUND_AMOUNT, 1000000);
                    if (amount > maxRefundAmount) {
                        ToastUtils.showToast(R.string.core_amount_max_refund_error);
                        return;
                    }
                    new MessageDialog.Builder(mActivity)
                            .setMessage(mActivity.getString(R.string.core_amount_confirm_format, FormatUtils.formatAmount(amount)))
                            .setConfirmButton(dialog -> {
                                pubBean.setAmount(amount);
                                callback.onResult(true);
                            })
                            .setCancelButton(dialog -> {
                            })
                            .show();
                } else {
                    pubBean.setAmount(amount);
                    callback.onResult(true);
                }

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
