package acquire.core.trans.impl.installment;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import acquire.base.activity.callback.SimpleCallback;
import acquire.core.R;
import acquire.core.TransResultListener;
import acquire.core.constant.CoreContent;
import acquire.core.constant.IntentParamKeyContent;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.common.submenu.CoreFeatureSubMenuListFragment;
import acquire.core.model.FeatureSubMenuModel;
import acquire.core.trans.AbstractTrans;

/**
 * Installment pay
 *
 * @author Janson
 * @date 2021/8/3 17:42
 */
public class InstallmentMenu extends AbstractTrans {
//    PAY_FLEX_MENU

    @Override
    public void transact(TransResultListener listener) {

        String titleText = mActivity.getIntent().getStringExtra(IntentParamKeyContent.TRANS_TITLE_TEXT);
        ArrayList<FeatureSubMenuModel> subMenu = CoreContent.PAY_FLEX_MENU;

        Fragment fragment = CoreFeatureSubMenuListFragment.newInstance(titleText, subMenu, new SimpleCallback() {
            @Override
            public void result() {
                pubBean.setResultCode(ResultCode.OK);
                pubBean.setMessage(R.string.core_transaction_result_success);
                listener.onTransResult(true);
            }
        });
        mActivity.mSupportDelegate.switchBracContent(fragment,0);
    }


    /*@Override
    public void transact(TransResultListener listener) {
        chain.next(new PreCheckStep(true, true,  false))
                .next(new InputAmountStep())
                .next(new InputInstallmentStep())
                .next(new ReadCardStep(new InputPinStep(), new PackInstallmentStep(),
                        EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP))
                .next(new AddRecordStep())
                .next(new SignatureStep())
//                .next(new PrintReceiptStep())
//                .next(new FlyReceiptStep())
                .proceed(isSucc -> showResult(isSucc, listener));
    }

    */
}
