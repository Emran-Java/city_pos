package acquire.core.trans.impl.preauth;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import acquire.base.activity.callback.SimpleCallback;
import acquire.core.R;
import acquire.core.TransResultListener;
import acquire.core.constant.CoreContent;
import acquire.core.constant.IntentParamKeyContent;
import acquire.core.constant.ResultCode;
import acquire.core.model.FeatureSubMenuModel;
import acquire.core.trans.AbstractTrans;
import acquire.core.fragment.common.submenu.CoreFeatureSubMenuListFragment;

/**
 * Pre-Auth
 *
 * @author Janson
 * @date 2019/7/31 10:26
 */
public class PreAuthMenu extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {

        String titleText = mActivity.getIntent().getStringExtra(IntentParamKeyContent.TRANS_TITLE_TEXT);
        ArrayList<FeatureSubMenuModel> subMenu = CoreContent.PRE_AUTH_MENU;

        Fragment fragment = CoreFeatureSubMenuListFragment.newInstance(titleText, subMenu, new SimpleCallback() {
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

        /*        chain.next(new PreCheckStep(true,true,false))
                .next(new InputAmountStep())
                .next(new ReadCardStep(new InputPinStep(), new PackPreAuthStep(), EntryMode.MAG|EntryMode.INSERT|EntryMode.TAP))
                .next(new AddRecordStep())
//                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));*/

    }
}
