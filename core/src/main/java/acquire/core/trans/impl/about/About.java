package acquire.core.trans.impl.about;

import acquire.base.activity.callback.SimpleCallback;
import acquire.core.R;
import acquire.core.TransResultListener;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.version.AboutFragment;
import acquire.core.trans.AbstractTrans;

/**
 * App about
 *
 * @author Janson
 * @date 2021/9/13 10:39
 */
public class About extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        mActivity.mSupportDelegate.switchContent(AboutFragment.newInstance(new SimpleCallback() {
            @Override
            public void result() {
                pubBean.setResultCode(ResultCode.OK);
                pubBean.setMessage(R.string.core_transaction_result_success);
                listener.onTransResult(true);
            }
        }));
    }
}
