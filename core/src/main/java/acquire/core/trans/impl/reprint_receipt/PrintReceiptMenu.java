package acquire.core.trans.impl.reprint_receipt;

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
 * Print Receipt
 *
 * @author Emran
 * @date 2026/6/9 16:05
 */
public class PrintReceiptMenu extends AbstractTrans {

    @Override
    public void transact(TransResultListener listener) {

        String titleText = mActivity.getIntent().getStringExtra(IntentParamKeyContent.TRANS_TITLE_TEXT);
        ArrayList<FeatureSubMenuModel> subMenu = CoreContent.PRINT_RECEIPT_MENU;

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
