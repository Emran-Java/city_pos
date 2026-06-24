package acquire.core.trans.steps;

import android.graphics.Bitmap;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.TransType;
import acquire.core.display2.ResultPresentation;
import acquire.core.fragment.bottom_sheet.QRShowBottomSheet;
import acquire.core.fragment.print.PrintFragment;
import acquire.core.fragment.print.PrintViewModel;
import acquire.core.tools.CardInfoUtility;
import acquire.core.trans.BaseStep;
import acquire.database.model.Record;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;

/**
 * Print receipt
 *
 * @author Janson
 * @date 2023/4/26 14:09
 */
public class PrintReceiptStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        if (!BDevice.supportPrint()&& !ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
            callback.onResult(true);
            return;
        }
        if (Model.X800.equals(BDevice.getDeviceModel())) {
            mActivity.runOnUiThread(() -> {
                ResultPresentation presentation = new ResultPresentation(mActivity, true, pubBean.getMessage());
                presentation.show();
            });
        }

        Record record = getRecord();

        boolean isShow = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT,false);
        if(isShow && record.getTransType().equalsIgnoreCase(TransType.TRANS_SALE)){

            QRShowBottomSheet qrSheet = QRShowBottomSheet.getInstance();
            //Bitmap bitmap, String topText, String bottomText
            Bitmap bitmap = PrintViewModel.getBracQRImage(record);
            qrSheet.setDataListener(bitmap, "TK: "+CardInfoUtility.formatAmount(record.getAmount()),"SEND SUCCESS",new QRShowBottomSheet.ButtonClickListener() {
                @Override
                public void onButtonClick(String buttonCode) {
                    printPaper(buttonCode, record, callback);
                }
            } );
            qrSheet.setCancelable(false);
            qrSheet.show(mActivity.getSupportFragmentManager(), "MyBottomSheet");
        }else{
            printPaper(QRShowBottomSheet.BUTTON_QR_CODE_PRINT, record, callback);
        }
    }

    private void printPaper(String buttonCode, Record record, Callback callback) {

        if(buttonCode.equalsIgnoreCase(QRShowBottomSheet.BUTTON_QR_CODE_PRINT)) {
            mActivity.mSupportDelegate.switchContent(PrintFragment.newReceiptInstance(record, false, new FragmentCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    callback.onResult(true);
                }

                @Override
                public void onFail(int errorType, String errorMsg) {
                    //always success
                    callback.onResult(true);
                }
            }));
        }else{
            callback.onResult(true);
        }
    }
}
