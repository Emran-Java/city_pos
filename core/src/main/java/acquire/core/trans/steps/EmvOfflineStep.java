package acquire.core.trans.steps;


import acquire.core.R;
import acquire.core.constant.ResultCode;
import acquire.core.tools.StatisticsUtils;
import acquire.core.trans.BaseStep;

/**
 * A step to handle emv offline
 *
 * @author Janson
 * @date 2022/6/14 16:31
 */
public class EmvOfflineStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        initPubBean();
        StatisticsUtils.increaseTraceNo();
        pubBean.setResultCode(ResultCode.OK);
        pubBean.setMessage(R.string.core_transaction_result_success);
        callback.onResult(true);
    }

}
