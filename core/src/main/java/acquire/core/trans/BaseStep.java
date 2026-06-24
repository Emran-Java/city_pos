package acquire.core.trans;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Date;

import acquire.base.activity.BaseActivity;
import acquire.base.chain.Interceptor;
import acquire.base.utils.DateUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.iso8583.ISO8583;
import acquire.base.utils.thread.Locker;
import acquire.core.TransActivity;
import acquire.core.bean.PubBean;
import acquire.core.bean.StepBean;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.TransTag;
import acquire.core.constant.TransType;
import acquire.core.tools.MultiMerchantUtils;
import acquire.core.tools.PinpadHelper;
import acquire.database.model.Merchant;
import acquire.database.model.Record;
import acquire.database.repository.ReversalDataRepository;

/**
 * The base step implements {@link Interceptor}
 *
 * @author Janson
 * @date 2019/12/24 14:57
 */
public abstract class BaseStep implements Interceptor<StepBean> {
    protected PubBean pubBean;
    protected BaseActivity mActivity;
    protected final ISO8583 iso8583 = ISO8583.getDefault();
    protected StepBean stepBean;

    @Override
    public void init(@NonNull StepBean stepBean) {
        this.pubBean = stepBean.getPubBean();
        this.mActivity = stepBean.getActivity();
        this.stepBean = stepBean;
    }

    /**
     * Set current record
     */
    protected void setRecord(Record record) {
        stepBean.setRecord(record);
    }

    /**
     * Get original record
     */
    protected void setOrigRecord(Record record) {
        stepBean.setOrigRecord(record);
    }

    protected Record getRecord() {
        return stepBean.getRecord();
    }

    protected Record getOrigRecord() {
        return stepBean.getOrigRecord();
    }

    /**
     * check reversal
     */
    protected boolean doReversal() {
        if (new ReversalDataRepository().getReverseRecord() == null) {
            //no reversal data
            return true;
        }
        //start a reversal transaction
        Intent intent = new Intent(mActivity, TransActivity.class);
        intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_REVERSAL);
        Locker<Boolean> locker = new Locker<>(false);
        mActivity.mSupportDelegate.startActivityForResult(intent, null, result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                locker.setResult(true);
                locker.wakeUp();
            } else {
                locker.setResult(false);
                locker.wakeUp();
            }
        });
        locker.waiting();
        return locker.getResult();
    }


    /**
     * Dynamic Currency Conversion (DCC) . To be implemented.
     * <pre>
     *     e.g.
     *  class PackSaleStep extends BaseStep {
     *     public void intercept(Callback callback) {
     *         ...
     *         initPubBean();
     *         if(!dcc()){
     *             pubBean.setResultCode(ResultCode.FL);
     *             pubBean.setMessage("DCC failed.");
     *            callback.onResult(false);
     *            return;
     *         }
     *         ...
     *     }
     * }
     * </pre>
     */
    protected boolean dcc() {
        //If you need to do DCC, implement it here. Then call in the transaction packXXstep
//        pubBean.setConversionRate("3.75");
//        pubBean.setMarkupRate("1.5%");
//        pubBean.setForeignAmount(20);
//        pubBean.setForeignCurrency("156");
        return true;
    }

    /**
     * Init PubBean
     */
    public void initPubBean() {
        initPubBean(MultiMerchantUtils.getMerchant(pubBean));
    }

    /**
     * Init PubBean of a merchant
     */
    public void initPubBean(Merchant merchant) {

        Log.d("SendRequest", "Merchant: - " + merchant);

        pubBean.setMerchantName(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME));
        pubBean.setTraceNo(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_TRACE_NO));
        String strDate = DateUtils.formatTime(new Date(), DateUtils.YYYYMMDD);
        String strTime = DateUtils.formatTime(new Date(), DateUtils.HHMMSS);
        pubBean.setDate(strDate);
        pubBean.setTime(strTime);
        pubBean.setTid(merchant.getTid());
        pubBean.setMid(merchant.getMid());
        pubBean.setBatchNo(merchant.getBatchNo());
        pubBean.setNii(merchant.getNii());

        int kekIndex = merchant.getMasterKeyIndex();
        if (pubBean.getTransType().equals(TransType.TRANS_INSTALLMENT)) {
            kekIndex = 1;
        }
        PinpadHelper pinpadHelper = new PinpadHelper(kekIndex, merchant.getAlgorithm());
        if (pubBean.isRequireKsnInc()) {
            pubBean.setRequireKsnInc(false);
            pinpadHelper.increaseKsn();
        }

        pubBean.setKsn(pinpadHelper.getKsn());//Key Serial Number
    }

    public String field63Padding(int totalL, boolean isPaddingRight, String mainText) {
        int wantChar = totalL - (mainText.length() / 1);
        StringBuilder hexValPaddingResult = new StringBuilder();
        //hexValPaddingResult.append("0090");

        if (isPaddingRight) {
            hexValPaddingResult.append(mainText);
        }

        StringBuilder tmpValPaddingResult = new StringBuilder();

        for (int i = 1; i <= wantChar; i++) {
            tmpValPaddingResult.append("0");
        }
        hexValPaddingResult.append(tmpValPaddingResult.toString());

/*        for (char ch : tmpValPaddingResult.toString().toCharArray()) {
            hexValPaddingResult.append(String.format("%02X", (int) ch));
        }*/

        if (!isPaddingRight) {
            hexValPaddingResult.append(mainText);
        }

        return hexValPaddingResult.toString();
    }

    public String fieldDecToHax(String inval) {
        StringBuilder hexValPaddingResult = new StringBuilder();
        for (char ch : inval.toCharArray()) {
            hexValPaddingResult.append(String.format("%02X", (int) ch));
        }
        return hexValPaddingResult.toString();
    }
}
