package acquire.core.trans.impl.settle;

import java.util.List;

import acquire.core.R;
import acquire.core.constant.ResultCode;
import acquire.core.tools.SignatureDirManager;
import acquire.core.tools.StatisticsUtils;
import acquire.core.trans.BaseStep;
import acquire.database.bean.TransactionSummary;
import acquire.database.model.Merchant;
import acquire.database.repository.HistorySummaryRepository;
import acquire.database.repository.MerchantRepository;
import acquire.database.repository.RecordRepository;
import acquire.database.repository.ReversalDataRepository;

/**
 * The {@link BaseStep} that clear records.
 *
 * @author Janson
 * @date 2019/7/29 15:19
 */
class ClearSettleStep extends BaseStep {
    private final MerchantRepository merchantRepository = new MerchantRepository();

    @Override
    public void intercept(Callback callback)  {
        merchantRepository.clearHalt(pubBean.getSettleMerchants());
        for (Merchant merchant : pubBean.getSettleMerchants()) {
            // clear the records of this merchant.
            deleteRecords(merchant);
            // batch num +1 of this merchant.
            StatisticsUtils.increaseBatchNo(merchant.getMid(),merchant.getTid());
            //delete signature bmp files of this merchant.
            SignatureDirManager.clearSignatureDir(merchant.getMid(),merchant.getTid());
        }
        //delete reversal data
        ReversalDataRepository reversalRepository = new ReversalDataRepository();
        reversalRepository.deleteAllReversalData();
        pubBean.setResultCode(ResultCode.OK);
        pubBean.setMessage(R.string.core_settle_success);
        callback.onResult(true);
    }
    private void deleteRecords(Merchant merchant){
        RecordRepository recordRepository = new RecordRepository();
//        List<TransactionSummary> transactionSummaries = recordRepository.getTransactionSummary(merchant.getMid(),merchant.getTid());
        List<TransactionSummary> transactionSummaries = recordRepository.getBracTransactionSummary(merchant.getMid(),merchant.getTid());

        HistorySummaryRepository historySummaryRepository = new HistorySummaryRepository();
        historySummaryRepository.clear(merchant);
        historySummaryRepository.addTransactionSummaries(merchant,transactionSummaries);

//        recordRepository.deleteByMidTid(merchant.getMid(),merchant.getTid());
        recordRepository.deleteBtacSettleByMidTid(merchant.getMid(),merchant.getTid());
    }
}
