package acquire.core.trans.steps;

import android.graphics.Bitmap;

import java.io.File;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.BitmapUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.core.display2.Display1SignatureFragment;
import acquire.core.fragment.signature.SignatureFragment;
import acquire.core.tools.SignatureDirManager;
import acquire.core.trans.BaseStep;
import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;

/**
 * electric signature
 *
 * @author Janson
 * @date 2020/11/17 14:21
 */
public class SignatureStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        if (!ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_ELECSIGN_IS_SUPPORT)){
            //Electric signature is off.
            callback.onResult(true);
            return;
        }
        if (pubBean.isFreeSign()){
            LoggerUtils.i("Free sign, skip signature.");
            callback.onResult(true);
            return;
        }
        FragmentCallback<Bitmap> fragmentCallback = new FragmentCallback<Bitmap>() {
            @Override
            public void onSuccess(Bitmap signature) {
                String signatureFile = SignatureDirManager.getSignatureFile(pubBean.getMid(),pubBean.getTid(),pubBean.getTraceNo());
                File parentDir = new File(signatureFile).getParentFile();
                if (!parentDir.exists()){
                    parentDir.mkdir();
                }
                BitmapUtils.saveBmp(signature, signatureFile);
                LoggerUtils.i("Signature success. Path: "+signatureFile);
                pubBean.setSignPath(signatureFile);
                Record record = getRecord();
                if (record != null){
                    record.setSignPath(signatureFile);
                    try {
                        new RecordRepository().update(record);
                        LoggerUtils.d("Update signature flag of record completion！");
                    } catch (Exception e) {
                        LoggerUtils.e("Update signature flag of record failed, trace no:"+ record.getTraceNo(),e);
                    }
                }
                callback.onResult(true);
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                //skip signature
                LoggerUtils.e("Signature cancel.");
                callback.onResult(true);
            }
        };
        //Start signature
        if (Model.X800.equals(BDevice.getDeviceModel())) {
            //X800
            mActivity.mSupportDelegate.switchContent(Display1SignatureFragment.newInstance(fragmentCallback));
        }else{
            mActivity.mSupportDelegate.switchContent(SignatureFragment.newInstance(fragmentCallback));
        }
    }
}
