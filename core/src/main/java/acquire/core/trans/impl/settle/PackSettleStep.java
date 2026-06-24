package acquire.core.trans.impl.settle;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.widget.dialog.progress.ProgressDialog;
import acquire.core.R;
import acquire.core.constant.CallerResult;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransType;
import acquire.core.tools.DataConverter;
import acquire.core.trans.BaseStep;
import acquire.core.trans.pack.PackField;
import acquire.core.trans.pack.iso.Caller;
import acquire.database.model.Merchant;
import acquire.database.model.Record;
import acquire.database.repository.MerchantRepository;
import acquire.database.repository.RecordRepository;

/**
 * The {@link BaseStep} that packs and sends settlement data to the server
 *
 * @author Janson
 * @date 2019/7/29 14:57
 */
class PackSettleStep extends BaseStep {
    private final MerchantRepository merchantRepository = new MerchantRepository();


    @Override
    public void intercept(Callback callback) {
        if (!doReversal()) {
            //check reversal
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_reversal_fail);
            callback.onResult(false);
            return;
        }
        for (Merchant merchant : pubBean.getSettleMerchants()) {
            //merchant's settleHalt indicates this merchant is ready to this step.
            if (merchant.getSettleStep() <= Settle.STEP_SETTLEMENT_SENT) {
                if (!sendSettle(merchant)) {
                    callback.onResult(false);
                    return;
                }
                merchant.setSettleStep(Settle.STEP_BATCH_UP);
                merchantRepository.update(merchant);
            }
            if (merchant.getSettleStep() <= Settle.STEP_BATCH_UP) {
                if (!merchant.isSettleEqual()) {
                    boolean result = batchUp(merchant);
                    if (!result) {
                        closeProgressUi();
                        callback.onResult(false);
                        return;
                    } else {
                        if (sendSettleAfterBatch(merchant)) {
                            closeProgressUi();
                            callback.onResult(true);
                        }
                    }
                }
                closeProgressUi();
                merchant.setSettleStep(Settle.STEP_BATCH_UP + 1);
                merchantRepository.update(merchant);
            }
        }
        callback.onResult(true);
    }

    /**
     * send a merchant total amount to the server
     */
    private boolean sendSettle(Merchant merchant) {
        initPubBean(merchant);
        pubBean.setProcessCode("920000");
        if(merchant.getType().equalsIgnoreCase("EMI")) {
            pubBean.setProcessCode("920080");
        }
        pubBean.setMessageId("0500");
        //pack 8583
        iso8583.initPack();
        try {
            iso8583.setField(0, pubBean.getMessageId());
            iso8583.setField(3, pubBean.getProcessCode());
            iso8583.setField(11, pubBean.getTraceNo());
            if (!TextUtils.isEmpty(pubBean.getNii())) {
                iso8583.setField(24, pubBean.getNii());
            }
            iso8583.setField(41, pubBean.getTid());
            iso8583.setField(42, pubBean.getMid());

            //iso8583.setField(49, pubBean.getCurrencyCode());
            //iso8583.setField(57, pubBean.getKsn());
            //iso8583.setField(62, pubBean.getBatchNo());

            String bNo = pubBean.getBatchNo();
            String bAsciiNo = decimalToAsciiHex(bNo);

            iso8583.setField(60, bAsciiNo);
            //iso8583.setField(60, pubBean.getBatchNo());
            //iso8583.setField(60, "0006303030303031");

            //String test = "0001303034303030303030303030383030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030";

            //iso8583.setField(63, test);

           /* SettleData amountObj = SettleData.getInstance();
            long totalSettleAmount = amountObj.getTotalAmount();
            long totalSettleNumber = amountObj.getTotalNumber();

            LoggerUtils.d("totalSettleAmount: "+totalSettleAmount+", totalSettleNumber: "+totalSettleNumber);

            String bracField63 = getField63Brac(totalSettleAmount);
            LoggerUtils.d("bracField63: "+bracField63);*/

            String f63 = PackField.packField63(pubBean.getMid(), pubBean.getTid());
            //String bracField63Ascii = decToAscii(f63);
            String bracField63Ascii = field63Padding(90, true, f63);
            LoggerUtils.d("decToAscii: " + bracField63Ascii);

            //iso8583.setField(63, PackField.packField63(pubBean.getMid(),pubBean.getTid()));
            iso8583.setField(63, bracField63Ascii);

            //iso8583.setField(64, Packet8583.getMac(pubBean,iso8583));
        } catch (Exception e) {
            LoggerUtils.e("set 8583 field error!", e);
            pubBean.setMessage(mActivity.getString(R.string.core_comm_pack_error) + e.getMessage());
            pubBean.setResultCode(ResultCode.FL);
            return false;
        }

        //send to the server
        int ret = new Caller.Builder(mActivity, pubBean, iso8583)
                .packComm();
        if (ret != CallerResult.OK) {
            return false;
        }
        String responseCode = iso8583.getField(39);
        merchant.setSettleEqual(ResultCode.OK.equals(responseCode));
        /* Save settlement date */
        merchant.setSettleDate(pubBean.getDate());
        merchant.setSettleTime(pubBean.getTime());
        //set the halt of this merchant to HALT_SETTLE_UP+1
        merchant.setSettleStep(Settle.STEP_SETTLEMENT_SENT + 1);
        return true;
    }

    private boolean sendSettleAfterBatch(Merchant merchant) {
        initPubBean(merchant);
        pubBean.setProcessCode("960000");
        if(merchant.getType().equalsIgnoreCase("EMI")) {
            pubBean.setProcessCode("960080");
        }
        pubBean.setMessageId("0500");
        //pack 8583
        iso8583.initPack();
        try {
            iso8583.setField(0, pubBean.getMessageId());
            iso8583.setField(3, pubBean.getProcessCode());
            iso8583.setField(11, pubBean.getTraceNo());
            if (!TextUtils.isEmpty(pubBean.getNii())) {
                iso8583.setField(24, pubBean.getNii());
            }
            iso8583.setField(41, pubBean.getTid());
            iso8583.setField(42, pubBean.getMid());

            //iso8583.setField(49, pubBean.getCurrencyCode());
            //iso8583.setField(57, pubBean.getKsn());
            //iso8583.setField(62, pubBean.getBatchNo());

            String bNo = pubBean.getBatchNo();
            String bAsciiNo = decimalToAsciiHex(bNo);

            iso8583.setField(60, bAsciiNo);
            //iso8583.setField(60, pubBean.getBatchNo());
            //iso8583.setField(60, "0006303030303031");

            //String test = "0001303034303030303030303030383030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030";

            //iso8583.setField(63, test);

           /* SettleData amountObj = SettleData.getInstance();
            long totalSettleAmount = amountObj.getTotalAmount();
            long totalSettleNumber = amountObj.getTotalNumber();

            LoggerUtils.d("totalSettleAmount: "+totalSettleAmount+", totalSettleNumber: "+totalSettleNumber);

            String bracField63 = getField63Brac(totalSettleAmount);
            LoggerUtils.d("bracField63: "+bracField63);*/

            String f63 = PackField.packField63(pubBean.getMid(), pubBean.getTid());
            //String bracField63Ascii = decToAscii(f63);
            String bracField63Ascii = field63Padding(90, true, f63);
            LoggerUtils.d("decToAscii: " + bracField63Ascii);

            //iso8583.setField(63, PackField.packField63(pubBean.getMid(),pubBean.getTid()));
            //iso8583.setField(63, bracField63Ascii);

            //iso8583.setField(64, Packet8583.getMac(pubBean,iso8583));
        } catch (Exception e) {
            LoggerUtils.e("set 8583 field error!", e);
            pubBean.setMessage(mActivity.getString(R.string.core_comm_pack_error) + e.getMessage());
            pubBean.setResultCode(ResultCode.FL);
            return false;
        }

        //send to the server
        int ret = new Caller.Builder(mActivity, pubBean, iso8583)
                .packComm();
        if (ret != CallerResult.OK) {
            return false;
        }
        String responseCode = iso8583.getField(39);
        merchant.setSettleEqual(ResultCode.OK.equals(responseCode));
        /* Save settlement date */
        merchant.setSettleDate(pubBean.getDate());
        merchant.setSettleTime(pubBean.getTime());
        //set the halt of this merchant to HALT_SETTLE_UP+1
        merchant.setSettleStep(Settle.STEP_SETTLEMENT_SENT + 1);
        return true;
    }

/*

    private String field63DecToAsciiPadding(int totalL, boolean isPaddingRight, String mainText) {
        int wantChar = totalL-(mainText.length()/1);
        StringBuilder hexValPaddingResult = new StringBuilder();
        //hexValPaddingResult.append("0090");

        if(isPaddingRight){
            hexValPaddingResult.append(mainText);
        }

        StringBuilder tmpValPaddingResult = new StringBuilder();

        for(int i=1;i<=wantChar;i++){
            tmpValPaddingResult.append("0");
        }
        hexValPaddingResult.append(tmpValPaddingResult.toString());

*/
/*        for (char ch : tmpValPaddingResult.toString().toCharArray()) {
            hexValPaddingResult.append(String.format("%02X", (int) ch));
        }*//*


        if(!isPaddingRight){
            hexValPaddingResult.append(mainText);
        }

        return hexValPaddingResult.toString();
    }
*/

    public String decimalToAsciiHex(String number) {
        //String decimalString = number;//String.valueOf(number); // Convert number to string
        StringBuilder hexResult = new StringBuilder();
        //e.g: 1 convert to 000001
//        hexResult.append(String.format(Locale.ENGLISH,"%06d", number.length()));
//        hexResult.append("0006");
        hexResult.append(number);

        /*for (char ch : decimalString.toCharArray()) {
            hexResult.append(String.format("%02X", (int) ch));
        }*/
        return hexResult.toString();
    }

    private String decToAscii(String inVal) {
        StringBuilder rtnHexData = new StringBuilder();
        // rtnHexData.append(String.format(Locale.ENGLISH, "%04d", inVal.length()));
        /*for (char ch : inVal.toCharArray()) {
           // rtnHexData.append(String.format(Locale.ENGLISH,"%02X", (int) ch));
        }*/
        return rtnHexData.toString();
    }

 /*   public  String getField63Brac(long amount) {
        //String decimalString = amount;//String.valueOf(number); // Convert number to string
        StringBuilder decResult = new StringBuilder();
        StringBuilder rtnHexData = new StringBuilder();
        rtnHexData.append("0090"); //static length for Brac ISO field8583
        //add amount
        decResult.append(String.format(Locale.ENGLISH,"%012d", amount));

        for(int i=decResult.length();i<90;i++){
            decResult.append("0");
        }

        for (char ch : decResult.toString().toCharArray()) {
            rtnHexData.append(String.format("%02X", (int) ch));
        }

        return rtnHexData.toString();
    }*/


    /**
     * send every record of a merchant.
     */
    private boolean batchUp(Merchant merchant) {
        RecordRepository recordRepository = new RecordRepository();
        int count = recordRepository.getCountByMidTidBracSettle(merchant.getMid(), merchant.getTid());
        LoggerUtils.i(merchant.getType() + " record sum: " + count);
        if (count <= 0) {
            return true;
        }
        //upload all records of this merchant
        int index = 0;
        for (int i = 0; i < count; i++) {
            Record record = recordRepository.findByIndexBracSettle(merchant.getMid(), merchant.getTid(), i);

            LoggerUtils.d("batch up -> getTraceNo:" + record.getTraceNo());
            LoggerUtils.d("batch up -> Field22-: " + record.getField22());
            LoggerUtils.d("batch up -> Field37-getReferNo: " + record.getReferNo());
            LoggerUtils.d("batch up -> Field38-getAuthCode: " + record.getAuthCode());
            LoggerUtils.d("batch up -> Field38-getOrgAuthCode: " + record.getOrigAuthCode());

            updateProgressUi(index);
            for (int j = 0; j < 3; j++) {
                if (record.isBatchUpFlag()) {
                    LoggerUtils.e("batch uploaded.");
                    break;
                }
                DataConverter.recordToPubBean(record, pubBean);
                initPubBean(merchant);
                pubBean.setMessageId("0320");
                pubBean.setProcessCode("000000");
                //pack 8583
                iso8583.initPack();
                try {
                    //brac: 2,3,4,11,42,13,14,22,24,25,37,38,41,42,55,62
                    iso8583.setField(0, pubBean.getMessageId());
                    iso8583.setField(2, pubBean.getCardNo());
                    if(record.getProcessCode()!=null && !record.getProcessCode().isEmpty()){
                        pubBean.setProcessCode(record.getProcessCode());
                    }
                    iso8583.setField(3, pubBean.getProcessCode());
                    iso8583.setField(4, pubBean.getAmountField());
                    iso8583.setField(11, pubBean.getTraceNo());
                    iso8583.setField(12, pubBean.getTime());
                    //MMdd
                    iso8583.setField(13, pubBean.getDate().substring(4));
                    //if (!TextUtils.isEmpty(pubBean.getExpDate())) {
                    iso8583.setField(14, pubBean.getExpDate());
                    //}
                    if (pubBean.getField22() == null) {
                        pubBean.setField22(record.getField22());
                    }
                    iso8583.setField(22, pubBean.getField22());
//                    iso8583.setField(23, pubBean.getCardSn());

                    if (!TextUtils.isEmpty(pubBean.getNii())) {
                        iso8583.setField(24, pubBean.getNii());
                    }
                    //POS Condition Code
                    String posConditionCode = pubBean.getServerCode();
                    if (posConditionCode == null || posConditionCode.isEmpty()) {
                        posConditionCode = "00";
                    }
                    iso8583.setField(25, posConditionCode);

                    if (pubBean.getReferNo() == null) {
                        pubBean.setReferNo(record.getReferNo());
                    }
                    iso8583.setField(37, pubBean.getReferNo());


                    String authCode = pubBean.getOrigAuthCode();
                    if (TextUtils.isEmpty(authCode)) {
                        authCode = pubBean.getAuthCode();
                    }
                    if (TextUtils.isEmpty(authCode)) {
                        authCode = record.getAuthCode();
                    }
                    if (TextUtils.isEmpty(authCode)) {
                        authCode = record.getOrigAuthCode();
                    }
                    pubBean.setAuthCode(authCode);
                    iso8583.setField(38, authCode);
                    iso8583.setField(39, "00");
                    //------------------

                    iso8583.setField(41, pubBean.getTid());
                    iso8583.setField(42, pubBean.getMid());
                    iso8583.setField(49, pubBean.getCurrencyCode());
                    if (!TextUtils.isEmpty(pubBean.getField55())) {
                        iso8583.setField(55, pubBean.getField55());
                    }

//                    iso8583.setField(57, pubBean.getKsn());

                    /*
                    DE_60 = 002230323030303030303131303132313336363634323330
                    [Length]4[Orginal MTI]4[STAN of Orginal transaction]6[RRN of original Transaction]12
                    0022            =       Total Length
                    30323030        =		0200 = Original transaction MTI
                    303030303131	=		000011 = STAN of original transaction
                    303132313336363634323330	=	RRN of original Transaction
*/
                    StringBuilder f60 = getSettleField60Data(record);
                    iso8583.setField(60, f60.toString());

                    iso8583.setField(62, pubBean.getBatchNo());

                    String f63 = PackField.packField63(pubBean.getMid(), pubBean.getTid());
                    String bracField63Ascii = field63Padding(90, true, f63);
                    LoggerUtils.d("newCall Settle batch bracField63Ascii decToAscii: " + bracField63Ascii);
                    iso8583.setField(63, bracField63Ascii);


                    // iso8583.setField(64, Packet8583.getMac(pubBean,iso8583));
//                    String field60Data =  getSettleFiel60Data();


                } catch (Exception e) {
                    LoggerUtils.e("set 8583 field error!", e);
                    pubBean.setMessage(mActivity.getString(R.string.core_comm_pack_error) + e.getMessage());
                    pubBean.setResultCode(ResultCode.FL);
                    ToastUtils.showToast(R.string.core_batch_upload_pack_error);
                    continue;
                }

                //send to the server
                int ret = new Caller.Builder(mActivity, pubBean, iso8583)
                        .withoutPrompts()
                        .checkResp(true)
                        .packComm();
                if (ret == CallerResult.FAIL_NET_CONNECT) {
                    //net  error,exit
                    return false;
                }
                if (ret == CallerResult.OK) {
                    //update batch up flag to true
                    record.setBatchUpFlag(true);
                    recordRepository.update(record);
                    //response code :Success.
                    index++;
                    ToastUtils.showToast(mActivity.getString(R.string.core_batch_upload_success) + index);
                    LoggerUtils.d("batch up success [" + record.getTraceNo() + "]");
                    break;
                } else {
                    //response code :Failed.
                    ToastUtils.showToast(pubBean.getMessage());
                    LoggerUtils.d("batch up failed [" + record.getTraceNo() + "]");
                }
            }// end batch for

            if (!record.isBatchUpFlag()) {
                // record upload failed
                pubBean.setResultCode(ResultCode.FL);
                pubBean.setMessage(R.string.core_batch_upload_failed);
                return false;
            }
        }
        return true;
    }

    @NonNull
    private static StringBuilder getSettleField60Data(Record record) {
        StringBuilder f60 = new StringBuilder();
        String MTI = "0000";
        if (record.getTransType().equalsIgnoreCase(TransType.TRANS_SALE)
                || record.getTransType().equalsIgnoreCase(TransType.TRANS_VOID_SALE)
                || record.getTransType().equalsIgnoreCase(TransType.TRANS_INSTALLMENT)
                || record.getTransType().equalsIgnoreCase(TransType.TRANS_VOID_INSTALLMENT)
                || record.getTransType().equalsIgnoreCase(TransType.TRANS_VOID_PRE_AUTH)
        ) {
            MTI = "0200";
        } else if (record.getTransType().equalsIgnoreCase(TransType.TRANS_PRE_AUTH)) {
            MTI = "0100";
        } else if (
                record.getTransType().equalsIgnoreCase(TransType.TRANS_AUTH_COMPLETE)
                || record.getTransType().equalsIgnoreCase(TransType.TRANS_TIP_SALE)
                || record.getTransType().equalsIgnoreCase(TransType.TRANS_VOID_TIP_ADJUST)
        ) {
            MTI = "0220";
        }

        f60.append(MTI).append(record.getTraceNo()).append(record.getReferNo());
        return f60;
    }

    private ProgressDialog progressDialog;

    private void updateProgressUi(int index) {
        mActivity.runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.getContent().setText(mActivity.getString(R.string.core_batch_uploading_fomat, index + 1));
                progressDialog.show();
            } else {
                progressDialog = new ProgressDialog.Builder(mActivity)
                        .setContent(mActivity.getString(R.string.core_batch_uploading_fomat, index + 1))
                        .show();
            }
        });
    }

    private void closeProgressUi() {
        mActivity.runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }
}
