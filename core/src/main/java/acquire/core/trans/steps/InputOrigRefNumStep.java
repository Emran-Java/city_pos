package acquire.core.trans.steps;

import android.text.InputType;

import acquire.base.activity.callback.FragmentCallback;
import acquire.core.R;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.input.InputInfoFragment;
import acquire.core.fragment.input.InputInfoFragmentArgs;
import acquire.core.trans.BaseStep;
import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;

/**
 * Input original reference number.
 *
 * @author Janson
 * @date 2019/7/25 11:26
 */
public class InputOrigRefNumStep extends BaseStep {
    @Override
    public void intercept(Callback callback) {
        if (pubBean.getOrigReferNo() != null){
            RecordRepository recordRepository = new RecordRepository();
            Record origRecord = recordRepository.findByReferNum(pubBean.getOrigReferNo());
            setOrigRecord(origRecord);
            callback.onResult(true);
            return;
        }
        InputInfoFragmentArgs args = new InputInfoFragmentArgs();
        args.setHint(mActivity.getString(R.string.core_refund_orig_refnum));
        args.setInputType(InputType.TYPE_CLASS_NUMBER);
        args.setMinLen(12);
        args.setMaxLen(12);
        mActivity.mSupportDelegate.switchContent(InputInfoFragment.newInstance(args, new FragmentCallback<String>() {
            @Override
            public void onSuccess(String refNum) {
                RecordRepository recordRepository = new RecordRepository();
                Record origRecord = recordRepository.findByReferNum(refNum);
                setOrigRecord(origRecord);
                pubBean.setOrigReferNo(refNum);
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