package acquire.core.tools;


import java.util.Locale;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.database.model.Merchant;
import acquire.database.repository.MerchantRepository;

/**
 * Statistics utils
 *
 * @author Janson
 * @date 2018/12/13 9:50
 */
public class StatisticsUtils {

    /**
     * Trace number +1
     */
    public static synchronized void increaseTraceNo() {
        String traceNo = ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_TRACE_NO,"1");
        int currenNo = 1;
        try {
            currenNo = Integer.parseInt(traceNo);
        } catch (Exception e) {
            LoggerUtils.e("increaseTraceNo parseInt "+traceNo+" failed!",e);
        }
        int nextNo = (currenNo + 1) % 1000000;
        if (nextNo == 0){
            nextNo = 1;
        }
        ParamsUtils.setString(ParamsConst.PARAMS_KEY_BASE_TRACE_NO, String.format(Locale.US, "%06d", nextNo));
    }

    /**
     * Batch number +1
     */
    public static synchronized void increaseBatchNo(String mid,String tid) {
        MerchantRepository merchantRepository = new MerchantRepository();
        Merchant merchant = merchantRepository.findByMidTid(mid,tid);
        if (merchant == null){
            LoggerUtils.e("No such merchant[mid = "+mid+",tid = "+tid+"].");
            return;
        }
        String batchNo = merchant.getBatchNo();
        int currenNo = 1;
        try {
            currenNo = Integer.parseInt(batchNo);
        } catch (Exception e) {
            LoggerUtils.e("increaseBatchNo parseInt "+batchNo+" failed!",e);
        }
        int nextNo = (currenNo + 1) % 1000000;
        if (nextNo == 0){
            nextNo = 1;
        }
        merchant.setBatchNo(String.format(Locale.US, "%06d", nextNo));
        merchantRepository.update(merchant);
    }


}
