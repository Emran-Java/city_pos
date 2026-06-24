package acquire.core.trans.steps;

import android.text.InputType;

import acquire.base.activity.callback.FragmentCallback;
import acquire.core.R;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransType;
import acquire.core.fragment.input.InputInfoFragment;
import acquire.core.fragment.input.InputInfoFragmentArgs;
import acquire.core.trans.BaseStep;
import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;

/**
 * Input orignial auth code.
 *
 * @author Janson
 * @date 2019/5/20 18:16
 */
public class InputOrigAuthCodeStep extends BaseStep {
    @Override
    public void intercept(Callback callback)  {
        if (pubBean.getOrigAuthCode() != null){
            Record record = new RecordRepository().findByAuthCode(TransType.TRANS_PRE_AUTH, pubBean.getOrigAuthCode());
            if(record!=null){
                setOrigRecord(record);
            }
            callback.onResult(true);
            return;
        }
        InputInfoFragmentArgs args = new InputInfoFragmentArgs();
        args.setHint(mActivity.getString(R.string.core_auth_code_title));
        args.setInputType(InputType.TYPE_CLASS_NUMBER);
        args.setMinLen(6);
        args.setMaxLen(6);
        mActivity.mSupportDelegate.switchContent(InputInfoFragment.newInstance(args, new FragmentCallback<String>() {
            @Override
            public void onSuccess(String authCode) {
                //save auth information
                pubBean.setOrigAuthCode(authCode);
                //find original record by auth code
                Record record = new RecordRepository().findByAuthCode(TransType.TRANS_PRE_AUTH,authCode);
                setOrigRecord(record);
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
