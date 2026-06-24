package acquire.core;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;


import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.ConvertUtils;
import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import com.newlandnpt.nexo.base.utils.FormatUtils;
import com.newlandnpt.nexo.scap.casp.msgdef.message.components.miscellaneous.ResponseType9;
import com.newlandnpt.nexo.scap.casp.msgdef.message.datatypes.codeset.card.CardDataReading8Code;
import com.newlandnpt.nexo.scap.casp.msgdef.message.datatypes.codeset.iso.ISO4217ActiveCurrency;
import com.newlandnpt.nexo.scap.casp.msgdef.message.datatypes.codeset.response.Response5Code;
import com.newlandnpt.nexo.scap.casp.msgdef.message.datatypes.codeset.retailer.RetailerResultDetail1Code;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.device.service.transmission.TransmissionParam;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.device.service.transmission.TransmissionResult;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.financial.service.constant.TransactionType;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.financial.service.payment.PaymentParam;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.financial.service.payment.PaymentResult;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.financial.service.preauth.PreAuthParam;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.financial.service.preauth.PreAuthResult;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.financial.service.preauth.completion.PreAuthCompletionParam;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.financial.service.preauth.completion.PreAuthCompletionResult;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.financial.service.refund.RefundParam;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.financial.service.refund.RefundResult;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.financial.service.reversal.ReversalParam;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.financial.service.reversal.ReversalResult;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.system.service.abort.AbortParam;
import com.newlandnpt.nexo.scap.retailer.sdk.modules.system.service.abort.AbortResult;
import com.newlandnpt.payment.api.HiPayApi;
import com.newlandnpt.payment.api.PaymentCallBack;

import java.util.Date;
import java.util.List;

import acquire.base.ActivityStackManager;
import acquire.base.BaseApplication;
import acquire.base.activity.BaseActivity;
import acquire.base.activity.BaseFragment;
import acquire.base.utils.AmountUtils;
import acquire.base.utils.DateUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransTag;
import acquire.core.constant.TransType;
import acquire.core.tools.SelfCheckHelper;
import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;
import acquire.sdk.device.BDevice;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Third Call by HiPos
 *
 * @author Janson
 * @date 2025/2/20 14:23
 */
@AutoService(HiPayApi.class)
public class HiPayApiImpl extends HiPayApi {
    private boolean init;

    private void selfCheck() {
        if (!init) {
            SelfCheckHelper.initDevice(BaseApplication.getAppContext());
            init = true;
        }
    }

    @Override
    public void cardPayment(PaymentParam request, PaymentCallBack<PaymentResult> callBack) {
        selfCheck();
        LoggerUtils.d("[HIPOS][CARD PAYMENT][REQUEST]:" + new Gson().toJson(request));
        Context context = BaseApplication.getAppContext();
        Intent intent = new Intent(context, ThirdActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(TransTag.OUT_ORDER_NO, request.getTransactionReference());
        bundle.putString(TransTag.TRANS_TYPE, TransType.TRANS_SALE);

        //Brac
        //bundle.putString(TransTag.CARD_NO, request.getMaskedPAN());
        /*bundle.putString("issuerName", "BRAC BANK PLC");
        bundle.putString(TransTag.BATCH_NO, "102030");
        bundle.putString("merchantId", "777708");*/


        long amount = AmountUtils.getAmountFromString(request.getTotalAmount());
        bundle.putLong(TransTag.AMOUNT, amount);

        //TIP
        long tip = 0L;
        if (!TextUtils.isEmpty(request.getGratuity())) {
            tip = AmountUtils.getAmountFromString(request.getGratuity());
        }
        bundle.putLong(TransTag.TIP, tip);
        if (tip > 0L) {
            bundle.putLong(TransTag.BASE_AMOUNT, amount - tip);
        }

        TransactionType type = request.getTransactionType();
        switch (type) {
            case CSHP:
                bundle.putString(TransTag.TRANS_TYPE, TransType.TRANS_CASH_BACK);
                //Amount for cash back
                long cashBackAmount = 0L;
                if (!TextUtils.isEmpty(request.getCashBack())) {
                    cashBackAmount = AmountUtils.getAmountFromString(request.getCashBack());
                }
                bundle.putLong(TransTag.CASH_AMOUNT, cashBackAmount);
                break;
            case QRCP:
                bundle.putString(TransTag.TRANS_TYPE, TransType.TRANS_QR_CODE);
                break;
            case HCEP:
                bundle.putString(TransTag.TRANS_TYPE, TransType.TRANS_HCE_SALE);
                break;
            default:
                break;
        }
        bundle.putBinder(TransActivity.KEY_TASK, new CallbackBinder(data -> {
            PaymentResult result = new PaymentResult();
            if (data == null) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.REFU);
                callBack.onResult(result);
                return;
            }

            String resultCode = data.getStringExtra(TransTag.RESULT_CODE);
            if (TextUtils.isEmpty(resultCode)) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.REFU);
                callBack.onResult(result);
                return;
            }
            result.setTransactionReference(request.getTransactionReference());
            result.setTransactionType(request.getTransactionType());
            result.setRecipientPartyIdentification(BDevice.getSn());
            result.setMerchantIdentification(data.getStringExtra(TransTag.MID));
            //result.setMerchantIdentificationName(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME));
            String mName = ParamsUtils.getString("MERCHANTNAME");
            result.setMerchantIdentificationName(mName);
            result.setPoiIdentificationId(data.getStringExtra(TransTag.TID));
            result.setAdditionalResponseInformation(data.getStringExtra(TransTag.MESSAGE));
            result.setAccountType(request.getAccountType());

            switch (resultCode) {
                case ResultCode.OK:
                    result.setCurrency(ISO4217ActiveCurrency.getCurrencyByCode(data.getStringExtra(TransTag.CURRENCY_CODE)));
                    result.setResponse(Response5Code.SUCC);
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        if (extras.containsKey(TransTag.TIME)) {
                            result.setTransactionDateTime(DateUtils.getDate(extras.getString(TransTag.DATE)+extras.getString(TransTag.TIME),DateUtils.YYYYMMDDHHMMSS));
                        }
                        if (extras.containsKey(TransTag.AMOUNT)) {
                            result.setTotalAmount(AmountUtils.getAmountWithDigit(String.valueOf(extras.getLong(TransTag.AMOUNT))));
                        }
                        if (extras.containsKey(TransTag.TIP)) {
                            result.setGratuity(AmountUtils.getAmountWithDigit(String.valueOf(extras.getLong(TransTag.TIP))));
                        }
                        if (extras.containsKey(TransTag.CASH_AMOUNT)) {
                            result.setGratuity(AmountUtils.getAmountWithDigit(String.valueOf(extras.getLong(TransTag.TIP))));
                        }
                        result.setPoiTransactionIdentification(extras.getString(TransTag.TRACE_NO));
//                        result.setMaskedPAN(FormatUtils.maskCardNo(extras.getString(TransTag.CARD_NO)));
                        String cardNumber = extras.getString(TransTag.CARD_NO);
                        String mid = "";
                        String batchNo = "";
                        String authCode = "";
                        //if(cardNumber==null || cardNumber.isEmpty()){
                            try{
                                String traceNo = extras.getString(TransTag.TRACE_NO);
                                LoggerUtils.d("[HIPOS][CARD PAYMENT][REQUEST] get record data by TRACE_NO:" + traceNo);
                                RecordRepository recordRepository = new RecordRepository();
                                Record record = recordRepository.findByTrace(extras.getString(TransTag.TRACE_NO));
                                authCode = record.getAuthCode();
                                if(authCode==null || authCode.isEmpty()) authCode =record.getOrigAuthCode();

                                cardNumber = record.getCardNo();
                                mid  = record.getMid();
                                batchNo  = record.getBatchNo();
                                LoggerUtils.d("[HIPOS][CARD PAYMENT][REQUEST] get record data currency code :" + record.getCurrencyCode());
                                LoggerUtils.d("[HIPOS][CARD PAYMENT][REQUEST] get record :" + record.toString());
                                LoggerUtils.d("[HIPOS][CARD PAYMENT][REQUEST] get MID :" + record.getMid());

                            }catch (Exception ex){
                                LoggerUtils.e("[HIPOS][CARD PAYMENT][REQUEST] ex:" + ex.getMessage());
                            }
                        //}
                        LoggerUtils.d("[HIPOS][CARD PAYMENT][REQUEST] get mName:" + mName);

                        String maskCardNum = FormatUtils.maskCardNo(cardNumber);
                        LoggerUtils.d("[HIPOS][CARD PAYMENT][REQUEST] maskCardNum:" + maskCardNum);
                        result.setMaskedPAN(maskCardNum);
                        result.setMerchantIdentification(mid);
                        //MID
                        //Merchant Name
                        //Approval code

                        result.setAdditionalSaleData("{\"batchNumber\":\""+batchNo+"\", \"approvalCode\":\""+authCode+"\", \"issuerName\":\"BRAC BANK PLC\",\"merchantId\":\""+mid+"\",\"merchantName\":\""+mName+"\" }");    //{"batchNumber":"+batchNo+", "issuerName":"BRAC BANK PLC"}

                        int entryMode = data.getIntExtra(TransTag.ENTRY_MODE, EntryMode.NONE);
                        result.setCardDataEntryMode(getCardDataReading8Code(entryMode));
                        result.setCardBrand(extras.getString(TransTag.CARD_SCHEME));
                        result.setIssuerReferenceData(extras.getString(TransTag.REFERENCE_NO));
                    }
                    break;
                case ResultCode.UC:
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.CANC);
                    break;
                case ResultCode.FL:
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.REFU);
                default:
                    break;
            }
            LoggerUtils.d("[HIPOS][CARD PAYMENT][RESPONSE]:" + new Gson().toJson(result));
            callBack.onResult(result);
        }));
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void scanCodePayment(PaymentParam request, PaymentCallBack<PaymentResult> callBack) {
        selfCheck();
        LoggerUtils.d("[HIPOS][SCAN PAYMENT][REQUEST]:" + new Gson().toJson(request));
        Context context = BaseApplication.getAppContext();
        Intent intent = new Intent(context, ThirdActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(TransTag.OUT_ORDER_NO, request.getTransactionReference());
        bundle.putString(TransTag.TRANS_TYPE, TransType.TRANS_SCAN_PAY);
        long amount = AmountUtils.getAmountFromString(request.getTotalAmount());
        bundle.putLong(TransTag.AMOUNT, amount);

//        bundle.putString(TransTag.CARD_NO, request.getMaskedPAN());
        /*bundle.putString("issuerName", "BRAC BANK PLC");
        bundle.putString(TransTag.BATCH_NO, "102030");
        bundle.putString("merchantId", "777709");*/

        bundle.putBinder(TransActivity.KEY_TASK, new CallbackBinder(data -> {
            PaymentResult result = new PaymentResult();
            if (data == null) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.REFU);
                callBack.onResult(result);
                return;
            }

            String resultCode = data.getStringExtra(TransTag.RESULT_CODE);
            if (TextUtils.isEmpty(resultCode)) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.REFU);
                callBack.onResult(result);
                return;
            }
            result.setTransactionReference(request.getTransactionReference());
            result.setTransactionType(request.getTransactionType());
            result.setRecipientPartyIdentification(BDevice.getSn());
            result.setMerchantIdentification(data.getStringExtra(TransTag.MID));
            result.setMerchantIdentificationName(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME));
            result.setPoiIdentificationId(data.getStringExtra(TransTag.TID));
            result.setAdditionalResponseInformation(data.getStringExtra(TransTag.MESSAGE));
            result.setAccountType(request.getAccountType());

            switch (resultCode) {
                case ResultCode.OK:
                    result.setCurrency(ISO4217ActiveCurrency.getCurrencyByCode(data.getStringExtra(TransTag.CURRENCY_CODE)));
                    result.setResponse(Response5Code.SUCC);
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        if (extras.containsKey(TransTag.TIME)) {
                            result.setTransactionDateTime(DateUtils.getDate(extras.getString(TransTag.DATE)+extras.getString(TransTag.TIME),DateUtils.YYYYMMDDHHMMSS));
                        }
                        if (extras.containsKey(TransTag.AMOUNT)) {
                            result.setTotalAmount(AmountUtils.getAmountWithDigit(String.valueOf(extras.getLong(TransTag.AMOUNT))));
                        }
                        if (extras.containsKey(TransTag.TIP)) {
                            result.setGratuity(AmountUtils.getAmountWithDigit(String.valueOf(extras.getLong(TransTag.TIP))));
                        }
                        if (extras.containsKey(TransTag.CASH_AMOUNT)) {
                            result.setGratuity(AmountUtils.getAmountWithDigit(String.valueOf(extras.getLong(TransTag.TIP))));
                        }
                        result.setPoiTransactionIdentification(extras.getString(TransTag.TRACE_NO));
                        result.setMaskedPAN(FormatUtils.maskCardNo(extras.getString(TransTag.CARD_NO)));
                        int entryMode = data.getIntExtra(TransTag.ENTRY_MODE, EntryMode.NONE);
                        result.setCardDataEntryMode(getCardDataReading8Code(entryMode));
                        result.setIssuerReferenceData(extras.getString(TransTag.BIZ_ORDER_NO));
                    }
                    break;
                case ResultCode.UC:
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.CANC);
                    break;
                case ResultCode.FL:
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.REFU);
                default:
                    break;
            }
            LoggerUtils.d("[HIPOS][SCAN PAYMENT][RESPONSE]:" + new Gson().toJson(result));
            callBack.onResult(result);
        }));
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void qrCodePayment(PaymentParam request, PaymentCallBack<PaymentResult> callBack) {
        selfCheck();
        LoggerUtils.d("[HIPOS][SCAN PAYMENT][REQUEST]:" + new Gson().toJson(request));
        Context context = BaseApplication.getAppContext();
        Intent intent = new Intent(context, ThirdActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(TransTag.OUT_ORDER_NO, request.getTransactionReference());
        bundle.putString(TransTag.TRANS_TYPE, TransType.TRANS_QR_CODE);
        long amount = AmountUtils.getAmountFromString(request.getTotalAmount());
        bundle.putLong(TransTag.AMOUNT, amount);

//        bundle.putString(TransTag.CARD_NO, request.getMaskedPAN());
        /*bundle.putString("issuerName", "BRAC BANK PLC");
        bundle.putString(TransTag.BATCH_NO, "102030");
        bundle.putString("merchantId", "777710");*/

        bundle.putBinder(TransActivity.KEY_TASK, new CallbackBinder(data -> {
            PaymentResult result = new PaymentResult();
            if (data == null) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.REFU);
                callBack.onResult(result);
                return;
            }

            String resultCode = data.getStringExtra(TransTag.RESULT_CODE);
            if (TextUtils.isEmpty(resultCode)) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.REFU);
                callBack.onResult(result);
                return;
            }
            result.setTransactionReference(request.getTransactionReference());
            result.setTransactionType(request.getTransactionType());
            result.setRecipientPartyIdentification(BDevice.getSn());
            result.setMerchantIdentification(data.getStringExtra(TransTag.MID));
            result.setMerchantIdentificationName(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME));
            result.setPoiIdentificationId(data.getStringExtra(TransTag.TID));
            result.setAdditionalResponseInformation(data.getStringExtra(TransTag.MESSAGE));
            result.setAccountType(request.getAccountType());

            switch (resultCode) {
                case ResultCode.OK:
                    result.setCurrency(ISO4217ActiveCurrency.getCurrencyByCode(data.getStringExtra(TransTag.CURRENCY_CODE)));
                    result.setResponse(Response5Code.SUCC);
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        if (extras.containsKey(TransTag.TIME)) {
                            result.setTransactionDateTime(DateUtils.getDate(extras.getString(TransTag.DATE)+extras.getString(TransTag.TIME),DateUtils.YYYYMMDDHHMMSS));
                        }
                        if (extras.containsKey(TransTag.AMOUNT)) {
                            result.setTotalAmount(AmountUtils.getAmountWithDigit(String.valueOf(extras.getLong(TransTag.AMOUNT))));
                        }
                        if (extras.containsKey(TransTag.TIP)) {
                            result.setGratuity(AmountUtils.getAmountWithDigit(String.valueOf(extras.getLong(TransTag.TIP))));
                        }
                        result.setPoiTransactionIdentification(extras.getString(TransTag.TRACE_NO));
                        result.setMaskedPAN(FormatUtils.maskCardNo(extras.getString(TransTag.CARD_NO)));
                        int entryMode = data.getIntExtra(TransTag.ENTRY_MODE, EntryMode.NONE);
                        result.setCardDataEntryMode(getCardDataReading8Code(entryMode));
                        result.setIssuerReferenceData(extras.getString(TransTag.BIZ_ORDER_NO));
                    }
                    break;
                case ResultCode.UC:
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.CANC);
                    break;
                case ResultCode.FL:
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.REFU);
                default:
                    break;
            }
            LoggerUtils.d("[HIPOS][QRCODE PAYMENT][RESPONSE]:" + new Gson().toJson(result));
            callBack.onResult(result);
        }));
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void refund(RefundParam request, PaymentCallBack<RefundResult> callBack) {
        selfCheck();
        LoggerUtils.d("[HIPOS][REFUND][REQUEST]:" + new Gson().toJson(request));
        Context context = BaseApplication.getAppContext();
        Intent intent = new Intent(context, ThirdActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(TransTag.OUT_ORDER_NO, request.getTransactionReference());
        bundle.putString(TransTag.TRANS_TYPE, TransType.TRANS_REFUND);
        bundle.putString(TransTag.ORIG_REFERENCE_NO, request.getOrgIssuerReferenceData());
        long amount = AmountUtils.getAmountFromString(request.getTotalAmount());
        bundle.putLong(TransTag.AMOUNT, amount);

//        bundle.putString(TransTag.CARD_NO, request.getMaskedPAN());
      /*  bundle.putString("issuerName", "BRAC BANK PLC");
        bundle.putString(TransTag.BATCH_NO, "102030");
        bundle.putString("merchantId", "777711");*/

        Date time = request.getOrgTransactionDateTime();
        String date = DateUtils.formatTime(time, DateUtils.YYYYMMDD);
        bundle.putSerializable(TransTag.ORIG_DATE, date);

        bundle.putBinder(TransActivity.KEY_TASK, new CallbackBinder(data -> {
            RefundResult result = new RefundResult();
            if (data == null) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.REFU);
                callBack.onResult(result);
                return;
            }
            String resultCode = data.getStringExtra(TransTag.RESULT_CODE);
            if (TextUtils.isEmpty(resultCode)) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.REFU);
                callBack.onResult(result);
                return;
            }

            result.setTransactionReference(request.getTransactionReference());
            result.setTransactionType(request.getTransactionType());
            result.setRecipientPartyIdentification(BDevice.getSn());
            result.setMerchantIdentification(data.getStringExtra(TransTag.MID));
            result.setMerchantIdentificationName(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME));
            result.setPoiIdentificationId(data.getStringExtra(TransTag.TID));
            result.setAdditionalResponseInformation(data.getStringExtra(TransTag.MESSAGE));
            result.setAccountType(request.getAccountType());

            switch (resultCode) {
                case ResultCode.OK:
                    result.setResponse(Response5Code.SUCC);
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        if (extras.containsKey(TransTag.TIME)) {
                            result.setTransactionDateTime(DateUtils.getDate(extras.getString(TransTag.DATE)+extras.getString(TransTag.TIME),DateUtils.YYYYMMDDHHMMSS));
                        }
                        if (extras.containsKey(TransTag.AMOUNT)) {
                            result.setTotalAmount(AmountUtils.getAmountWithDigit(String.valueOf(extras.getLong(TransTag.AMOUNT))));
                        }

                        result.setPoiTransactionIdentification(extras.getString(TransTag.TRACE_NO));
                        result.setMaskedPAN(FormatUtils.maskCardNo(extras.getString(TransTag.CARD_NO)));
                        int entryMode = data.getIntExtra(TransTag.ENTRY_MODE, EntryMode.NONE);
                        result.setCardDataEntryMode(getCardDataReading8Code(entryMode));
                        result.setCardBrand(extras.getString(TransTag.CARD_SCHEME));
                        result.setIssuerReferenceData(extras.getString(TransTag.REFERENCE_NO));
                    }
                    break;
                case ResultCode.UC:
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.CANC);
                    break;
                case ResultCode.FL:
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.REFU);
                    callBack.onResult(result);
                default:
                    break;
            }
            LoggerUtils.d("[HIPOS][REFUND][RESPONSE]:" + new Gson().toJson(result));
            callBack.onResult(result);
        }));
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void reversal(ReversalParam request, PaymentCallBack<ReversalResult> callBack) {
        selfCheck();
        LoggerUtils.d("[HIPOS][REVERSAL][REQUEST]:" + new Gson().toJson(request));
        Context context = BaseApplication.getAppContext();
        Intent intent = new Intent(context, ThirdActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(TransTag.OUT_ORDER_NO, request.getTransactionReference());

        ReversalResult result = new ReversalResult();
        RecordRepository recordRepository = new RecordRepository();
        Record record = recordRepository.findByOutOrderNo(request.getOrgTransactionReference());
        if (record == null) {
            result.setResponse(Response5Code.FAIL);
            result.setResponseReason(RetailerResultDetail1Code.TNFD);
            callBack.onResult(result);
            return;
        }
        bundle.putString(TransTag.ORIG_TRACE_NO, record.getTraceNo());
        String voidTransType;
        switch (record.getTransType()) {
            case TransType.TRANS_PRE_AUTH:
                voidTransType = TransType.TRANS_VOID_PRE_AUTH;
                bundle.putLong(TransTag.AMOUNT,record.getAmount());
                bundle.putString(TransTag.ORIG_AUTH_CODE,record.getAuthCode());
                bundle.putString(TransTag.DATE,record.getDate());
                break;
            case TransType.TRANS_AUTH_COMPLETE:
                voidTransType = TransType.TRANS_VOID_AUTH_COMPLETE;
                break;
            case TransType.TRANS_INSTALLMENT:
                voidTransType = TransType.TRANS_VOID_INSTALLMENT;
                break;
            case TransType.TRANS_SALE:
                voidTransType = TransType.TRANS_VOID_SALE;
                break;
            default:
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.TNFD);
                callBack.onResult(result);
                return;
        }
        bundle.putString(TransTag.TRANS_TYPE, voidTransType);

        bundle.putBinder(TransActivity.KEY_TASK, new CallbackBinder(data -> {
            if (data == null) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.REFU);
                callBack.onResult(result);
                return;
            }
            String resultCode = data.getStringExtra(TransTag.RESULT_CODE);
            if (TextUtils.isEmpty(resultCode)) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.REFU);
                callBack.onResult(result);
                return;
            }

            result.setTransactionReference(request.getTransactionReference());
            result.setTransactionType(TransactionType.CRDP);
            result.setRecipientPartyIdentification(BDevice.getSn());
            result.setAdditionalResponseInformation(data.getStringExtra(TransTag.MESSAGE));
            result.setMerchantIdentification(data.getStringExtra(TransTag.MID));
            result.setMerchantIdentificationName(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME));
            result.setPoiIdentificationId(data.getStringExtra(TransTag.TID));

            switch (resultCode) {
                case ResultCode.OK:
                    result.setCurrency(ISO4217ActiveCurrency.getCurrencyByCode(data.getStringExtra(TransTag.CURRENCY_CODE)));
                    result.setResponse(Response5Code.SUCC);
                    result.setAccountType(request.getAccountType());
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        if (extras.containsKey(TransTag.TIME)) {
                            result.setTransactionDateTime(DateUtils.getDate(extras.getString(TransTag.DATE)+extras.getString(TransTag.TIME),DateUtils.YYYYMMDDHHMMSS));
                        }
                        if (extras.containsKey(TransTag.AMOUNT)) {
                            result.setTotalAmount(AmountUtils.getAmountWithDigit(String.valueOf(extras.getLong(TransTag.AMOUNT))));
                        }
                        if (extras.containsKey(TransTag.TIP)) {
                            result.setGratuity(AmountUtils.getAmountWithDigit(String.valueOf(extras.getLong(TransTag.TIP))));
                        }
                        result.setPoiTransactionIdentification(extras.getString(TransTag.TRACE_NO));
                        result.setMaskedPAN(FormatUtils.maskCardNo(extras.getString(TransTag.CARD_NO)));
                        int entryMode = intent.getIntExtra(TransTag.ENTRY_MODE, EntryMode.NONE);
                        result.setCardDataEntryMode(getCardDataReading8Code(entryMode));
                        result.setCardBrand(extras.getString(TransTag.CARD_SCHEME));
                        result.setIssuerReferenceData(extras.getString(TransTag.REFERENCE_NO));
                    }
                    break;
                case ResultCode.UC:
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.CANC);
                    break;
                case ResultCode.FL:
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.REFU);
                default:
                    break;
            }
            LoggerUtils.d("[HIPOS][REVERSAL][RESPONSE]:" + new Gson().toJson(result));
            callBack.onResult(result);
        }));
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void preAuth(PreAuthParam request, PaymentCallBack<PreAuthResult> callBack) {
        selfCheck();
        LoggerUtils.d("[HIPOS][PREAUTH][REQUEST]:" + new Gson().toJson(request));
        Context context = BaseApplication.getAppContext();
        Intent intent = new Intent(context, ThirdActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(TransTag.OUT_ORDER_NO, request.getTransactionReference());
        bundle.putString(TransTag.TRANS_TYPE, TransType.TRANS_PRE_AUTH);
        long amount = AmountUtils.getAmountFromString(request.getTotalAmount());
        bundle.putLong(TransTag.AMOUNT, amount);

//        bundle.putString(TransTag.CARD_NO, request.getMaskedPAN());
        /*bundle.putString("issuerName", "BRAC BANK PLC");
        bundle.putString(TransTag.BATCH_NO, "102030");
        bundle.putString("merchantId", "777712");*/

        bundle.putBinder(TransActivity.KEY_TASK, new CallbackBinder(data -> {
            PreAuthResult result = new PreAuthResult();
            if (data == null) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.REFU);
                callBack.onResult(result);
                return;
            }
            String resultCode = data.getStringExtra(TransTag.RESULT_CODE);
            if (TextUtils.isEmpty(resultCode)) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.REFU);
                callBack.onResult(result);
                return;
            }
            result.setTransactionReference(request.getTransactionReference());
            result.setTransactionType(request.getTransactionType());
            result.setRecipientPartyIdentification(BDevice.getSn());
            result.setAdditionalResponseInformation(data.getStringExtra(TransTag.MESSAGE));
            result.setMerchantIdentification(data.getStringExtra(TransTag.MID));
            result.setMerchantIdentificationName(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME));
            result.setPoiIdentificationId(data.getStringExtra(TransTag.TID));
            result.setAdditionalResponseInformation(data.getStringExtra(TransTag.MESSAGE));

            switch (resultCode) {
                case ResultCode.OK:
                    result.setCurrency(ISO4217ActiveCurrency.getCurrencyByCode(data.getStringExtra(TransTag.CURRENCY_CODE)));
                    result.setResponse(Response5Code.SUCC);
                    result.setAccountType(request.getAccountType());
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        if (extras.containsKey(TransTag.TIME)) {
                            result.setTransactionDateTime(DateUtils.getDate(extras.getString(TransTag.DATE)+extras.getString(TransTag.TIME),DateUtils.YYYYMMDDHHMMSS));
                        }
                        if (extras.containsKey(TransTag.AMOUNT)) {
                            result.setTotalAmount(AmountUtils.getAmountWithDigit(String.valueOf(extras.getLong(TransTag.AMOUNT))));
                        }
                        result.setPoiTransactionIdentification(extras.getString(TransTag.TRACE_NO));
                        result.setMaskedPAN(FormatUtils.maskCardNo(extras.getString(TransTag.CARD_NO)));
                        //result.setMaskedPAN("999999******9999");
                        int entryMode = intent.getIntExtra(TransTag.ENTRY_MODE, EntryMode.NONE);
                        result.setCardDataEntryMode(getCardDataReading8Code(entryMode));
                        result.setCardBrand(extras.getString(TransTag.CARD_SCHEME));
                        result.setIssuerReferenceData(extras.getString(TransTag.REFERENCE_NO));
                        result.setAuthorisationCode(extras.getString(TransTag.AUTH_CODE));
                    }
                    break;
                case ResultCode.UC:
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.CANC);
                    break;
                case ResultCode.FL:
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.REFU);
                default:
                    break;
            }
            LoggerUtils.d("[HIPOS][PREAUTH][RESPONSE]:" + new Gson().toJson(result));
            callBack.onResult(result);
        }));
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void preAuthCompletion(PreAuthCompletionParam request, PaymentCallBack<PreAuthCompletionResult> callBack) {
        selfCheck();
        LoggerUtils.d("[HIPOS][PREAUTH COMPLETION][REQUEST]:" + new Gson().toJson(request));
        Context context = BaseApplication.getAppContext();
        Intent intent = new Intent(context, ThirdActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(TransTag.OUT_ORDER_NO, request.getTransactionReference());
        bundle.putString(TransTag.TRANS_TYPE, TransType.TRANS_AUTH_COMPLETE);
        //AMOUNT
        long amount = AmountUtils.getAmountFromString(request.getTotalAmount());
        bundle.putLong(TransTag.AMOUNT, amount);

        //bundle.putString(TransTag.CARD_NO, request.getMaskedPAN());
        /*bundle.putString("issuerName", "BRAC BANK PLC");
        bundle.putString(TransTag.BATCH_NO, "102030");
        bundle.putString("merchantId", "777713");*/

        PreAuthCompletionResult result = new PreAuthCompletionResult();
        RecordRepository recordRepository = new RecordRepository();
        if (TextUtils.isEmpty(request.getOrgAuthorisationCode())) {
            if (TextUtils.isEmpty(request.getOrgTransactionReference())) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.TNFD);
                callBack.onResult(result);
                return;
            } else {
                Record record = recordRepository.findByOutOrderNo(request.getOrgTransactionReference());
                if (record == null) {
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.TNFD);
                    callBack.onResult(result);
                    return;
                }
                bundle.putString(TransTag.ORIG_DATE, record.getDate());
                bundle.putString(TransTag.ORIG_AUTH_CODE, record.getAuthCode());
            }

        } else {
            Date time = request.getOrgTransactionDateTime();
            String date = DateUtils.formatTime(time, DateUtils.YYYYMMDD);
            bundle.putString(TransTag.ORIG_DATE, date);
            bundle.putString(TransTag.ORIG_AUTH_CODE, request.getOrgAuthorisationCode());
        }


        bundle.putBinder(TransActivity.KEY_TASK, new CallbackBinder(data -> {
            if (data == null) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.REFU);
                callBack.onResult(result);
                return;
            }
            String resultCode = data.getStringExtra(TransTag.RESULT_CODE);
            if (TextUtils.isEmpty(resultCode)) {
                result.setResponse(Response5Code.FAIL);
                result.setResponseReason(RetailerResultDetail1Code.REFU);
                callBack.onResult(result);
                return;
            }
            result.setTransactionReference(request.getTransactionReference());
            result.setTransactionType(request.getTransactionType());
            result.setRecipientPartyIdentification(BDevice.getSn());
            result.setAdditionalResponseInformation(data.getStringExtra(TransTag.MESSAGE));
            result.setMerchantIdentification(data.getStringExtra(TransTag.MID));
            result.setMerchantIdentificationName(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME));
            result.setPoiIdentificationId(data.getStringExtra(TransTag.TID));
            result.setAdditionalResponseInformation(data.getStringExtra(TransTag.MESSAGE));

            switch (resultCode) {
                case ResultCode.OK:
                    result.setCurrency(ISO4217ActiveCurrency.getCurrencyByCode(data.getStringExtra(TransTag.CURRENCY_CODE)));
                    result.setResponse(Response5Code.SUCC);
                    result.setAccountType(request.getAccountType());
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        if (extras.containsKey(TransTag.TIME)) {
                            result.setTransactionDateTime(DateUtils.getDate(extras.getString(TransTag.DATE)+extras.getString(TransTag.TIME),DateUtils.YYYYMMDDHHMMSS));
                        }
                        if (extras.containsKey(TransTag.AMOUNT)) {
                            result.setTotalAmount(AmountUtils.getAmountWithDigit(String.valueOf(extras.getLong(TransTag.AMOUNT))));

                        }
                        if (extras.containsKey(TransTag.TIP)) {
                            result.setGratuity(AmountUtils.getAmountWithDigit(String.valueOf(extras.getLong(TransTag.TIP))));
                        }
                        result.setPoiTransactionIdentification(extras.getString(TransTag.TRACE_NO));
                        result.setMaskedPAN(FormatUtils.maskCardNo(extras.getString(TransTag.CARD_NO)));
                        int entryMode = extras.getInt(TransTag.ENTRY_MODE, EntryMode.NONE);
                        result.setCardDataEntryMode(getCardDataReading8Code(entryMode));
                        result.setCardBrand(extras.getString(TransTag.CARD_SCHEME));
                        result.setIssuerReferenceData(extras.getString(TransTag.REFERENCE_NO));
                        result.setAuthorisationCode(extras.getString(TransTag.AUTH_CODE));
                    }
                    break;
                case ResultCode.UC:
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.CANC);
                    break;
                case ResultCode.FL:
                    result.setResponse(Response5Code.FAIL);
                    result.setResponseReason(RetailerResultDetail1Code.REFU);
                default:
                    break;
            }
            LoggerUtils.d("[HIPOS][PREAUTH COMPLETION][RESPONSE]:" + new Gson().toJson(result));
            callBack.onResult(result);
        }));
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void transmitMessage(TransmissionParam transmissionParam, PaymentCallBack<TransmissionResult> callBack) {
        ToastUtils.showToast("Received message:" + ConvertUtils.bytes2String(transmissionParam.getMessageToSend()));
        TransmissionResult transmissionResult = new TransmissionResult();
        transmissionResult.setRecipientPartyIdentification(BDevice.getSn()); ;
        transmissionResult.setReceivedMessage(ConvertUtils.string2Bytes("SUCCESS"));
        transmissionResult.processResponseStdType(ResponseType9.success());
        callBack.onResult(transmissionResult);
    }


    @Override
    public void abort(AbortParam abortParam, PaymentCallBack<AbortResult> paymentCallBack) {
        LoggerUtils.d("[HIPOS][ABORT][REQUEST]:" + new Gson().toJson(abortParam));
        Activity topActivity = ActivityStackManager.getTopActivity();
        if (topActivity instanceof TransActivity) {
            new Handler(Looper.getMainLooper()).post(topActivity::onBackPressed);
        }
    }

    private CardDataReading8Code getCardDataReading8Code(int entryMode) {
        switch (entryMode) {
            case EntryMode.MAG:
                return CardDataReading8Code.MGST;
            case EntryMode.INSERT:
                return CardDataReading8Code.CICC;
            case EntryMode.TAP:
                return CardDataReading8Code.CTLS;
            case EntryMode.MANUAL:
                return null;
            case EntryMode.SCAN:
            case EntryMode.SHOW_QR:
                return CardDataReading8Code.QRCD;
            case EntryMode.NONE:
            default:
                return null;
        }
    }

    public Fragment getTopFragment(BaseActivity activity) {
        List<Fragment> fragments = activity.getSupportFragmentManager().getFragments();
        if (fragments.isEmpty()) {
            return null;
        }
        int size = fragments.size();
        BaseFragment fragment = null;
        for (int i = size - 1; i >= 0; i--) {
            fragment = (BaseFragment) fragments.get(i);
            if (fragment != null) {
                break;
            }
        }
        return fragment;
    }
}
