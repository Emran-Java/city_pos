package acquire.core.trans.steps;

import acquire.core.R;
import acquire.core.constant.ResultCode;
import acquire.core.trans.BaseStep;
import acquire.database.model.Record;
import acquire.sdk.emv.constant.EntryMode;

/**
 * The {@link BaseStep} that check original card num
 *
 * @author Janson
 * @date 2019/7/25 14:12
 */
public class CheckVoidPanStep extends BaseStep {

    @Override
    public void intercept(Callback callback)  {
        Record record = getOrigRecord();
        if (pubBean.getCardNo() == null) {
            pubBean.setEntryMode(EntryMode.MANUAL);
            if (record != null){
                pubBean.setCardNo(record.getCardNo());
            }
        } else {
            if (record != null && !pubBean.getCardNo().equals(record.getCardNo())) {
                pubBean.setMessage(R.string.core_voidsale_orig_record_pan_no_match);
                pubBean.setResultCode(ResultCode.FL);
                callback.onResult(false);
                return;
            }
        }
        callback.onResult(true);
    }
}
