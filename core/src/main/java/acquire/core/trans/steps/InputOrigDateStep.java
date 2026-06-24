package acquire.core.trans.steps;

import acquire.base.utils.StringUtils;
import acquire.base.widget.dialog.date.DateDialog;
import acquire.core.R;
import acquire.core.constant.ResultCode;
import acquire.core.trans.BaseStep;

/**
 * Input original date.
 *
 * @author Janson
 * @date 2021/6/29 11:29
 */
public class InputOrigDateStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        if (pubBean.getOrigDate() != null){
            callback.onResult(true);
            return;
        }
        mActivity.runOnUiThread(() ->
            new DateDialog.Builder(mActivity)
                    .setTitle(R.string.core_orig_date_title)
                    .setConfirmListener((year, month, day) -> {
                        String strYear = StringUtils.fill(year + "", "0", 4, true);
                        String strMon = StringUtils.fill(month + "", "0", 2, true);
                        String strDay = StringUtils.fill(day + "", "0", 2, true);
                        pubBean.setOrigDate(strYear+strMon+strDay);
                        callback.onResult(true);
                    })
                    .setCancelListener(dialog -> {
                        pubBean.setResultCode(ResultCode.UC);
                        pubBean.setMessage(R.string.core_transaction_result_user_cancel);
                        callback.onResult(false);
                    })
                    .show()
        );

    }
}
