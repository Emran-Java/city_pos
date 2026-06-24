package acquire.core.trans.impl.settle;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.bean.SettleReceiptBean;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransType;
import acquire.core.fragment.print.PrintFragment;
import acquire.core.model.SchemeGroup;
import acquire.core.report_data_factiry.CardDataFactory;
import acquire.core.trans.BaseStep;
import acquire.database.bean.TransactionSummary;
import acquire.database.model.Merchant;
import acquire.database.repository.MerchantRepository;
import acquire.database.repository.RecordRepository;
import acquire.sdk.device.BDevice;

/**
 * The {@link BaseStep} that prints settlement data
 *
 * @author Janson
 * @date 2019/7/29 15:08
 */
class PrintSettleStep extends BaseStep {
    private final MerchantRepository merchantRepository = new MerchantRepository();

    @Override
    public void intercept(Callback callback)  {
        if (!BDevice.supportPrint()&& !ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
            //This device doesn't support print
            for (Merchant merchant : pubBean.getSettleMerchants()) {
                //set the halt of this merchant to HALT_PRINT_SETTLE+1
                merchant.setSettleStep(Settle.STEP_PRINT +1);
                merchantRepository.update(merchant);
            }
            callback.onResult(true);
        } else {
            printMerchant(0,pubBean.getSettleMerchants(),callback);
        }
    }

    private void printMerchant(int index, List<Merchant> merchants, Callback callback){
        if (index >= merchants.size()){
            callback.onResult(true);
            return;
        }

        Merchant merchant = merchants.get(index);
        if (merchant.getSettleStep() > Settle.STEP_PRINT){
            printMerchant(index+1,merchants,callback);
            return;
        }

        SettleReceiptBean settleReceiptBean = new SettleReceiptBean();
        settleReceiptBean.setMerchantType(merchant.getType());
        settleReceiptBean.setMerchantName(merchant.getMerchantName());
        settleReceiptBean.setMid(merchant.getMid());
        settleReceiptBean.setTid(merchant.getTid());
        settleReceiptBean.setBatch(merchant.getBatchNo());
        settleReceiptBean.setSettleEqual(merchant.isSettleEqual());
        settleReceiptBean.setSettleDate(merchant.getSettleDate());
        settleReceiptBean.setSettleTime(merchant.getSettleTime());
//        List<TransactionSummary> transactionSummaries= new RecordRepository().getTransactionSummary(merchant.getMid(),merchant.getTid());
        List<TransactionSummary> transactionSummaries= new RecordRepository().getBracTransactionSummary(merchant.getMid(),merchant.getTid());
        settleReceiptBean.setTransactionSummaries(transactionSummaries);
        List<SchemeGroup> schemeGroupList = new ArrayList<>();

        if(merchant.getType().equalsIgnoreCase("EMI")){
            schemeGroupList = CardDataFactory.getEmiSchemeGroups();
            settleReceiptBean.setPrintStartTitle("SETTLEMENT (PAY-FLEX)");
            settleReceiptBean.setPrintEndTitle("END OF SETTLEMENT (PAY-FLEX)");
        }else{
            schemeGroupList = CardDataFactory.getSettleSchemeGroups();
            settleReceiptBean.setPrintStartTitle("SETTLEMENT (SALE)");
            settleReceiptBean.setPrintEndTitle("END OF SETTLEMENT (SALE)");
        }

        settleReceiptBean.setSchemeGroupList(schemeGroupList);

        mActivity.mSupportDelegate.switchContent(PrintFragment.newSettlementInstance(settleReceiptBean, new FragmentCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                merchant.setSettleStep(Settle.STEP_PRINT +1);
                merchantRepository.update(merchant);
                printMerchant(index+1,merchants,callback);
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
                LoggerUtils.e("Print merchant[mid="+merchant.getMid()+",tid="+merchant.getTid()+"] failed.");
                callback.onResult(false);
            }
        }));
    }
}
