package acquire.core.trans.impl.settings;

import android.content.Intent;

import acquire.core.R;
import acquire.core.TransResultListener;
import acquire.core.constant.ResultCode;
import acquire.core.trans.AbstractTrans;

/**
 * Enter the set interface.
 *
 * @author Janson
 * @date 2021/6/23 14:24
 */
public class Settings extends AbstractTrans {
    @Override
    public void transact(TransResultListener listener) {
        //go to settings/src/main/java/acqiore/settings/SettingsActivity.java
        Intent intent = new Intent("android.intent.action.NEWLAND.Settings");
        intent.setPackage(mActivity.getPackageName());
        mActivity.mSupportDelegate.startActivityForResult(intent, null, result -> {
            pubBean.setResultCode(ResultCode.OK);
            pubBean.setMessage(R.string.core_transaction_result_success);
            listener.onTransResult(true);
        });
    }
}
