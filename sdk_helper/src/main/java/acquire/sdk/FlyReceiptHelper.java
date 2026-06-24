package acquire.sdk;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.newlandnpt.mss.api.ReceiptApiManager;
import com.newlandnpt.mss.api.listener.IReceiptListener;
import com.newlandnpt.mss.api.model.CardScheme;
import com.newlandnpt.mss.api.model.OutCome;
import com.newlandnpt.mss.api.model.ReadCardType;
import com.newlandnpt.mss.api.model.TransCategory;
import com.newlandnpt.mss.api.model.UploadReceiptRequestBean;

import acquire.base.utils.LoggerUtils;


/**
 * A tool for Fly Receipt Service.
 *
 * @author Janson
 * @date 2023/2/1 16:08
 * @since 3.7
 */
public class FlyReceiptHelper {
    private static volatile FlyReceiptHelper instance;

    private FlyReceiptHelper() {
    }

    public static FlyReceiptHelper getInstance() {
        if (instance == null) {
            synchronized (FlyReceiptHelper.class) {
                if (instance == null) {
                    instance = new FlyReceiptHelper();
                }
            }
        }
        return instance;
    }

    /**
     * send the receipt to TOMS Platform
     */
    public void sendReceipt(Context context,ReceiptBean receiptBean, FlyReceiptCallback callback) {
        String sequenceNo = receiptBean.uuid;
        if (sequenceNo != null){
            sequenceNo = sequenceNo.replace("-", "");
        }
        String  oriSequenceNo = receiptBean.oriUuid;
        if (oriSequenceNo != null){
            oriSequenceNo = oriSequenceNo.replace("-", "");
        }
        UploadReceiptRequestBean.Builder builder = new UploadReceiptRequestBean.Builder()
                .setSequenceNo(sequenceNo)
                .setTransType(receiptBean.transType.tomsTransType)
                .setReadCardType(receiptBean.cardEntry.tomsReadCardType)
                .setChannelTid(receiptBean.tid)
                .setChannelMid(receiptBean.mid)
                .setOrderNo(receiptBean.orderNo)
                .setAmountTotal(receiptBean.amount)
                .setCurrencyCode(receiptBean.currency)
                .setCardNoMask(receiptBean.panMask)
                .setOutCome(OutCome.APPROVED)
                .setBatchNo(receiptBean.batchNo)
                .setTraceNo(receiptBean.traceNo)
                .setReferenceNo(receiptBean.refNum)
                .setApprovalCode(receiptBean.approvalCode)
                .addReceiptImage(receiptBean.receipt)
                .setAmountTip(receiptBean.tip)
                .setTvr(receiptBean.tvr)
                .setTsi(receiptBean.tsi)
                .setEReceiptFlag(1);
        if (oriSequenceNo != null){
            builder.setOriSequenceNo(oriSequenceNo);
        }
        if (receiptBean.cardScheme != null){
            builder.setCardScheme(receiptBean.cardScheme.tomsCardScheme);
        }
        ReceiptApiManager.getInstance().uploadTransData(context, false,builder.build(), new IReceiptListener() {
            @Override
            public void onSuccess(String result) {
                LoggerUtils.e("fly receipt response json:"+result);
                ReceiptResponseBean receiptResponseBean = new Gson().fromJson(result,ReceiptResponseBean.class);
                callback.onSuccess(receiptResponseBean);
            }

            @Override
            public void onFailed(int code, String msg) {
                callback.onFailed(code, msg);
            }
        });
    }

    public static class ConvertFlyConsts{
        public enum FlyTransType {
            PURCHASE(TransCategory.PURCHASE),
            VOID(TransCategory.VOID),
            REFUND(TransCategory.REFUND),
            AUTHORIZATION(TransCategory.AUTHORIZATION),
            VOID_AUTHORIZATION(TransCategory.VOID_AUTHORIZATION),
            AUTHORIZATION_CAPTURE(TransCategory.AUTHORIZATION_CAPTURE),
            VOID_AUTHORIZATION_CAPTURE(TransCategory.VOID_AUTHORIZATION_CAPTURE),
            CASHBACK(TransCategory.CASHBACK),
            CASH_ADVANCE(TransCategory.CASH_ADVANCE),
            INSTALLMENT(TransCategory.INSTALLMENT),
            VOID_INSTALLMENT(TransCategory.VOID_INSTALLMENT);

            private final TransCategory tomsTransType;

            FlyTransType(TransCategory tomsTransType) {
                this.tomsTransType = tomsTransType;
            }
        }

        public enum FlyCardScheme {
            CUP(CardScheme.CUP),
            VIS(CardScheme.VIS),
            MCC(CardScheme.MCC),
            MAE(CardScheme.MAE),
            JCB(CardScheme.JCB),
            DCC(CardScheme.DCC),
            AMX(CardScheme.AMX);

            private final CardScheme tomsCardScheme;

            FlyCardScheme(CardScheme tomsCardScheme) {
                this.tomsCardScheme = tomsCardScheme;
            }
        }

        public enum FlyReadCardType {
            MAG(ReadCardType.MSR),
            INSERT(ReadCardType.ICC),
            TAP(ReadCardType.LC),
            QR_CODE(ReadCardType.QR_CODE),
            OTHER(ReadCardType.OTHER);

            private final ReadCardType tomsReadCardType;

            FlyReadCardType(ReadCardType tomsReadCardType) {
                this.tomsReadCardType = tomsReadCardType;
            }
        }
    }
    public static class ReceiptResponseBean {
        private String receiptUrl;
        private String orderNo;
        private String transUuid;

        public String getReceiptUrl() {
            return receiptUrl;
        }

        public void setReceiptUrl(String receiptUrl) {
            this.receiptUrl = receiptUrl;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public String getTransUuid() {
            return transUuid;
        }

        public void setTransUuid(String transUuid) {
            this.transUuid = transUuid;
        }

        @NonNull
        @Override
        public String toString() {
            return "ReceiptResponseBean{" +
                    "receiptUrl='" + receiptUrl + '\'' +
                    ", orderNo='" + orderNo + '\'' +
                    ", transUuid='" + transUuid + '\'' +
                    '}';
        }
    }
    public static class ReceiptBean {
        private String uuid;
        private String orderNo;
        private String mid;
        private String tid;
        private ConvertFlyConsts.FlyTransType transType;
        private long amount;
        private Bitmap receipt;
        private String batchNo;
        private String traceNo;
        private ConvertFlyConsts.FlyCardScheme cardScheme;
        private ConvertFlyConsts.FlyReadCardType cardEntry;
        private String refNum;
        private String currency;
        private String panMask;
        private String approvalCode;
        private String oriUuid;
        private long tip;
        private String tvr;
        private String tsi;

        public void setMid(String mid) {
            this.mid = mid;
        }


        public void setTid(String tid) {
            this.tid = tid;
        }

        public void setTransType(ConvertFlyConsts.FlyTransType transType) {
            this.transType = transType;
        }


        public void setAmount(long amount) {
            this.amount = amount;
        }

        public void setReceipt(Bitmap receipt) {
            this.receipt = receipt;
        }



        public void setBatchNo(String batchNo) {
            this.batchNo = batchNo;
        }

        public void setTraceNo(String traceNo) {
            this.traceNo = traceNo;
        }

        public void setCardScheme(ConvertFlyConsts.FlyCardScheme cardScheme) {
            this.cardScheme = cardScheme;
        }

        public void setCardEntry(ConvertFlyConsts.FlyReadCardType cardEntry) {
            this.cardEntry = cardEntry;
        }

        public void setRefNum(String refNum) {
            this.refNum = refNum;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public void setPanMask(String panMask) {
            this.panMask = panMask;
        }


        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public void setApprovalCode(String approvalCode) {
            this.approvalCode = approvalCode;
        }

        public void setTip(long tip) {
            this.tip = tip;
        }

        public void setTvr(String tvr) {
            this.tvr = tvr;
        }

        public void setTsi(String tsi) {
            this.tsi = tsi;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public void setOriUuid(String oriUuid) {
            this.oriUuid = oriUuid;
        }
    }

    /**
     * send the settle ticket to TOMS Platform
     */
    public void sendSettle(Context context,SettleTicketBean settleTicketBean, FlyReceiptCallback callback) {

//        SettleRequestBean requestBean = new SettleRequestBean.Builder()
//                .addMerchantName(settleTicketBean.merchantName)
//                .addMerchantNo(settleTicketBean.mid)
//                .addTerminalNo(settleTicketBean.tid)
//                .addTransName(settleTicketBean.transName)
//                .addTransCode(settleTicketBean.transCode)
//                .addVoucherType("R")
//                .addDebitTotal(settleTicketBean.debitNumber+"")
//                .addDebitAmount(FormatUtils.formatAmount(settleTicketBean.debitAmount,2,""))
//                .addCreditTotal(settleTicketBean.creditNumber+"")
//                .addCreditAmount(FormatUtils.formatAmount(settleTicketBean.creditAmount,2,""))
//                .build();
//        TOMSClientMssApiManager.getInstance().settle(context, requestBean, new IMSSListener() {
//            @Override
//            public void onSuccess(String result) {
//                callback.onSuccess(result);
//            }
//
//            @Override
//            public void onFailed(int code, String msg) {
//                callback.onFailed(code, msg);
//            }
//        });
        callback.onSuccess(null);
    }
    public static class SettleTicketBean {
        private String merchantName;
        private String mid;
        private String tid;
        private String transName;
        private String transCode;
        private long debitNumber;
        private long debitAmount;
        private long creditNumber;
        private long creditAmount;

        private Bitmap receipt;

        private Bitmap signature;

        public void setMerchantName(String merchantName) {
            this.merchantName = merchantName;
        }

        public void setMid(String mid) {
            this.mid = mid;
        }


        public void setTid(String tid) {
            this.tid = tid;
        }

        public void setTransName(String transName) {
            this.transName = transName;
        }

        public void setTransCode(String transCode) {
            this.transCode = transCode;
        }

        public void setDebitNumber(long debitNumber) {
            this.debitNumber = debitNumber;
        }

        public void setDebitAmount(long debitAmount) {
            this.debitAmount = debitAmount;
        }

        public void setCreditNumber(long creditNumber) {
            this.creditNumber = creditNumber;
        }

        public void setCreditAmount(long creditAmount) {
            this.creditAmount = creditAmount;
        }

        public void setReceipt(Bitmap receipt) {
            this.receipt = receipt;
        }

        public void setSignature(Bitmap signature) {
            this.signature = signature;
        }
    }

    public interface FlyReceiptCallback {
        void onSuccess(ReceiptResponseBean receiptResponseBean);

        void onFailed(int errorCode, String message);
    }

}
