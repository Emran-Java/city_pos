package acquire.core.trans.steps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.core.R;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransStatus;
import acquire.core.fragment.record.RecordFragment;
import acquire.core.fragment.record.RecordTipAdjustDetailFragment;
import acquire.core.trans.BaseStep;
import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;

/**
 * Input the original record by the trace number
 *
 * @author Janson
 * @date 2019/7/25 11:26
 */
public class FindOrigTipAdjustTraceStep extends BaseStep {
    private String[] mOrigTransTypes;
    private int[] mStatus ;

    /**
     * Input original transaction type
     *
     * @param origTransTypes original transaction type
     */
    public FindOrigTipAdjustTraceStep(String... origTransTypes) {
        if (origTransTypes == null) {
            return ;
        }
        //filter invalid types
        List<String> validTypes = new ArrayList<>();
        for (String transType : origTransTypes) {
            if (transType != null) {
                validTypes.add(transType);
            }
        }
        if (!validTypes.isEmpty()) {
            this.mOrigTransTypes = validTypes.toArray(new String[0]);
        }
    }

    /**
     * Input original transaction type
     *
     * @param origTransTypes original transaction type
     * @param status transaction status.
     * @see TransStatus
     */
    public FindOrigTipAdjustTraceStep(String[]origTransTypes, int[]status) {
        this.mOrigTransTypes = origTransTypes;
        this.mStatus = status;
    }


    @Override
    public void intercept(Callback callback) {
        if (pubBean.getOrigTraceNo() != null){

            Record record = new RecordRepository().findByTrace(pubBean.getOrigTraceNo());

            if (record == null){
                pubBean.setMessage(R.string.core_record_no_records_found);
                pubBean.setResultCode(ResultCode.FL);
                callback.onResult(false);
                return;
            }
            if (mOrigTransTypes != null){
                boolean correct = false;
                for (String origTransType : mOrigTransTypes) {
                    if (origTransType != null && origTransType.equals(record.getTransType())){
                        correct = true;
                        break;
                    }
                }
                if (!correct){
                    pubBean.setMessage(R.string.core_record_no_records_found);
                    pubBean.setResultCode(ResultCode.FL);
                    callback.onResult(false);
                    return;
                }
            }

            if (mStatus != null && mStatus.length > 0) {
               boolean statusResult = Arrays.stream(mStatus).anyMatch(num -> num == record.getStatus());
                if (!statusResult){
                    pubBean.setMessage(R.string.core_record_no_records_found);
                    pubBean.setResultCode(ResultCode.FL);
                    callback.onResult(false);
                    return;
                }
            }

            pubBean.setAmount(record.getAmount());
            pubBean.setTipAmount(record.getTipAmount());
            setOrigRecord(record);
            mActivity.mSupportDelegate.switchContent(RecordTipAdjustDetailFragment.newInstance(mActivity.getString(R.string.core_transaction_name_tip_adjust),record,  new FragmentCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
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
            return;
        }
        mActivity.mSupportDelegate.switchContent(RecordFragment.newInstance(mOrigTransTypes,mStatus,new FragmentCallback<Record>() {
            @Override
            public void onSuccess(Record record) {
                pubBean.setOrigTraceNo(record.getTraceNo());

                pubBean.setAmount(record.getAmount());
                pubBean.setTipAmount(record.getTipAmount());
                pubBean.setReferNo(record.getReferNo());
                pubBean.setOrigReferNo(record.getOrigReferNo());

                setOrigRecord(record);
                mActivity.mSupportDelegate.switchContent(RecordTipAdjustDetailFragment.newInstance(mActivity.getString(R.string.core_transaction_name_tip_adjust), record, new FragmentCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onResult(true);
                    }

                    @Override
                    public void onFail(int errorType, String errorMsg) {
                        mActivity.mSupportDelegate.popBackFragment(1);
                    }
                }));
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                switch (errorType) {
                    case FragmentCallback.CANCEL:
                    case FragmentCallback.TIMEOUT:
                        pubBean.setResultCode(ResultCode.UC);
                        break;
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
