package acquire.core.trans.steps;

import android.text.InputType;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ToastUtils;
import acquire.core.R;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.input.InputInfoFragment;
import acquire.core.fragment.input.InputInfoFragmentArgs;
import acquire.core.fragment.input.InstallmentBracInputInfoFragment;
import acquire.core.trans.BaseStep;

/**
 * The {@link BaseStep} that opens {@link InputInfoFragment} to input installment information.
 *
 * @author Janson
 * @date 2018/11/22 23:12
 */
public class InputInstallmentStep extends BaseStep {

    @Override
    public void intercept(Callback callback)  {
        if (pubBean.getInstalmentTerm() > 0) {
            callback.onResult(true);
            return;
        }
        InputInfoFragmentArgs args = new InputInfoFragmentArgs();
        args.setHint(mActivity.getString(R.string.core_installment_input_hint));
        args.setInputType(InputType.TYPE_CLASS_NUMBER);
        args.setMinLen(1);
        args.setMaxLen(3);
//        mActivity.mSupportDelegate.switchContent(InputInfoFragment.newInstance(args,new FragmentCallback<String>() {
        mActivity.mSupportDelegate.switchContent(InstallmentBracInputInfoFragment.newInstance(args,new FragmentCallback<String>() {
            @Override
            public void onSuccess(String text) {
                int count = Integer.parseInt(text);
                if (count == 0){
                    ToastUtils.showToast(R.string.core_installment_count_not_0);
                    return;
                }
                pubBean.setInstalmentTerm(count);
                callback.onResult(true);
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                switch (errorType) {
                    case FragmentCallback.CANCEL:
                        pubBean.setResultCode(ResultCode.UC);
                        break;
                    case FragmentCallback.TIMEOUT:
                    case FragmentCallback.FAIL:
                    default:
                        pubBean.setResultCode(ResultCode.FL);
                        break;
                }
                pubBean.setMessage(errorMsg);
                callback.onResult(false);
            }
        }));
    }

}
