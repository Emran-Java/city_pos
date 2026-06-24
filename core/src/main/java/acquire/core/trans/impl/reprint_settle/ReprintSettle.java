package acquire.core.trans.impl.reprint_settle;

import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.core.R;
import acquire.core.TransResultListener;
import acquire.core.bean.SettleReceiptBean;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.print.PrintFragment;
import acquire.core.trans.AbstractTrans;
import acquire.database.bean.TransactionSummary;
import acquire.database.model.HistorySummary;
import acquire.database.model.Merchant;
import acquire.database.repository.HistorySummaryRepository;
import acquire.database.repository.MerchantRepository;
import acquire.sdk.device.BDevice;

/**
 * reprint settle data
 *
 * @author Janson
 * @date 2021/6/30 10:48
 */
public class ReprintSettle extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        if (!BDevice.supportPrint() && !ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_print_unsupport);
            listener.onTransResult(false);
            return;
        }

        HistorySummaryRepository historySummaryRepository = new HistorySummaryRepository();
        if (historySummaryRepository.getCount() == 0) {
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_print_no_such_data);
            ToastUtils.showToast(R.string.core_print_no_such_data);
            listener.onTransResult(false);
            return;
        }
        List<Merchant> merchants = new MerchantRepository().findAll();
        printMerchant(0,merchants,listener);
    }

    private void printMerchant(int index, List<Merchant> merchants,TransResultListener listener){
        if (index >= merchants.size()){
            pubBean.setResultCode(ResultCode.OK);
            pubBean.setMessage(R.string.core_print_success);
            listener.onTransResult(true);
            return;
        }
        Merchant merchant = merchants.get(index);
        String mid = merchant.getMid();
        String tid = merchant.getTid();
        SettleReceiptBean settleReceiptBean = new SettleReceiptBean();
        settleReceiptBean.setMerchantType(merchant.getType());
        settleReceiptBean.setMid(mid);
        settleReceiptBean.setTid(tid);
        HistorySummaryRepository historySummaryRepository = new HistorySummaryRepository();
        List<TransactionSummary> transactionSummaries = historySummaryRepository.getTransactionSummaries(merchant);
        if (transactionSummaries.isEmpty()){
            printMerchant(index+1,merchants,listener);
            return;
        }
        settleReceiptBean.setSettleDate(historySummaryRepository.getSettleDate(merchant));
        settleReceiptBean.setSettleTime(historySummaryRepository.getSettleTime(merchant));
        settleReceiptBean.setSettleEqual(historySummaryRepository.isSettleEqual(merchant));
        transactionSummaries.removeIf(item -> item.getTransType() == null);
        settleReceiptBean.setTransactionSummaries(transactionSummaries);
        mActivity.mSupportDelegate.switchContent(PrintFragment.newSettlementInstance(settleReceiptBean, new FragmentCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                printMerchant(index+1,merchants,listener);
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
                listener.onTransResult(false);
            }
        }));
    }

}
