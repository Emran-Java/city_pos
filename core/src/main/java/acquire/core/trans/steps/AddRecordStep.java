package acquire.core.trans.steps;


import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.UUID;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.R;
import acquire.core.constant.CoreContent;
import acquire.core.constant.OnUsBinMap;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransStatus;
import acquire.core.constant.TransType;
import acquire.core.model.CardBinModel;
import acquire.core.tools.DataConverter;
import acquire.core.trans.BaseStep;
import acquire.database.model.Record;
import acquire.database.model.ReversalData;
import acquire.database.repository.PreStepVoidAbleRecordData;
import acquire.database.repository.RecordRepository;
import acquire.database.repository.ReversalDataRepository;

/**
 * The {@link BaseStep} that add the record to database
 *
 * @author Janson
 * @date 2018/11/23 10:42
 */
public class AddRecordStep extends BaseStep {
    /**
     * The status of the original record to be updated .
     *
     * @see TransStatus
     */
    private int newStatus = -1;

    /**
     * @param newStatus Original record status to this new status
     * @see TransStatus
     */
    public AddRecordStep(int newStatus) {
        this.newStatus = newStatus;
    }

    public AddRecordStep() {
    }

    @Override
    public void intercept(Callback callback) {
        RecordRepository recordRepository = new RecordRepository();
        if (TextUtils.isEmpty(pubBean.getRemarks())) {
            //receipt remarks
            pubBean.setRemarks(ParamsUtils.getString(ParamsConst.PARAMS_KEY_PRINT_REMARKS));
        }
        Record origRecord = getOrigRecord();
        // 1. save the transaction
        Record record = new Record();
        DataConverter.pubBeanToRecord(pubBean, record);
        String eUuid = UUID.randomUUID().toString();
        record.setUuid(eUuid);

        //Brac Add
        if (record.getTransType().equalsIgnoreCase(TransType.TRANS_SALE)
                || record.getTransType().equalsIgnoreCase(TransType.TRANS_PRE_AUTH)
                || record.getTransType().equalsIgnoreCase(TransType.TRANS_INSTALLMENT)
                || record.getTransType().equalsIgnoreCase(TransType.TRANS_TIP_SALE)
        ) {
            String ben = record.getCardNo().substring(0, 6);
            String cardTitle = "";
            try{
                cardTitle = OnUsBinMap.REPORT_CARD_ONUS_MAP.get(ben).getCardTitle();
            }catch (Exception ex){
                LoggerUtils.e("newCall AddRecordStep exception: :"+ex.getMessage());
            }

            record.setCardTitle(cardTitle);
        }

/*        if (PreStepVoidAbleRecordData.oldTransactionType != null)
            switch (PreStepVoidAbleRecordData.oldTransactionType) {
                case TransType.TRANS_SALE: {
                    record.setDisplayTitle(CoreContent.REPORT_DISPLAY_TEXT_SALE);
                    break;
                }
                case TransType.TRANS_VOID_SALE: {
                    record.setDisplayTitle(CoreContent.REPORT_DISPLAY_TEXT_VOID);
                    break;
                }
                case TransType.TRANS_PRE_AUTH: {
                    record.setDisplayTitle(CoreContent.REPORT_DISPLAY_TEXT_PRE_AUTH);
                    break;
                }
                case TransType.TRANS_VOID_PRE_AUTH: {
                    record.setDisplayTitle(CoreContent.REPORT_DISPLAY_TEXT_VOID_PRE_AUTH);
                    break;
                }
                case TransType.TRANS_AUTH_COMPLETE: {
                    record.setDisplayTitle(CoreContent.REPORT_DISPLAY_TEXT_PRE_AUTH_COMPLETE);
                    break;
                }
                case TransType.TRANS_VOID_AUTH_COMPLETE: {
                    record.setDisplayTitle(CoreContent.REPORT_DISPLAY_TEXT_VOID_PRE_AUTH_COMPLETE);
                    break;
                }
            }*/


//        if (record.getTransType().equalsIgnoreCase(TransType.TRANS_SALE)
//                || record.getTransType().equalsIgnoreCase(TransType.TRANS_PRE_AUTH)) {
        if (record.getCardNo() != null && !record.getCardNo().isEmpty()) {
            String ben = record.getCardNo().substring(0, 6);
            CardBinModel cardBinModel = OnUsBinMap.REPORT_CARD_ONUS_MAP.get(ben);
            if (cardBinModel != null) {
                String cardTitle = cardBinModel.getCardTitle();
                record.setCardTitle(cardTitle);
                if (cardBinModel.getStartBin().length() > 5 && cardBinModel.getEndBin().length() > 5) {
                    record.setOnUs(true);
                }
            } else {
                LoggerUtils.e("newCall CardBinModel is null");
            }
        }
//        }

  /*      if(record.getTransType().equalsIgnoreCase(TransType.TRANS_SALE)){
            record.setDisplayTitle(CoreContent.REPORT_DISPLAY_TEXT_SALE);
        }
        else if(record.getTransType().equalsIgnoreCase(TransType.TRANS_VOID_SALE)){
            record.setDisplayTitle(CoreContent.REPORT_DISPLAY_TEXT_VOID);
        }
        else if(record.getTransType().equalsIgnoreCase(TransType.TRANS_VOID_AUTH_COMPLETE)){
            record.setDisplayTitle(CoreContent.REPORT_DISPLAY_TEXT_VOID_PRE_AUTH_COMPLETE);
        }
        else if(record.getTransType().equalsIgnoreCase(TransType.TRANS_AUTH_COMPLETE)){
            record.setDisplayTitle(CoreContent.REPORT_DISPLAY_TEXT_PRE_AUTH_COMPLETE);
        }
        else if(record.getTransType().equalsIgnoreCase(TransType.TRANS_VOID_PRE_AUTH)){
            record.setDisplayTitle(CoreContent.REPORT_DISPLAY_TEXT_VOID_PRE_AUTH);
        }
        else if(record.getTransType().equalsIgnoreCase(TransType.TRANS_PRE_AUTH)){
            record.setDisplayTitle(CoreContent.REPORT_DISPLAY_TEXT_PRE_AUTH);
        }*/

        //--------------

        boolean result = recordRepository.add(record);
        if (!result) {
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_add_record_fail);
            callback.onResult(false);
            return;
        }
        //2. delete reversal data that is pre-saved
        ReversalDataRepository reversalDataRepository = new ReversalDataRepository();
        ReversalData reversalData = reversalDataRepository.getReverseRecord();
        if (reversalData != null) {
            reversalDataRepository.deleteAllReversalData();
        }
        //3. set printed record data
        setRecord(record);
        //4. update original record status
        if (origRecord != null && newStatus >= 0) {
            origRecord.setStatus(newStatus);
            recordRepository.update(origRecord);
        }
        callback.onResult(true);
    }

}
