package acquire.core.trans.impl.help_center;

import acquire.base.activity.callback.SimpleCallback;
import acquire.core.R;
import acquire.core.TransResultListener;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.help_center.HelpCenterFragment;
import acquire.core.fragment.version.AboutFragment;
import acquire.core.trans.AbstractTrans;

/**
 * App about
 *
 * @author Emran
 * @date 2026/6/9 17:29
 */
public class HelpCenter extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        mActivity.mSupportDelegate.switchContent(HelpCenterFragment.newInstance(new SimpleCallback() {
            @Override
            public void result() {
                pubBean.setResultCode(ResultCode.OK);
                pubBean.setMessage(R.string.core_transaction_result_success);
                listener.onTransResult(true);
            }
        }));
    }
}
