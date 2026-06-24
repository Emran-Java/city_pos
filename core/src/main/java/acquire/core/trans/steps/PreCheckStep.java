package acquire.core.trans.steps;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.R;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.PasswordType;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.password.PasswordFragment;
import acquire.core.tools.TransUtils;
import acquire.core.trans.BaseStep;
import acquire.database.model.Merchant;
import acquire.database.repository.MerchantRepository;
import acquire.database.repository.RecordRepository;
import acquire.sdk.ExtServiceHelper;
import acquire.sdk.ServiceHelper;

/**
 * Check something before transaction
 *
 * @author Janson
 * @date 2021/6/30 10:40
 */
public class PreCheckStep extends BaseStep {
    private final boolean mCheckSettle;
    private boolean mCheckAdminPwd;
    private final boolean mIncKsn;

    /**
     * Check before transaction
     *
     * @param checkSettle  Check whether to settle before this transaction.
     * @param checkAdminPwd Check whether to input the admin password before this transaction.
     */
    public PreCheckStep(boolean incKsn, boolean checkSettle, boolean checkAdminPwd) {
        this.mCheckSettle = checkSettle;
        this.mCheckAdminPwd = checkAdminPwd;
        this.mIncKsn = incKsn;
    }

    @Override
    public void intercept(Callback callback) {
        if(pubBean.isThirdCall()){
            mCheckAdminPwd = false;
        }
        //Check external PIN pad.
        boolean isExternal = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL);
        if (isExternal){
            if (!ExtServiceHelper.getInstance().isInit()){
                pubBean.setResultCode(ResultCode.FL);
                pubBean.setMessage(R.string.core_transaction_result_external_nsdk_error);
                LoggerUtils.e("init external failed!");
                callback.onResult(false);
                return;
            }
        }
        if (!ServiceHelper.getInstance().isInit()){
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_transaction_result_nsdk_error);
            LoggerUtils.e(pubBean.getMessage());
            LoggerUtils.e("init nsdk failed!");
            callback.onResult(false);
            return;
        }
        //Check transaction ParamsConst
        if (!ParamsUtils.getBoolean(TransUtils.getParamsKey(pubBean.getTransType()), true)) {
            //No support,exit transaction
            LoggerUtils.e("unsupported transaction,transaction failed.");
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(mActivity.getString(R.string.core_check_unsupport_trans_format,pubBean.getTransName()));
            LoggerUtils.e(pubBean.getMessage());
            callback.onResult(false);
            return;
        }
        //ksn +1
        if (mIncKsn){
            pubBean.setRequireKsnInc(true);
        }

        // Check whether the current needs to be settled
        if (mCheckSettle) {
            //if merchant's settle halt != 0, need to settle.
            List<Merchant> merchants = new MerchantRepository().findAll();
            for (Merchant merchant : merchants) {
                int merHalt = merchant.getSettleStep();
                if (merHalt != 0){
                    //last settlement not finished ,exit transaction.
                    LoggerUtils.e("last settlement is not completed,transaction failed.");
                    pubBean.setResultCode(ResultCode.FL);
                    pubBean.setMessage(R.string.core_check_settle_status_fail);
                    callback.onResult(false);
                    return;
                }
            }
            RecordRepository recordRepository = new RecordRepository();
            //record count is full.
            int count = recordRepository.getCount();
            if (count >= ParamsUtils.getInt(ParamsConst.PARAMS_KEY_BASE_MAX_TRANS_COUNT, 500)) {
                LoggerUtils.e("Records full,transaction failed.");
                pubBean.setResultCode(ResultCode.FL);
                pubBean.setMessage(R.string.core_check_record_max_limit_fail);
                callback.onResult(false);
                return;
            }
            //rom size is too low.
            if (getAvailableRomSize()< 80*1024*1024){
                LoggerUtils.e("Low rom size,transaction failed.");
                pubBean.setResultCode(ResultCode.FL);
                pubBean.setMessage(R.string.core_check_low_rom_size);
                callback.onResult(false);
                return;
            }
        }
        //check manage password.
        if (mCheckAdminPwd) {
            mActivity.mSupportDelegate.switchContent(PasswordFragment.newInstance(PasswordType.ADMIN,
                    new FragmentCallback<String>() {

                        @Override
                        public void onSuccess(String password) {
                            LoggerUtils.d("Admin password success.");
                            callback.onResult(true);
                        }

                        @Override
                        public void onFail(int errorType, String errorMsg) {
                            LoggerUtils.d("Admin password failed.");
                            switch (errorType) {
                                case FragmentCallback.CANCEL:
                                    pubBean.setResultCode(ResultCode.UC);
                                    pubBean.setMessage(R.string.core_check_admin_pwd_cancel);
                                    break;
                                case FragmentCallback.TIMEOUT:
                                    pubBean.setResultCode(ResultCode.UC);
                                    pubBean.setMessage(R.string.core_check_admin_pwd_timeout);
                                    break;
                                case FragmentCallback.FAIL:
                                default:
                                    pubBean.setResultCode(ResultCode.FL);
                                    pubBean.setMessage(R.string.core_check_admin_pwd_fail);
                                    break;
                            }
                            callback.onResult(false);
                        }
                    }));
        }else{
            //check over
            callback.onResult(true);
        }
    }

    /**
     * Get the device ROM free space size
     */
    private long getAvailableRomSize() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return availableBlocks * blockSize;
        } catch (Exception e) {
            LoggerUtils.e(" abnormal while obtaining the phone ROM free space size.",e);
        }
        return -1;
    }
}
