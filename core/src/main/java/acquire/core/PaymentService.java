package acquire.core;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.newland.payment.aidl.IPaymentListener;
import com.newland.payment.aidl.IPaymentService;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.TransTag;
import acquire.core.tools.DataConverter;
import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;

/**
 * Payment service
 *
 * @author Janson
 * @date 2019/4/26 9:49
 */
public class PaymentService extends Service {
    private final static String ACQUIRE_PERMISSION = "payment.permission.acquire";

    @Override
    public IBinder onBind(Intent arg0) {
        return new PaymentInfServiceStub();
    }

    private class PaymentInfServiceStub extends IPaymentService.Stub {

        @Override
        public boolean setParam(String key, String value) {
            if (isValid()){
                return ParamsUtils.setString(key, value);
            }else{
                LoggerUtils.e("Set param ["+key+"] failed, no permission.");
                return false;
            }
        }

        @Override
        public String getParam(String key) {
            if (isValid()) {
                return ParamsUtils.getString(key);
            } else {
                LoggerUtils.e("Get param ["+key+"] failed, no permission.");
                return null;
            }
        }

        private boolean isValid() {
            PackageManager pm = getPackageManager();
            String callingApp = pm.getNameForUid(Binder.getCallingUid());
            try {
                if (callingApp != null){
                    PackageInfo pack = pm.getPackageInfo(callingApp, PackageManager.GET_PERMISSIONS);
                    String[] permissions = pack.requestedPermissions;
                    for (String permission : permissions) {
                        if (ACQUIRE_PERMISSION.equals(permission)) {
                            return true;
                        }
                    }
                }else{
                    LoggerUtils.e("Calling App is null.");
                }
            } catch (PackageManager.NameNotFoundException e) {
                LoggerUtils.e("get package information failed!",e);
            }
            return false;
        }
        @Override
        public void findByOutOrderNo(String origOutOrderNo, IPaymentListener paymentListener) throws RemoteException {
            if (TextUtils.isEmpty(origOutOrderNo)) {
                Bundle error = new Bundle();
                error.putInt(TransTag.RESULT_CODE,ThirdActivity.THIRD_FAIL);
                error.putString(TransTag.MESSAGE,getString(R.string.core_outorder_null));
                paymentListener.onResult(error);
                return;
            }
            RecordRepository recordRepository = new RecordRepository();
            Record record = recordRepository.findByOutOrderNo(origOutOrderNo);
            if (record == null) {
                Bundle error = new Bundle();
                error.putInt(TransTag.RESULT_CODE,ThirdActivity.THIRD_FAIL);
                error.putString(TransTag.MESSAGE,getString(R.string.core_record_no_records_found));
                paymentListener.onResult(error);
                return;
            }
            Bundle bundle = new Bundle();
            DataConverter.recordToBundle(record,bundle);
            bundle.putString(TransTag.MESSAGE,getString(R.string.core_find_success));
            LoggerUtils.d("find record: " + bundle);
            paymentListener.onResult(bundle);
        }


        @Override
        public void findByTraceNo(String origTraceNo, IPaymentListener paymentListener) throws RemoteException {
            if (TextUtils.isEmpty(origTraceNo)) {
                Bundle error = new Bundle();
                error.putInt(TransTag.RESULT_CODE,ThirdActivity.THIRD_FAIL);
                error.putString(TransTag.MESSAGE,getString(R.string.core_trace_number_null));
                paymentListener.onResult(error);
                return;
            }
            RecordRepository recordRepository = new RecordRepository();
            Record record = recordRepository.findByTrace(origTraceNo);
            if (record == null) {
                Bundle error = new Bundle();
                error.putInt(TransTag.RESULT_CODE,ThirdActivity.THIRD_FAIL);
                error.putString(TransTag.MESSAGE,getString(R.string.core_record_no_records_found));
                paymentListener.onResult(error);
                return;
            }
            Bundle bundle = new Bundle();
            DataConverter.recordToBundle(record,bundle);
            bundle.putString(TransTag.MESSAGE,getString(R.string.core_find_success));
            LoggerUtils.d("find record: " + bundle);
            paymentListener.onResult(bundle);
        }

    }
}
