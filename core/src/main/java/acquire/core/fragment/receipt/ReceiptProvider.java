package acquire.core.fragment.receipt;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

import acquire.base.activity.BaseActivity;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.BitmapUtils;
import acquire.base.utils.BytesUtils;
import acquire.base.utils.DateUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.TlvUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.base.utils.emv.EmvTag;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.dialog.progress.ProgressDialog;
import acquire.core.R;
import acquire.core.constant.CardSchemes;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.TransType;
import acquire.core.fragment.print.PrintFragment;
import acquire.core.fragment.print.PrintViewModel;
import acquire.core.trans.pack.CallerException;
import acquire.core.trans.pack.json.JsonCaller;
import acquire.core.trans.pack.json.npi_receipt.NPIReceiptConst;
import acquire.core.trans.pack.json.npi_receipt.NPIReceiptUploadReq;
import acquire.core.trans.pack.json.npi_receipt.NPIReceiptUploadRsp;
import acquire.core.trans.pack.json.npi_sale.HCESaleConst;
import acquire.core.trans.pack.json.pixcel.PixcelConst;
import acquire.core.trans.pack.json.pixcel.PixcelReceiptReq;
import acquire.core.trans.pack.json.pixcel.PixcelReceiptResp;
import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;
import acquire.sdk.FlyReceiptHelper;
import acquire.sdk.device.BDevice;
import acquire.sdk.emv.constant.EntryMode;

public class ReceiptProvider {

    public static boolean hasSupportedReceipts(){
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_RECEIPT_AUTO_PRINT)){
            return BDevice.supportPrint() || ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL) ;
        }else {
            return !ReceiptTypeItem.getReceiptTypeList().isEmpty();
        }

    }

    public static void provideReceipt(BaseActivity activity, Record record, boolean isReprint,int receiptType, final ReceiptCallback callback) {
        switch (receiptType) {
            case ReceiptTypeItem.MERCHANT_PAPER:
                providePaperReceipt(activity, record, PrintViewModel.RECEIPT_OWNER_MERCHANT,isReprint, callback);
                break;
            case ReceiptTypeItem.CUSTOMER_PAPER:
                providePaperReceipt(activity, record, PrintViewModel.RECEIPT_OWNER_CUSTOMER,isReprint, callback);
                break;
            case ReceiptTypeItem.E_RECEIPT_NPI_DEMO:
                provideNPIReceipt(activity, record, callback);
                break;
            case ReceiptTypeItem.E_RECEIPT_PIXCEL:
                providePixcelReceipt(activity, record, callback);
                break;
            case ReceiptTypeItem.FLY_RECEIPT:
                provideFlyReceipt(activity, record, callback);
                break;
            default:
                ToastUtils.showToast("UnSupport");
                callback.finish();
        }


    }


    private static void providePaperReceipt(BaseActivity activity, Record record, int receiptOwner,boolean isReprint, final ReceiptCallback callback) {
        activity.mSupportDelegate.switchContent(PrintFragment.newReceiptInstance(record, isReprint, receiptOwner, new FragmentCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                activity.mSupportDelegate.popBackFragment(1);
                callback.finish();
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                //always success
                ToastUtils.showToast(errorMsg);
                activity.mSupportDelegate.popBackFragment(1);
                callback.finish();
            }
        }));
    }

    private static void provideNPIReceipt(BaseActivity activity, Record record, final ReceiptCallback callback) {
        ThreadPool.execute(() -> {
            if (TextUtils.isEmpty(record.getNpiDigitalReceiptUrl())) {
                if (TextUtils.isEmpty(record.getDigitalReceiptNo())){
                    record.setDigitalReceiptNo(HCESaleConst.generateThirdNo());
                    new RecordRepository().update(record);
                }
                //request bean
                Bitmap bitmap = PrintViewModel.getReceipt(record, false, PrintViewModel.RECEIPT_OWNER_CUSTOMER);
                String transDetail = "data:image/png;base64," + BitmapUtils.bitmapToBase64(bitmap);
                NPIReceiptUploadReq reqBean = new NPIReceiptUploadReq(
                        record.getDigitalReceiptNo(), record.getAmount(),
                        DateUtils.formatTime(record.getDate() + record.getTime()), transDetail);
                String reqUrl = NPIReceiptConst.HCE_BASE_URL + NPIReceiptConst.HCE_ORDER_UPLOAD + record.getDigitalReceiptNo();
                NPIReceiptUploadRsp rspBean = null;
                try {
                    rspBean = new JsonCaller.Builder(activity)
                            .packComm(reqUrl, reqBean, NPIReceiptUploadRsp.class, false);
                } catch (CallerException e) {
                    ToastUtils.showToast(e.getMessage());
                    callback.finish();
                    return;
                }
                if ((rspBean == null || rspBean.getCode() != 200)) {
                    ToastUtils.showToast(rspBean != null ? rspBean.getMsg() : "");
                    callback.finish();
                    return;
                }
                LoggerUtils.d("upload receipt success");
                String receiptUrl = NPIReceiptConst.HCE_BASE_URL + NPIReceiptConst.HCE_ORDER_CHECK + record.getDigitalReceiptNo();
                record.setNpiDigitalReceiptUrl(receiptUrl);
                new RecordRepository().update(record);
            }
            activity.mSupportDelegate.switchContent(HCEReceiptFragment.newInstance(record.getNpiDigitalReceiptUrl(), new FragmentCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    activity.mSupportDelegate.popBackFragment(1);
                    callback.finish();
                }

                @Override
                public void onFail(int errorType, String errorMsg) {
                    if (errorType != FragmentCallback.CANCEL) {
                        ToastUtils.showToast(errorMsg);
                    }
                    activity.mSupportDelegate.popBackFragment(1);
                    callback.finish();
                }
            }));
        });
    }

    private static void providePixcelReceipt(BaseActivity activity, Record record, final ReceiptCallback callback) {
        ThreadPool.execute(() -> {
            if (TextUtils.isEmpty(record.getDigitalReceiptUrl())) {
                if (TextUtils.isEmpty(record.getDigitalReceiptNo())){
                    record.setDigitalReceiptNo(HCESaleConst.generateThirdNo());
                    new RecordRepository().update(record);
                }
                //request bean
                PixcelReceiptReq reqBean = new PixcelReceiptReq();
                reqBean.merchantId = record.getMid();
                reqBean.deviceId = BDevice.getSn();
                reqBean.terminalTransactionTime = DateUtils.formatTimeISO8601(record.getDate() + record.getTime());
                reqBean.transactionId = record.getDigitalReceiptNo();
                reqBean.transactionAmount = record.getAmount() + "";
                reqBean.currencyCodeAlpha3 = CurrencyUtils.getCurrency(record.getCurrencyCode()).getAlphaCode();
                reqBean.selectedService = "PAYMENT";
                reqBean.transactionType = "CARD";
                reqBean.transacitonResult = "APPOVED";
                reqBean.maskedPan = record.getCardNo();
                PixcelConst.getAID(reqBean, record.getEntryMode(), record.getEmvPrintData());
                reqBean.technologySelected = PixcelConst.getDescription(record.getEntryMode());
                String reqUrl = PixcelConst.HCE_BASE_URL + PixcelConst.STATE_COMPLETE;
                Map<String, String> headers = new HashMap<>();
                headers.put(PixcelConst.API_KEY, PixcelConst.NPT_API_KEY);
                PixcelReceiptResp rspBean = null;
                try {
                    rspBean = new JsonCaller.Builder(activity)
                            .withHeaders(headers)
                            .packComm(reqUrl, reqBean, PixcelReceiptResp.class, false);
                } catch (Exception e) {
                    ToastUtils.showToast(e.getMessage());
                    callback.finish();
                    return;
                }

                if ((rspBean == null || TextUtils.isEmpty(rspBean.url))) {
                    ToastUtils.showToast("Upload Failed");
                    callback.finish();
                    return;
                }
                LoggerUtils.d("upload receipt success");
                String receiptUrl = rspBean.url;
                record.setDigitalReceiptUrl(receiptUrl);
                new RecordRepository().update(record);
            }
            activity.mSupportDelegate.switchContent(HCEReceiptFragment.newInstance(record.getDigitalReceiptUrl(), new FragmentCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    activity.mSupportDelegate.popBackFragment(1);
                    callback.finish();
                }

                @Override
                public void onFail(int errorType, String errorMsg) {
                    ToastUtils.showToast(errorMsg);
                    activity.mSupportDelegate.popBackFragment(1);
                    callback.finish();
                }
            }));
        });
    }

    private static void provideFlyReceipt(BaseActivity activity, Record record, final ReceiptCallback callback) {
        if (!TextUtils.isEmpty(record.getTomsDigitalReceiptUrl())){
            activity.mSupportDelegate.switchContent(HCEReceiptFragment.newInstance(record.getTomsDigitalReceiptUrl(), new FragmentCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    activity.mSupportDelegate.popBackFragment(1);
                    callback.finish();
                }

                @Override
                public void onFail(int errorType, String errorMsg) {
                    ToastUtils.showToast(errorMsg);
                    activity.mSupportDelegate.popBackFragment(1);
                    callback.finish();
                }
            }));
            return;
        }
        LoggerUtils.d("FlyReceipt request");
        //send Fly Receipt
        FlyReceiptHelper.ReceiptBean receiptBean = new FlyReceiptHelper.ReceiptBean();
        receiptBean.setAmount(record.getAmount());
        receiptBean.setTraceNo(record.getTraceNo());
        receiptBean.setBatchNo(record.getBatchNo());
        switch (record.getEntryMode()) {
            case EntryMode.MAG:
                receiptBean.setCardEntry(FlyReceiptHelper.ConvertFlyConsts.FlyReadCardType.MAG);
                break;
            case EntryMode.INSERT:
                receiptBean.setCardEntry(FlyReceiptHelper.ConvertFlyConsts.FlyReadCardType.INSERT);
                break;
            case EntryMode.TAP:
                receiptBean.setCardEntry(FlyReceiptHelper.ConvertFlyConsts.FlyReadCardType.TAP);
                break;
            case EntryMode.SCAN:
            case EntryMode.SHOW_QR:
                receiptBean.setCardEntry(FlyReceiptHelper.ConvertFlyConsts.FlyReadCardType.QR_CODE);
                break;
            default:
                receiptBean.setCardEntry(FlyReceiptHelper.ConvertFlyConsts.FlyReadCardType.OTHER);
                break;
        }
        switch (record.getTransType()) {
            case TransType.TRANS_SALE:
            case TransType.TRANS_SCAN_PAY:
            case TransType.TRANS_QR_CODE:
            case TransType.TRANS_HCE_SALE:
                receiptBean.setTransType(FlyReceiptHelper.ConvertFlyConsts.FlyTransType.PURCHASE);
                break;
            case TransType.TRANS_REFUND:
            case TransType.TRANS_QR_REFUND:
                receiptBean.setTransType(FlyReceiptHelper.ConvertFlyConsts.FlyTransType.REFUND);
                break;
            case TransType.TRANS_PRE_AUTH:
                receiptBean.setTransType(FlyReceiptHelper.ConvertFlyConsts.FlyTransType.AUTHORIZATION);
                break;
            case TransType.TRANS_AUTH_COMPLETE:
                receiptBean.setTransType(FlyReceiptHelper.ConvertFlyConsts.FlyTransType.AUTHORIZATION_CAPTURE);
                break;
            case TransType.TRANS_VOID_SALE:
                receiptBean.setTransType(FlyReceiptHelper.ConvertFlyConsts.FlyTransType.VOID);
                break;
            case TransType.TRANS_VOID_AUTH_COMPLETE:
                receiptBean.setTransType(FlyReceiptHelper.ConvertFlyConsts.FlyTransType.VOID_AUTHORIZATION_CAPTURE);
                break;
            case TransType.TRANS_VOID_PRE_AUTH:
                receiptBean.setTransType(FlyReceiptHelper.ConvertFlyConsts.FlyTransType.VOID_AUTHORIZATION);
                break;
            case TransType.TRANS_INSTALLMENT:
                receiptBean.setTransType(FlyReceiptHelper.ConvertFlyConsts.FlyTransType.INSTALLMENT);
                break;
            case TransType.TRANS_VOID_INSTALLMENT:
                receiptBean.setTransType(FlyReceiptHelper.ConvertFlyConsts.FlyTransType.VOID_INSTALLMENT);
                break;
            case TransType.TRANS_CASH_ADVANCE:
                receiptBean.setTransType(FlyReceiptHelper.ConvertFlyConsts.FlyTransType.CASH_ADVANCE);
                break;
            case TransType.TRANS_CASH_BACK:
                receiptBean.setTransType(FlyReceiptHelper.ConvertFlyConsts.FlyTransType.CASHBACK);
                break;
            default:
                callback.finish();
                break;
        }
        if (record.getCardScheme() != null) {
            switch (record.getCardScheme()) {
                case CardSchemes.AMEX:
                    receiptBean.setCardScheme(FlyReceiptHelper.ConvertFlyConsts.FlyCardScheme.AMX);
                    break;
                case CardSchemes.CUP:
                    receiptBean.setCardScheme(FlyReceiptHelper.ConvertFlyConsts.FlyCardScheme.CUP);
                    break;
                case CardSchemes.JCB:
                    receiptBean.setCardScheme(FlyReceiptHelper.ConvertFlyConsts.FlyCardScheme.JCB);
                    break;
                case CardSchemes.MASTER_CARD:
                    receiptBean.setCardScheme(FlyReceiptHelper.ConvertFlyConsts.FlyCardScheme.MAE);
                    break;
                case CardSchemes.DISCOVER:
                case CardSchemes.DINERS:
                    receiptBean.setCardScheme(FlyReceiptHelper.ConvertFlyConsts.FlyCardScheme.DCC);
                    break;
                case CardSchemes.MCCS:
                    receiptBean.setCardScheme(FlyReceiptHelper.ConvertFlyConsts.FlyCardScheme.MCC);
                    break;
                case CardSchemes.RUPAY:
                    receiptBean.setCardScheme(FlyReceiptHelper.ConvertFlyConsts.FlyCardScheme.VIS);
                    break;
                case CardSchemes.VISA:
                    receiptBean.setCardScheme(FlyReceiptHelper.ConvertFlyConsts.FlyCardScheme.VIS);
                    break;
                default:
                    break;
            }
        }
        receiptBean.setUuid(record.getUuid());
        if (!TextUtils.isEmpty(record.getOrigTraceNo())){
            Record origRecord = new RecordRepository().findByTrace(record.getOrigTraceNo());
            if (origRecord!=null){
                receiptBean.setOriUuid(origRecord.getUuid());
            }
        }
        receiptBean.setMid(record.getMid());
        receiptBean.setTid(record.getTid());
        receiptBean.setRefNum(record.getReferNo());
        receiptBean.setTip(record.getTipAmount());
        receiptBean.setApprovalCode(record.getAuthCode());
        SparseArray<byte[]> emvTlvs = TlvUtils.getTlvList(BytesUtils.hexToBytes(record.getEmvPrintData()));
        if (emvTlvs != null) {
            for (int i = 0; i < emvTlvs.size(); i++) {
                int tag = emvTlvs.keyAt(i);
                byte[] value = emvTlvs.get(tag);
                switch (tag) {
                    case EmvTag.TAG_95_TM_TVR:
                        receiptBean.setTvr(BytesUtils.bcdToString(value));
                        break;
                    case EmvTag.TAG_9B_TM_TSI:
                        receiptBean.setTsi(BytesUtils.bcdToString(value));
                        break;
                    default:
                        break;
                }
            }
        }

        CurrencyUtils.CurrencyBean currencyBean = CurrencyUtils.getCurrency(record.getCurrencyCode());
        receiptBean.setCurrency(currencyBean.getAlphaCode());
        if (!TextUtils.isEmpty(record.getCardNo())) {
            receiptBean.setPanMask(record.getCardNo().substring(record.getCardNo().length() - 4));
        }
        receiptBean.setReceipt(PrintViewModel.getReceipt(record, false, 0));
        receiptBean.setOrderNo(record.getOutOrderNo());
        activity.runOnUiThread(() -> {
            ProgressDialog progressDialog = new ProgressDialog.Builder(activity)
                    .setContent(R.string.core_fly_receipt_communicate_prompt)
                    .show();
            ThreadPool.execute(() ->
                    FlyReceiptHelper.getInstance().sendReceipt(activity, receiptBean, new FlyReceiptHelper.FlyReceiptCallback() {
                        @Override
                        public void onSuccess(FlyReceiptHelper.ReceiptResponseBean receiptResponseBean) {
                            LoggerUtils.d("FlyReceipt success:" + receiptResponseBean);
                            activity.runOnUiThread(progressDialog::dismiss);
                            ToastUtils.showToast(R.string.core_fly_receipt_success);
                            record.setTomsDigitalReceiptUrl(receiptResponseBean.getReceiptUrl());
                            new RecordRepository().update(record);
                            activity.mSupportDelegate.switchContent(HCEReceiptFragment.newInstance(receiptResponseBean.getReceiptUrl(), new FragmentCallback<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    activity.mSupportDelegate.popBackFragment(1);
                                    callback.finish();
                                }

                                @Override
                                public void onFail(int errorType, String errorMsg) {
                                    ToastUtils.showToast(errorMsg);
                                    activity.mSupportDelegate.popBackFragment(1);
                                    callback.finish();
                                }
                            }));
                        }

                        @Override
                        public void onFailed(int code, String msg) {
                            LoggerUtils.e("FlyReceipt error code:" + code + ", error msg:" + msg);
                            activity.runOnUiThread(progressDialog::dismiss);
                            ToastUtils.showToast(activity.getString(R.string.core_fly_receipt_communicate_error, msg));
                            callback.finish();
                        }
                    })
            );
        });


    }


    public interface ReceiptCallback {

        void finish();
    }

}
