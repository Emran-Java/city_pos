package acquire.core.trans.steps;

import android.text.TextUtils;

import androidx.camera.core.CameraSelector;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.widget.CharacterFilter;
import acquire.base.widget.dialog.image.ImageDialog;
import acquire.core.R;
import acquire.core.constant.Characters;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.constant.Scanner;
import acquire.core.display2.Display1ScanFragment;
import acquire.core.fragment.input.InputInfoFragment;
import acquire.core.fragment.input.InputInfoFragmentArgs;
import acquire.core.fragment.scan.ScanExternFragment;
import acquire.core.fragment.scan.ScanFragment;
import acquire.core.tools.SoundPlayer;
import acquire.core.trans.BaseStep;
import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;
import acquire.sdk.emv.constant.EntryMode;
import acquire.sdk.scan.BHardScanner;
import acquire.sdk.system.BSystem;

/**
 * The result {@link BaseStep} that scans QR code image.
 *
 * @author Janson
 * @date 2019/11/13 16:18
 */
public class ScanStep extends BaseStep {
    /**
     * if true, find original record by QR cide;otherwise, the QR code is used as the payment code
     */
    private final boolean findOrig;


    public ScanStep(boolean findOrig) {
        this.findOrig = findOrig;
    }


    private void setQrCode(String code) {
        if (findOrig) {
            pubBean.setOrigBizOrderNo(code);
            pubBean.setEntryMode(EntryMode.SCAN);
            Record record = new RecordRepository().findByQrOrder(pubBean.getOrigBizOrderNo());
            setOrigRecord(record);
        } else {
            pubBean.setQrPayCode(code);
            pubBean.setEntryMode(EntryMode.SCAN);
        }

    }

    @Override
    public void intercept(Callback callback) {
        pubBean.setEntryMode(EntryMode.SCAN);

        if (findOrig && !TextUtils.isEmpty(pubBean.getOrigBizOrderNo())) {
            //find the original recird and set the scan QR order
            Record record = new RecordRepository().findByQrOrder(pubBean.getOrigBizOrderNo());
            setOrigRecord(record);
            callback.onResult(true);
            return;
        }
        if (!findOrig && !TextUtils.isEmpty(pubBean.getQrPayCode())) {
            //set pay code
            callback.onResult(true);
            return;
        }
        FragmentCallback<String> fragmentCallback = new FragmentCallback<String>() {
            @Override
            public void onSuccess(String qrCode) {
                setQrCode(qrCode);
                callback.onResult(true);
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                switch (errorType) {
                    case ScanFragment.MANUAL_ENTRY:
                        inputQrCode(callback);
                        return;
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
        };
        int scanner = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_SCAN_PRIORITY_SCANNER);
        switch (scanner) {
            case Scanner.EXTERNAL:
                //external scanner
                mActivity.mSupportDelegate.switchContent(ScanExternFragment.newInstance(fragmentCallback));
                break;
            case Scanner.FRONT_CAMERA:
            case Scanner.BACK_CAMERA:
                //camera scanner
                int lensFacing = scanner == Scanner.FRONT_CAMERA ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
                if (Model.X800.equals(BDevice.getDeviceModel())) {
                    //X800
                    mActivity.mSupportDelegate.switchContent(Display1ScanFragment.newInstance(lensFacing, fragmentCallback));
                } else {
                    mActivity.mSupportDelegate.switchContent(ScanFragment.newInstance(lensFacing,  fragmentCallback));
                }
                break;
            case Scanner.HARD_SCANNER:
                if (!BDevice.supportHardScanner()){
                    pubBean.setMessage(R.string.core_scan_hard_scanner_not_exost);
                    pubBean.setResultCode(ResultCode.FL);
                    callback.onResult(false);
                    return;
                }
                //hard scanner
                mActivity.runOnUiThread(() -> {
                    BHardScanner hardScanner = new BHardScanner();
                    new ImageDialog.Builder(mActivity)
                            .setImage(R.drawable.core_hard_scan)
                            .setMessage(R.string.core_scan_ing)
                            .setCancelButton(d -> {
                                pubBean.setMessage(R.string.core_scan_cancel);
                                pubBean.setResultCode(ResultCode.UC);
                                hardScanner.stopScan();
                                callback.onResult(false);
                            })
                            .setShowListener(di ->
                                    hardScanner.startScan( new BHardScanner.HardScannerListener() {
                                        @Override
                                        public void onDecoded(String qrCode) {
                                            LoggerUtils.d("Scan qr code:" + qrCode);
                                            SoundPlayer.getInstance().playScan();
                                            mActivity.runOnUiThread(() -> {
                                                di.dismiss();
                                                setQrCode(qrCode);
                                                callback.onResult(true);
                                            });
                                        }

                                        @Override
                                        public void onError(String msg) {
                                            LoggerUtils.e(msg);
                                            mActivity.runOnUiThread(() -> {
                                                di.dismiss();
                                                pubBean.setMessage(msg);
                                                pubBean.setResultCode(ResultCode.FL);
                                                callback.onResult(false);
                                            });
                                        }
                                    }))
                            .show();
                });
                break;
            default:
                pubBean.setResultCode(ResultCode.FL);
                pubBean.setMessage(R.string.core_scan_unsupport_type);
                callback.onResult(false);
                break;
        }
    }

    /**
     * Manual QR code
     */
    private void inputQrCode(Callback callback) {
        InputInfoFragmentArgs args = new InputInfoFragmentArgs();
        args.setHint(mActivity.getString(R.string.core_scan_manual_hint));
        args.setMinLen(1);
        args.setMaxLen(40);
        args.setFilters(new CharacterFilter(Characters.NUMBER + Characters.LETTER));
        mActivity.mSupportDelegate.switchContent(InputInfoFragment.newInstance(args, new FragmentCallback<String>() {
            @Override
            public void onSuccess(String qrCode) {
                setQrCode(qrCode);
                callback.onResult(true);
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                pubBean.setResultCode(ResultCode.UC);
                pubBean.setMessage(R.string.core_scan_cancel);
                callback.onResult(false);
            }
        }));
    }
}
