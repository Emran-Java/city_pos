package acquire.core.trans.impl.print_detail;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import acquire.base.activity.callback.SimpleCallback;
import acquire.core.R;
import acquire.core.TransResultListener;
import acquire.core.constant.IntentParamKeyContent;
import acquire.core.constant.ResultCode;
import acquire.core.model.FeatureSubMenuModel;
import acquire.core.trans.AbstractTrans;
import acquire.core.fragment.common.submenu.CoreFeatureSubMenuListFragment;

/**
 * print the detail
 *
 * @author Janson
 * @date 2021/6/30 10:48
 */
public class PrintDetail extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {
        //String packageName = mActivity.getPackageName();

        String titleText = mActivity.getIntent().getStringExtra(IntentParamKeyContent.TRANS_TITLE_TEXT);
        ArrayList<FeatureSubMenuModel> reportMenu =
                (ArrayList<FeatureSubMenuModel>) mActivity.getIntent().getSerializableExtra(IntentParamKeyContent.TRANS_REPORT_MENU);

        Fragment fragment = CoreFeatureSubMenuListFragment.newInstance(titleText, reportMenu, new SimpleCallback() {
            @Override
            public void result() {
                pubBean.setResultCode(ResultCode.OK);
                pubBean.setMessage(R.string.core_transaction_result_success);
                listener.onTransResult(true);
            }
        });

        /*Bundle bundle = new Bundle();
        bundle.putString(IntentParamKeyContent.TRANS_TITLE_TEXT, titleText);
        fragment.setArguments(bundle);*/
        mActivity.mSupportDelegate.switchBracContent(fragment,0);
    }

    /* @Override
    public void transact(TransResultListener listener) {
        //go to settings/src/main/java/acqiore/settings/SettingsActivity.java
//        Intent intent = new Intent("android.intent.action.NEWLAND.Settings");
        Intent intent =  new Intent(mActivity.getBaseContext(), ReportsActivity.class);

        try{
            intent.setPackage(mActivity.getPackageName());
            intent.putExtra("reportTitle","Prints Report");

        //    mActivity.startActivity(intent);
        }catch (Exception ex){
            Log.d("newCall","ex: "+ex.getMessage());
        }

        mActivity.mSupportDelegate.startActivityForResult(intent, null, result -> {
            pubBean.setResultCode(ResultCode.OK);
            pubBean.setMessage(R.string.core_transaction_result_success);
            listener.onTransResult(true);
        });
    }*/
}
