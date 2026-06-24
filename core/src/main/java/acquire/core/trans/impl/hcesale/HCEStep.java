package acquire.core.trans.impl.hcesale;

import android.text.TextUtils;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.core.R;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.hcesale.HCEFragment;
import acquire.core.fragment.hcesale.HCERequester;
import acquire.core.trans.BaseStep;
import acquire.core.trans.pack.CallerException;
import acquire.core.trans.pack.json.JsonGetCaller;
import acquire.core.trans.pack.json.npi_sale.HCESaleConst;
import acquire.core.trans.pack.json.npi_sale.HCESaleResp;

/**
 * The step that packs {@link HCESale} data and sends them to the server.
 *
 * @author Janson
 * @date 2021/9/13 15:30
 */
public class HCEStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        if (!doReversal()) {
            //check reversal
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_reversal_fail);
            callback.onResult(false);
            return;
        }

        FragmentCallback<String> fragmentCallback = new FragmentCallback<String>() {
            private boolean hasRun;

            @Override
            public void onSuccess(String orderNo) {
                if (hasRun) {
                    return;
                }
                hasRun = true;
                pubBean.setResultCode(ResultCode.OK);
                pubBean.setMessage(R.string.core_transaction_result_success);
                callback.onResult(true);
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                if (hasRun) {
                    return;
                }
                hasRun = true;
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
                if (!TextUtils.isEmpty(errorMsg)) {
                    pubBean.setMessage(errorMsg);
                }
                callback.onResult(false);
            }
        };
        HCERequester hceRequester = new HCERequester() {
            @Override
            public HCESaleResp createOrder() {
                initPubBean();
                if (TextUtils.isEmpty(pubBean.getOutOrderNo())) {
                    pubBean.setOutOrderNo(HCESaleConst.generateThirdNo());
                }

                try {
                    HCESaleResp resp = new JsonGetCaller.Builder(mActivity)
                            .packComm(HCESaleConst.HCE_ORDER_CREATE +
                                    pubBean.getOutOrderNo() + "?amount=" + FormatUtils.formatAmount(pubBean.getAmount()), HCESaleResp.class, true) ;

                    if (resp.getCode() == 200) {
                        resp.setUrl(HCESaleConst.HCE_VIEW_BASE_URL + "code=200&amount=" + FormatUtils.formatAmount(pubBean.getAmount()) + "&orderNo=" + pubBean.getOutOrderNo());
                    }
                    return resp;
                } catch (CallerException e) {
                    return new HCESaleResp(-1, e.getMessage());
                }
            }

            @Override
            public HCESaleResp queryResult() {
                LoggerUtils.d("hce caller - queryOrder");
                try {
                    return new JsonGetCaller.Builder(mActivity)
                            .withoutPrompts()
                            .packComm(HCESaleConst.HCE_ORDER_QUERY + pubBean.getOutOrderNo(), HCESaleResp.class, false);
                } catch (CallerException e) {
                    return new HCESaleResp(e.getCallerResult(), e.getMessage());
                }
            }
        };

        String amountPrompt = null;
        if (pubBean.getAmount() != 0) {
            amountPrompt = CurrencyUtils.getCurrencySymbol(pubBean.getCurrencyCode()) + FormatUtils.formatAmount(pubBean.getAmount());
        }
        mActivity.mSupportDelegate.switchContent(HCEFragment.newInstance(amountPrompt, hceRequester, fragmentCallback));


    }

}
