package acquire.core.tools;

import android.text.TextUtils;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.SensitiveGuard;
import acquire.base.utils.TlvUtils;
import acquire.base.utils.emv.EmvTag;
import acquire.core.R;
import acquire.core.bean.PubBean;
import acquire.core.constant.CardSchemes;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.sdk.emv.BEmvProcessor;
import acquire.sdk.emv.BExtEmvProcessor;
import acquire.sdk.emv.IEmvProcessor;
import acquire.sdk.emv.bean.EmvFetchBean;
import acquire.sdk.emv.bean.EmvLaunchParam;
import acquire.sdk.emv.constant.EntryMode;
import acquire.sdk.emv.listener.EmvListener;
import acquire.sdk.emv.listener.EmvSecondGacListener;
import acquire.sdk.nonbankcard.BCardReader;

/**
 * Emv utils
 *
 * @author Janson
 * @date 2018/11/23 9:57
 */
public class EmvHelper {
    /**
     * Emv processor
     */
    private final IEmvProcessor emvProcessor;

    public EmvHelper() {
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL)) {
            LoggerUtils.d("external card reader");
            emvProcessor = new BExtEmvProcessor();
        } else {
            LoggerUtils.d("built-in card reader");
            emvProcessor = new BEmvProcessor();
        }
    }

    public boolean isExternal(){
        return emvProcessor instanceof BExtEmvProcessor;
    }
    /**
     * start to read card
     */
    public void readCard(EmvLaunchParam launchParam, EmvListener emvListener) {
        emvProcessor.readCard(launchParam, emvListener);
    }

    /**
     * second gac
     *
     * @param requestOnline    When the server is requested successfully in this transaction, it is true
     * @param gacData          Gac request tlv data.(Valid tags: 0x89,0x71,0x72,0x91,0x8A)
     * @param completeListener Result listener
     */
    public void secondGac(boolean requestOnline, byte[] gacData, EmvSecondGacListener completeListener) {
        emvProcessor.secondGac(requestOnline, gacData, completeListener);
    }


    /**
     * Get emv data(HEX) by tag.
     */
    public String getEmvDataStr(int tag) {
        byte[] data = emvProcessor.getData(tag);
        String result = BytesUtils.bcdToString(data);
        LoggerUtils.d(String.format(Locale.getDefault(),"0x%x: %s" ,tag, result));
        return result;
    }

    /**
     * Get emv data by tag.
     */
    public byte[] getEmvData(int tag) {
        String data = getEmvDataStr(tag);
        return BytesUtils.str2bcd(data, false);
    }


    /**
     * Returns 0: No card; 1: detect a contact card ; 2: detect a contactless card.
     */
    public int cardExist() {
        if (isExternal()){
            return 0;
        }
        BCardReader cardReader = new BCardReader();
        if (cardReader.contactCardExist()) {
            return 1;
        }
        if (cardReader.contactlessCardExist()) {
            return 2;
        }
        return 0;
    }

    /**
     * Get card scheme by aid
     * @see CardSchemes
     */
    private String fetchCardSchemeByAid() {
        String aid = getEmvDataStr(EmvTag.TAG_4F_IC_AID);
        if (aid == null || aid.length() < 10) {
            return null;
        }
        String aidHead = aid.substring(0, 10);
        switch (aidHead) {
            case "A000000003":
                return CardSchemes.VISA;
            case "A000000004":
                return CardSchemes.MASTER_CARD;
            case "A000000025":
                return CardSchemes.AMEX;
            case "A000000065":
                return CardSchemes.JCB;
            case "A000000615":
                return CardSchemes.MCCS;
            case "A000000152":
                return CardSchemes.DINERS;
            case "A000000324":
                return CardSchemes.DISCOVER;
            case "A000000524":
                return CardSchemes.RUPAY;
            case "A000000277":
                return CardSchemes.INTERAC;
            case "A000000333":
                return CardSchemes.CUP;
            default:
                return null;
        }
    }

    /**
     * Get card  scheme
     * @param entryMode card entry mode. see {@link EntryMode}.
     * @param pan card number
     * @return card scheme.
     * @see CardSchemes
     */
    public String getCardScheme(int entryMode, String pan) {
        if (entryMode == EntryMode.INSERT || entryMode == EntryMode.TAP) {
            String carScheme = fetchCardSchemeByAid();
            if (!TextUtils.isEmpty(carScheme)) {
                LoggerUtils.d("Get card scheme by AID: " + carScheme);
                return carScheme;
            }
        }
        String cardScheme = CardRangeProvider.getCardScheme(pan);
        LoggerUtils.d("Get card scheme by BIN: " + cardScheme);
        return cardScheme;
    }

    /**
     * Pack field55
     */
    private String packField55() {
        List<Integer> tags = new ArrayList<>();
        tags.add(EmvTag.TAG_9F27_IC_CID);
        tags.add(EmvTag.TAG_9F10_IC_ISSAPPDATA);
        tags.add(EmvTag.TAG_9F37_TM_UNPNUM);
        tags.add(EmvTag.TAG_9F36_IC_ATC);
        tags.add(EmvTag.TAG_95_TM_TVR);
        tags.add(EmvTag.TAG_9A_TM_TRANSDATE);
        tags.add(EmvTag.TAG_9C_TM_TRANSTYPE);
        tags.add(EmvTag.TAG_9F02_TM_AUTHAMNTN);
        tags.add(EmvTag.TAG_5F2A_CURRENCY_CODE);
        tags.add(EmvTag.TAG_82_IC_AIP);
        tags.add(EmvTag.TAG_9F1A_COUNTRY_CODE);
        tags.add(EmvTag.TAG_9F03_TM_OTHERAMNTN);
        tags.add(EmvTag.TAG_9F33_TM_CAP);
        tags.add(EmvTag.TAG_9F34_TM_CVMRESULT);
        tags.add(EmvTag.TAG_9F35_TM_TERMTYPE);
        tags.add(EmvTag.TAG_9F1E_TM_IFDSN);
        tags.add(EmvTag.TAG_84_IC_DFNAME);
        tags.add(EmvTag.TAG_9F09_TM_APPVERNO);
        tags.add(EmvTag.TAG_9F41_TM_TRSEQCNTR);
        tags.add(EmvTag.TAG_9F63_IC_PRODUCTID);
        tags.add(EmvTag.TAG_9F26_IC_AC);
        //Newly added tags for Brac
        tags.add(EmvTag.TAG_5F34_IC_PANSN);
        tags.add(EmvTag.TAG_9F06_TM_AID);
        tags.add(EmvTag.TAG_9F12_IC_APPNAME);
        tags.add(EmvTag.TAG_9F21_TM_TRANSTIME);
        tags.add(EmvTag.TAG_9F45_IC_DTAUTHCODE);
        tags.add(EmvTag.TAG_9F6E_FF_INDICATOR);
        tags.add(EmvTag.TAG_5F20_IC_HOLDERNAME);
        //------------------------------

        //Brac PayFlex
        tags.add(EmvTag.TAG_9B_TM_TSI);
//        tags.add(EmvTag.TAG_9F34_TM_CVMRESULT); // already added|
        //tags.add(EmvTag.TAG_9F10_IC_ISSAPPDATA); // already added|
        //tags.add(EmvTag.TAG_9F26_IC_AC); // already added|
        //tags.add(EmvTag.TAG_9F26_IC_AC); // already added|
        //tags.add(EmvTag.TAG_9F36_IC_ATC); // already added|
        //------------------------------

        byte[] result = emvProcessor.getListData(tags, false);
        LoggerUtils.d("pack field55 tag:"+tags);
        return BytesUtils.bcdToString(result);
    }

    /**
     * Pack reversal field55
     */
    public String packReversalField55(boolean isPack9F36) {
        List<Integer> tags = new ArrayList<>();
        tags.add(EmvTag.TAG_95_TM_TVR);
        tags.add(EmvTag.TAG_9F1E_TM_IFDSN);
        tags.add(EmvTag.TAG_9F10_IC_ISSAPPDATA);
        if (isPack9F36) {
            tags.add(EmvTag.TAG_9F36_IC_ATC);
        }
        byte[] result = emvProcessor.getListData(tags, false);
        return BytesUtils.bcdToString(result);
    }

    public String packEmvPrintData() {
        List<Integer> tags = new ArrayList<>();
        tags.add(EmvTag.TAG_9F12_IC_APPNAME);
        tags.add(EmvTag.TAG_50_IC_APPLABEL);
        tags.add(EmvTag.TAG_4F_IC_AID);
        tags.add(EmvTag.TAG_95_TM_TVR);
        tags.add(EmvTag.TAG_9B_TM_TSI);
        byte[] result = emvProcessor.getListData(tags, false);
        return BytesUtils.bcdToString(result);
    }

    /**
     * pack gac data for secondary authorization
     */
    public byte[] packGac(boolean success, String responseCode, String field55) {
        TlvUtils.PackTlv gacTlv = TlvUtils.newPackTlv();
        SparseArray<byte[]> emvTlvs = TlvUtils.getTlvList(BytesUtils.hexToBytes(field55));
        boolean has8A = false;
        if (emvTlvs != null){
            for (int i = 0; i < emvTlvs.size(); i++) {
                int tag = emvTlvs.keyAt(i);
                byte[] value = emvTlvs.get(tag);
                switch (tag) {
                    case EmvTag.TAG_8A_TM_ARC:
                        has8A = true;
                    case EmvTag.TAG_89_TM_AUTHCODE:
                    case EmvTag.TAG_71_ISSSCR_TEMPLATE_1:
                    case EmvTag.TAG_72_ISSSCR_TEMPLATE_2:
                    case EmvTag.TAG_91_TM_ISSAUTHDT:
                        gacTlv.append(tag, value);
                        break;
                    default:
                        break;
                }

            }
        }
        if (!has8A){
            if (success) {
                //success
                gacTlv.append(EmvTag.TAG_8A_TM_ARC, "00".getBytes());
            } else {
                //failed
                if (TextUtils.isEmpty(responseCode) || ResultCode.OK.equals(responseCode)) {
                    //convert code to FL
                    gacTlv.append(EmvTag.TAG_8A_TM_ARC, ResultCode.FL.getBytes());
                } else {
                    gacTlv.append(EmvTag.TAG_8A_TM_ARC, responseCode.getBytes());
                }
            }
        }
        return gacTlv.pack();
    }
    /**
     * Deal emv data
     */
    public boolean dealEmvData(@NonNull PubBean pubBean) {
        EmvFetchBean emvFetchBean = getEmvFetchBean();
        String value;
        String trac1=null;
        String field55=null;
        //entry mode
        if (pubBean.getEntryMode() == 0) {
            pubBean.setEntryMode(emvFetchBean.getUserEntryMode());
        }
        //track1
        if (TextUtils.isEmpty(pubBean.getCardHolderName())) {
            trac1 = emvFetchBean.getTrack1();
            LoggerUtils.d("get card track1" );
        }
        //track2
        if (TextUtils.isEmpty(pubBean.getTrack2())) {
            value = emvFetchBean.getTrack2();
            LoggerUtils.d("get card track2" );
            pubBean.setTrack2(value);
        }
        //track3
        if (TextUtils.isEmpty(pubBean.getTrack3())) {
            value = emvFetchBean.getTrack3();
            LoggerUtils.d("get card track3");
            pubBean.setTrack3(value);
        }
        //card num
        if (TextUtils.isEmpty(pubBean.getCardNo())) {
            value = emvFetchBean.getPan();
            if (TextUtils.isEmpty(value)) {
                LoggerUtils.e("Failed to get card number");
                pubBean.setResultCode(ResultCode.FL);
                pubBean.setMessage(R.string.core_emv_fetch_pan_fail);
                return false;
            }
            LoggerUtils.d("get card number");
            pubBean.setCardNo(value);
        }
        //expiration date
        value = emvFetchBean.getExpDate();
        if (!TextUtils.isEmpty(value)) {
            LoggerUtils.d("get card expiration date" );
            pubBean.setExpDate(value.substring(0, 4));
        }
        //required signature.
        pubBean.setFreeSign(emvFetchBean.freeSign());
        LoggerUtils.d("Card require free sign: " + pubBean.isFreeSign());
        //card serial
        value = getEmvDataStr(EmvTag.TAG_5F34_IC_PANSN);
        if (!TextUtils.isEmpty(value)) {
            pubBean.setCardSn("0" + value);
        } else {
            pubBean.setCardSn("000");
        }
        //card scheme
        value = getCardScheme(pubBean.getEntryMode(), pubBean.getCardNo());
        LoggerUtils.d("Card scheme: " + value);
        pubBean.setCardScheme(value);
        //field 55
        if (pubBean.getEntryMode() == EntryMode.INSERT || pubBean.getEntryMode() == EntryMode.TAP) {
            value = packField55();
            if (TextUtils.isEmpty(value)) {
                pubBean.setResultCode(ResultCode.FL);
                pubBean.setMessage(R.string.core_emv_fetch_field55_fail);
                return false;
            } else {
                field55 = value;
                pubBean.setField55(value);
            }
        }

        pubBean.setCardHolderName(CardInfoUtility.getCardholderName(field55, trac1));

                //EMV CARD tag
        pubBean.setEmvPrintData(packEmvPrintData());
        return true;
    }

    /**
     * Fetch EMV processed data. Unlike {@link #getEmvData(int)}, it doesn't need to process the EMV kernel data
     */
    public EmvFetchBean getEmvFetchBean() {
        return new EmvFetchBean(emvProcessor);
    }

    /**
     * Returns true if the card is a cipher card.
     *
     * @param serviceCode card service code.
     */
    public boolean isIcCard(String serviceCode) {
        return !TextUtils.isEmpty(serviceCode)
                && (serviceCode.startsWith("2") || serviceCode.startsWith("6"));
    }

    /**
     * cancel emv transaction performation
     */
    public void cancelEmv() {
        emvProcessor.cancelEmv();
    }

    /**
     * terminate card reader
     */
    public void terminateTransaction() {
        emvProcessor.terminateTransaction();
    }



}
