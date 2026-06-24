package acquire.core.fragment.print;

import static acquire.base.utils.FormatUtils.formatAmount;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.SparseArray;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import acquire.base.BaseApplication;
import acquire.base.utils.BytesUtils;
import acquire.base.utils.DateUtils;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.TlvUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.base.utils.emv.EmvTag;
import acquire.base.utils.qrcode.QRCodeUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.bean.SettleReceiptBean;
import acquire.core.constant.FileConst;
import acquire.core.constant.OnUsBinMap;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.PrintSize;
import acquire.core.constant.ReportConstant;
import acquire.core.constant.SettleAttr;
import acquire.core.constant.TransType;
import acquire.core.esc.EscPrinter;

import acquire.core.model.CardSchemeReportModel;
import acquire.core.model.DeviceItem;
import acquire.core.model.GroupByTranType;
import acquire.core.model.PayFlexField63ResponseModel;
import acquire.core.model.SchemeGroup;
import acquire.core.tools.CardInfoUtility;
import acquire.core.tools.FieldDataParseUtility;
import acquire.core.tools.TransUtils;
import acquire.database.bean.TransactionSummary;
import acquire.database.model.Merchant;
import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;
import acquire.sdk.device.BDevice;
import acquire.sdk.emv.constant.EntryMode;
import acquire.sdk.printer.BPrinter;
import acquire.sdk.printer.BitmapDraw;
import acquire.sdk.printer.IPrinter;

/**
 * Print ViewModel
 *
 * @author Janson
 * @date 2023/4/28 10:05
 */
public class PrintViewModel extends ViewModel {

    public final static String LINE_DOTED_SEPARATOR = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -";
    public final static String STER_DOTED_SEPARATOR = "* * * * * * * * * * * * * * * * * * * * * * * *";
    public final static int STATUS_OUT_OF_PAPER = -2, STATUS_ERROR = -1, STATUS_READY = 0, STATUS_SUCCESS = 1, STATUS_NEXT_RECEIPT = 2;

    public final static int RECEIPT_OWNER_MERCHANT = 0, RECEIPT_OWNER_CUSTOMER = 1, RECEIPT_OWNER_BANK = 2;


    //    private int index = 0;
    private IPrinter printer = new BPrinter();
    private boolean supportCut;

    private final MutableLiveData<Bitmap> receipt = new MutableLiveData<>();
    private final MutableLiveData<PrtStatus> status = new MutableLiveData<>();

    private final MutableLiveData<Integer> prompt = new MutableLiveData<>();

    public MutableLiveData<Bitmap> getReceipt() {
        return receipt;
    }

    public MutableLiveData<PrtStatus> getStatus() {
        return status;
    }

    public MutableLiveData<Integer> getPrompt() {
        return prompt;
    }

    public void init() {
        supportCut = BDevice.isCpos();
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
            int mode = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL_CONNECT_MODE);
            int baudRate = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL_SERIAL_BAUDRATE);
            printer = new EscPrinter(mode, baudRate);
            supportCut = true;
        }
    }

/*    private void showPinSubmitSheet(String transType) {
        boolean isNumberShuffle = false;
        NumberPadBottomSheet bottomSheetFragment =
                NumberPadBottomSheet.Companion.newInstance(new NumberPadBottomSheet.ItemClickListener() {
                    @Override
                    public void onBottomSheetItemClick(@Nullable String amountValue, boolean isTakeAction) {

                        if(amountValue != null && amountValue.isEmpty()) amountValue="0.00";
                        binding.tvAmount.setText(amountValue);
                        if(isTakeAction){
                            Intent intent = new Intent(mActivity, TransActivity.class);
                            intent.putExtra(TransTag.TRANS_TYPE, transType);
                            intent.putExtra(TransTag.AMOUNT, amountValue);
                            ActivityCompat.startActivity(mActivity, intent, null);
                            binding.tvAmount.setText("0.00");
                        }
                    }
                }, isNumberShuffle, true);

        bottomSheetFragment.setCancelable(false);
        bottomSheetFragment.show(getChildFragmentManager(), bottomSheetFragment.getTag());
    }*/

    /**
     * print receipt. Such as purchase ticket.
     */
    public void getBitmapBracReprint(Record record, boolean isReprint, int receiptOwner) {
        status.postValue(new PrtStatus(STATUS_READY));
        switch (receiptOwner) {
            case RECEIPT_OWNER_MERCHANT:
                prompt.postValue(R.string.core_print_progress_merchant_copy);
                break;
            case RECEIPT_OWNER_CUSTOMER:
                prompt.postValue(R.string.core_print_progress_customer_copy);
                break;
            case RECEIPT_OWNER_BANK:
            default:
                prompt.postValue(R.string.core_print_progress_bank_copy);
                break;
        }

        Bitmap bitmap = null;
        if (Objects.equals(record.getTransType(), TransType.TRANS_TEST_TRX)) {
            bitmap = getBracTestTranReceipt(record, isReprint, receiptOwner);
        } else {
            bitmap = getBracReceipt(record, isReprint, receiptOwner);
        }

        receipt.postValue(bitmap);

    }
    /**
     * print receipt. Such as purchase ticket.
     */
    public void bracReprintReceipt(Bitmap bitmap) {

        printer.print(bitmap, true, new IPrinter.PrintCallback() {
            @Override
            public void onFinish() {
                //finish
                if (supportCut) {
                    printer.cutPaper();
                }
                status.postValue(new PrtStatus(STATUS_SUCCESS));
//                index++;
//                if (index == total) {
//                    status.postValue(new PrtStatus(STATUS_SUCCESS));
//                } else {
//                    if (index == 1) {
//                        status.postValue(new PrtStatus(STATUS_NEXT_RECEIPT, getString(R.string.core_print_dialog_title_customer)));
//                    } else {
//                        status.postValue(new PrtStatus(STATUS_NEXT_RECEIPT, getString(R.string.core_print_dialog_title_bank)));
//                    }
//                }
            }

            @Override
            public void onError(String message) {
                status.postValue(new PrtStatus(STATUS_ERROR, message));
            }

            @Override
            public void onOutOfPaper() {
                status.postValue(new PrtStatus(STATUS_OUT_OF_PAPER));
            }
        });
    }

    /**
     * print receipt. Such as purchase ticket.
     */
    public void printReceipt(Record record, boolean isReprint, int receiptOwner) {
        status.postValue(new PrtStatus(STATUS_READY));
        switch (receiptOwner) {
            case RECEIPT_OWNER_MERCHANT:
                prompt.postValue(R.string.core_print_progress_merchant_copy);
                break;
            case RECEIPT_OWNER_CUSTOMER:
                prompt.postValue(R.string.core_print_progress_customer_copy);
                break;
            case RECEIPT_OWNER_BANK:
            default:
                prompt.postValue(R.string.core_print_progress_bank_copy);
                break;
        }

        Bitmap bitmap = null;
        if (Objects.equals(record.getTransType(), TransType.TRANS_TEST_TRX)) {
            bitmap = getBracTestTranReceipt(record, isReprint, receiptOwner);
        } else {
            bitmap = getBracReceipt(record, isReprint, receiptOwner);
        }

        receipt.postValue(bitmap);


        printer.print(bitmap, true, new IPrinter.PrintCallback() {
            @Override
            public void onFinish() {
                //finish
                if (supportCut) {
                    printer.cutPaper();
                }
                status.postValue(new PrtStatus(STATUS_SUCCESS));
//                index++;
//                if (index == total) {
//                    status.postValue(new PrtStatus(STATUS_SUCCESS));
//                } else {
//                    if (index == 1) {
//                        status.postValue(new PrtStatus(STATUS_NEXT_RECEIPT, getString(R.string.core_print_dialog_title_customer)));
//                    } else {
//                        status.postValue(new PrtStatus(STATUS_NEXT_RECEIPT, getString(R.string.core_print_dialog_title_bank)));
//                    }
//                }
            }

            @Override
            public void onError(String message) {
                status.postValue(new PrtStatus(STATUS_ERROR, message));
            }

            @Override
            public void onOutOfPaper() {
                status.postValue(new PrtStatus(STATUS_OUT_OF_PAPER));
            }
        });
    }

    /**
     * generate receipt bitmap
     */
    public static @NonNull Bitmap getReceipt(Record record, boolean isReprint, @IntRange(from = 0) int receiptOwner) {
        BitmapDraw bitmapDraw = new BitmapDraw();
        try {
            bitmapDraw.image(BitmapFactory.decodeStream(BaseApplication.getAppContext().getAssets().open(FileConst.LOGO_IMG)));
        } catch (IOException e) {
            LoggerUtils.e("decodeStream " + FileConst.LOGO_IMG + " failed!", e);
        }
        bitmapDraw.text(getString(R.string.core_receipt_merchant_id_title), record.getMid(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_terminal_id_title), record.getTid(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_date_time_title), DateUtils.formatTime(record.getDate() + record.getTime()), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_batch_title), record.getBatchNo(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_trace_title), record.getTraceNo(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_refnum_title), record.getReferNo(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_authcode_title), record.getAuthCode(), PrintSize.NORMAL, false);
        if (!TextUtils.isEmpty(record.getOrigTraceNo())) {
            bitmapDraw.text(getString(R.string.core_receipt_orig_trace_title), record.getOrigTraceNo(), PrintSize.NORMAL, false);
        }
        if (!TextUtils.isEmpty(record.getOrigAuthCode())) {
            bitmapDraw.text(getString(R.string.core_receipt_orig_authcode_title), record.getOrigAuthCode(), PrintSize.NORMAL, false);
        }

        if (!TextUtils.isEmpty(record.getBizOrderNo())) {
            Bitmap bitmap = QRCodeUtils.create2dCode(record.getBizOrderNo());
            if (bitmap != null) {
                bitmapDraw.image(bitmap);
                bitmapDraw.text(record.getBizOrderNo(), PrintSize.NORMAL, false, Paint.Align.CENTER);
            }
        }
        bitmapDraw.text(TransUtils.getName(record.getTransType()), PrintSize.TRAN_TYPE, true, Paint.Align.CENTER);
        bitmapDraw.text(record.getCardScheme(), PrintSize.NORMAL, false, Paint.Align.CENTER);
        bitmapDraw.text(EntryMode.getDescription(record.getEntryMode()), PrintSize.NORMAL, false, Paint.Align.CENTER);
        bitmapDraw.text(FormatUtils.maskCardNo(record.getCardNo()), PrintSize.NORMAL, false, Paint.Align.CENTER);
        String currency = CurrencyUtils.getCurrencySymbol(record.getCurrencyCode());

        if (record.getCashAmount() != 0) {
            //Cash back
            bitmapDraw.text(getString(R.string.core_receipt_purchase_amount), currency + formatAmount(record.getBaseAmount()), PrintSize.NORMAL, true);
            bitmapDraw.text(getString(R.string.core_receipt_cash_amount), currency + formatAmount(record.getCashAmount()), PrintSize.NORMAL, true);
            bitmapDraw.text(getString(R.string.core_receipt_total_amount), PrintSize.NORMAL, true, Paint.Align.LEFT);
            bitmapDraw.text(currency + formatAmount(record.getAmount()), PrintSize.AMOUNT, true, Paint.Align.RIGHT);
        } else if (record.getTipAmount() != 0) {
            // TIP
            bitmapDraw.text(getString(R.string.core_receipt_base_amount), currency + formatAmount(record.getBaseAmount()), PrintSize.NORMAL, true);
            bitmapDraw.text(getString(R.string.core_receipt_tip_amount), currency + formatAmount(record.getTipAmount()), PrintSize.NORMAL, true);
            bitmapDraw.text(getString(R.string.core_receipt_total_amount), PrintSize.NORMAL, true, Paint.Align.LEFT);
            bitmapDraw.text(currency + formatAmount(record.getAmount()), PrintSize.AMOUNT, true, Paint.Align.RIGHT);
        } else if (record.getForeignAmount() != 0) {
            //DCC
            //foreign amount
            String foreignCurrency = CurrencyUtils.getCurrencySymbol(record.getForeignCurrency());
            bitmapDraw.text(getString(R.string.core_receipt_purchase_amount), PrintSize.NORMAL, true, Paint.Align.LEFT);
            bitmapDraw.text(foreignCurrency + formatAmount(record.getForeignAmount()), PrintSize.AMOUNT, true, Paint.Align.RIGHT);
            //local currency amount
            CurrencyUtils.CurrencyBean currencyBean = CurrencyUtils.getCurrency(record.getCurrencyCode());
            bitmapDraw.text(getString(R.string.core_receipt_local_currency_amount_format, currencyBean.getAlphaCode()), formatAmount(record.getAmount()), PrintSize.NORMAL, false);
            if (!TextUtils.isEmpty(record.getMarkupRate())) {
                bitmapDraw.text(getString(R.string.core_receipt_mark_up_rate), record.getMarkupRate(), PrintSize.NORMAL, false);
            }
            if (!TextUtils.isEmpty(record.getConversionRate())) {
                bitmapDraw.text(getString(R.string.core_receipt_conversion_rate), record.getConversionRate(), PrintSize.NORMAL, false);
            }
        } else {
            //Normal transaction
            bitmapDraw.text(currency + formatAmount(record.getAmount()), PrintSize.AMOUNT, true, Paint.Align.CENTER);
        }

        switch (record.getTransType()) {
            case TransType.TRANS_QR_CODE:
            case TransType.TRANS_QR_REFUND:
            case TransType.TRANS_SCAN_PAY:
            case TransType.TRANS_HCE_SALE:
                break;
            default:
                //signature
                if (receiptOwner == RECEIPT_OWNER_MERCHANT) {
                    //merchant copy
                    if (record.isFreeSign()) {
                        bitmapDraw.feedPaper(PrintSize.NORMAL);
                        bitmapDraw.text(getString(R.string.core_receipt_no_signature), PrintSize.NORMAL, false, Paint.Align.CENTER);
                    } else {
                        if (!TextUtils.isEmpty(record.getSignPath())) {
                            bitmapDraw.image(BitmapFactory.decodeFile(record.getSignPath()));
                        } else {
                            bitmapDraw.feedPaper(PrintSize.SIGN_FEED);
                        }
                        bitmapDraw.text(getString(R.string.core_receipt_signature_line), PrintSize.NORMAL, false, Paint.Align.CENTER);
                    }
                }
                break;
        }
        bitmapDraw.text(record.getRemarks(), PrintSize.NORMAL, false, Paint.Align.CENTER);
        if (record.getEntryMode() == EntryMode.INSERT || record.getEntryMode() == EntryMode.TAP) {
            SparseArray<byte[]> emvTlvs = TlvUtils.getTlvList(BytesUtils.hexToBytes(record.getEmvPrintData()));
            boolean appNamePrinted = false;
            if (emvTlvs != null) {
                for (int i = 0; i < emvTlvs.size(); i++) {
                    int tag = emvTlvs.keyAt(i);
                    byte[] value = emvTlvs.get(tag);
                    switch (tag) {
                        case EmvTag.TAG_9F12_IC_APPNAME:
                        case EmvTag.TAG_50_IC_APPLABEL:
                            if (!appNamePrinted) {
                                String emvAppName = new String(value);
                                //check ASCII
                                if (emvAppName.matches("\\A\\p{ASCII}*\\z")) {
                                    bitmapDraw.text(getString(R.string.core_receipt_emv_app_title), emvAppName, PrintSize.SMALL, false);
                                    appNamePrinted = true;
                                }
                            }
                            break;
                        case EmvTag.TAG_4F_IC_AID:
                            bitmapDraw.text(getString(R.string.core_receipt_emv_aid_title), BytesUtils.bcdToString(value), PrintSize.SMALL, false);
                            break;
                        case EmvTag.TAG_95_TM_TVR:
                            bitmapDraw.text(getString(R.string.core_receipt_emv_tvr_title), BytesUtils.bcdToString(value), PrintSize.SMALL, false);
                            break;
                        case EmvTag.TAG_9B_TM_TSI:
                            bitmapDraw.text(getString(R.string.core_receipt_emv_tsi_title), BytesUtils.bcdToString(value), PrintSize.SMALL, false);
                            break;
                        default:
                            break;
                    }
                }
            }

        }
        if (isReprint) {
            bitmapDraw.text(getString(R.string.core_receipt_reprint_flag), PrintSize.NORMAL, false, Paint.Align.CENTER);
        }
        switch (receiptOwner) {
            case RECEIPT_OWNER_MERCHANT:
                bitmapDraw.text(getString(R.string.core_receipt_merchant_copy), PrintSize.NORMAL, false, Paint.Align.CENTER);
                break;
            case RECEIPT_OWNER_CUSTOMER:
                bitmapDraw.text(getString(R.string.core_receipt_customer_copy), PrintSize.NORMAL, false, Paint.Align.CENTER);
                break;
            case RECEIPT_OWNER_BANK:
            default:
                bitmapDraw.text(getString(R.string.core_receipt_bank_copy), PrintSize.NORMAL, false, Paint.Align.CENTER);
                break;
        }
        bitmapDraw.text("-------------x----------------x-------------", PrintSize.LINE, false, Paint.Align.CENTER);
        return bitmapDraw.getBitmap();
    }

    /**
     * generate BRAC receipt bitmap
     */
    public static @NonNull Bitmap getBracTestTranReceipt(Record record, boolean isReprint, @IntRange(from = 0) int receiptOwner) {
        BitmapDraw bitmapDraw = new BitmapDraw();
//        try {
//            bitmapDraw.image(BitmapFactory.decodeStream(BaseApplication.getAppContext().getAssets().open(FileConst.LOGO_IMG)));
//        } catch (IOException e) {
//            LoggerUtils.e("decodeStream " + FileConst.LOGO_IMG + " failed!", e);
//        }
//
//        //Address
//        bitmapDraw.text("BRAC LIVE TEST POS", PrintSize.NORMAL, true, Paint.Align.CENTER);
//        bitmapDraw.text("BRAC BANK PLC, ADC DEPARTMENT", PrintSize.NORMAL, true, Paint.Align.CENTER);
//        bitmapDraw.text("TEJGAON I/A, DHAKA", PrintSize.NORMAL, true, Paint.Align.CENTER);
//        bitmapDraw.text("", PrintSize.NORMAL, true, Paint.Align.CENTER);

        String titleValueSeparator = ":";

        bitmapDraw.text(
                getString(R.string.core_receipt_date_title)
                        + titleValueSeparator + DateUtils.formatOnlyDate(record.getDate()),
                getString(R.string.core_receipt_time_title)
                        + titleValueSeparator + DateUtils.formatOnlyTime(record.getTime()), PrintSize.NORMAL, false);

        bitmapDraw.text(getString(R.string.core_receipt_merchant_id_title) + record.getMid(),
                getString(R.string.core_receipt_terminal_id_title) + record.getTid(),
                PrintSize.NORMAL, false);

        bitmapDraw.text(
                getString(R.string.core_receipt_batch_title) + record.getBatchNo(),
                getString(R.string.core_receipt_trace_title) + record.getTraceNo(),
                PrintSize.NORMAL, false);

        String lTxt = "";
        String rText = "";
        if (!TextUtils.isEmpty(record.getOrigTraceNo())) {
            lTxt = getString(R.string.core_receipt_orig_trace_title) + record.getOrigTraceNo();

            //bitmapDraw.text(getString(R.string.core_receipt_orig_trace_title), record.getOrigTraceNo(), PrintSize.NORMAL, false);
        }
        if (!TextUtils.isEmpty(record.getOrigAuthCode())) {
            rText = getString(R.string.core_receipt_orig_authcode_title) + record.getOrigAuthCode();
//            bitmapDraw.text(getString(R.string.core_receipt_orig_authcode_title), record.getOrigAuthCode(), PrintSize.NORMAL, false);
        }

        if (!lTxt.isEmpty() || !rText.isEmpty())
            bitmapDraw.text(lTxt, rText, PrintSize.NORMAL, false);

        bitmapDraw.text(TransUtils.getName(record.getTransType()).toUpperCase(), PrintSize.TRAN_TYPE, true, Paint.Align.CENTER);

        bitmapDraw.text("", PrintSize.LINE, false, Paint.Align.CENTER);
        bitmapDraw.text("-------------x----------------x-------------", PrintSize.LINE, false, Paint.Align.CENTER);
        return bitmapDraw.getBitmap();
    }


    public static @NonNull BitmapDraw getBracPreDetailsTopPart(BitmapDraw bitmapDraw, String title) {
        //BitmapDraw bitmapDraw = new BitmapDraw();
        int receiptOwner = RECEIPT_OWNER_MERCHANT;

        bitmapDraw.text(title.toUpperCase(), PrintSize.TRAN_TYPE, true, Paint.Align.CENTER);

        //1. CARD NO            EXP DATE
        bitmapDraw.text("CARD NO", "EXP DATE", PrintSize.SMALL, false);

        //2. INVOICE            APPROVAL CODE
        bitmapDraw.text("INVOICE", "APPROVAL CODE", PrintSize.SMALL, false);

        //3. TRANS              AMOUNT
        bitmapDraw.text("TRANS", "AMOUNT", PrintSize.SMALL, false);

        //4. Date               Time
        bitmapDraw.text("DATE", "TIME", PrintSize.SMALL, false);

        //5. CARD TYPE          CARD MODE
        bitmapDraw.text("CARD TYPE", "CARD MODE", PrintSize.SMALL, false);

        //6. ------------------------
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);

        return bitmapDraw;
    }

    public static @NonNull Bitmap getBracAllDetails(Record record) {
        BitmapDraw bitmapDraw = new BitmapDraw();

        //bitmapDraw.text(TransUtils.getName(record.getTransType()).toUpperCase(), PrintSize.TRAN_TYPE, true, Paint.Align.CENTER);

//        bitmapDraw.text(record.getCardScheme(), PrintSize.NORMAL, false, Paint.Align.CENTER);
        /**Card number*/
        String maskedCardNumb = "";
        if (record.getCardNo() != null && !record.getCardNo().isEmpty()) {
            maskedCardNumb = FormatUtils.maskCardNo(record.getCardNo());
            //bitmapDraw.text("CARD: " + maskedCardNumb, PrintSize.TRAN_TYPE, true, Paint.Align.CENTER);
        }

        String expDate = getString(R.string.core_receipt_card_exp_MASK);
        /*if (record.getExpDate() != null && !record.getExpDate().isEmpty()) {
            expDate = record.getExpDate();
        }*/

        //1. CARD NO            EXP DATE
        bitmapDraw.text(maskedCardNumb, "EXP DATE:" + expDate, PrintSize.SMALL, false);

        //2. INVOICE            APPROVAL CODE
        String trnsType = TransUtils.getName(record.getTransType()).toUpperCase();
        String invText = "";
        String apviText = "";
        if (trnsType.contains("Void")) {
            //bitmapDraw.text("INV:"+record.getOrigTraceNo(),"APVI:"+record.getOrigAuthCode(),PrintSize.SMALL, false);
            invText = record.getOrigTraceNo();
            apviText = record.getOrigAuthCode();
        } else {
            invText = record.getTraceNo();
            apviText = record.getAuthCode();
        }
        if (invText == null) invText = "";
        if (apviText == null) apviText = "";
        bitmapDraw.text("INV:" + invText, "APVI:" + apviText, PrintSize.SMALL, false);
        //-------------------------------------


        //3. TRANS              AMOUNT
        String amountTxt = "TK:" + CardInfoUtility.formatAmount(record.getAmount());
        if (trnsType.contains("Void")) {
            trnsType = "VOID SALE";
        }
        bitmapDraw.text(trnsType.toUpperCase(), amountTxt, PrintSize.SMALL, false);

        //4. Date               Time
        String formatedDate = DateUtils.formatOnlyDate(record.getDate());
        String formatedTime = DateUtils.formatOnlyTime(record.getTime());
        bitmapDraw.text("DATE:" + formatedDate, "TIME:" + formatedTime, PrintSize.SMALL, false);

        //5. CARD TYPE          CARD MODE
        String schemeText = record.getCardScheme();
        if (schemeText != null) {
            String onOffUs = "OFFUS " + record.getCardScheme();
            try {
                String ben = record.getCardNo().substring(0, 6);
                String cardTitle = OnUsBinMap.REPORT_CARD_ONUS_MAP.get(ben).getCardTitle();
                if (cardTitle != null && !cardTitle.isEmpty()) {
                    onOffUs = cardTitle;
                }
            } catch (Exception ex) {

            }
            bitmapDraw.text(onOffUs, EntryMode.getDescription(record.getEntryMode()), PrintSize.SMALL, false);
        }
        //----------------------------

        //6. ------------------------
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);

        return bitmapDraw.getBitmap();
    }

    public static String formatNumber(int input) {
        return String.format("%03d", input);
    }

    public static @NonNull Bitmap getBracAllCardReportDetails2(SchemeGroup schemeGroup) {
        BitmapDraw bitmapDraw = new BitmapDraw();

        bitmapDraw.text("", PrintSize.NORMAL, true, Paint.Align.LEFT);

        //VISA ONUS
        bitmapDraw.text(schemeGroup.getSchemeTitle().toUpperCase(), PrintSize.CARD_SCHEME_TITLE, true, Paint.Align.LEFT);
        // doted ------
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);
        // TXN TYPE     COUNT       AMOUNT
        bitmapDraw.textBracCardReportInnerTran("TXN TYPE", "COUNT", "AMOUNT", PrintSize.NORMAL, true);
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);
        // SALE      003     TK 1.03
        for (GroupByTranType groupByTranType : schemeGroup.getGroupByTranType()) {
            String count = formatNumber(groupByTranType.getCount());

            String title = groupByTranType.getTitle();
            if (title == null || title.isEmpty()) title = groupByTranType.getTranType();

            bitmapDraw.textBracCardReportInnerTran(title.toUpperCase(), count, "TK:" + CardInfoUtility.formatAmount(groupByTranType.getAmount()), PrintSize.NORMAL, true);
            // doted ------
            bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);

            //TIP
            if (groupByTranType.getTipAmount() > 0) {
                bitmapDraw.textBracCardReportInnerTran("TIP", count, "TK:" + CardInfoUtility.formatAmount(groupByTranType.getTipAmount()), PrintSize.NORMAL, true);
                // doted ------
                bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);
            }

        }
        // TOTAL      003     TK 1.03
        bitmapDraw.textBracCardReportInnerTran("TOTAL", formatNumber(schemeGroup.getTotalSchemeWiseTranCount()), "TK:" + CardInfoUtility.formatAmount(schemeGroup.getTotalSchemeWiseTranAmount()), PrintSize.NORMAL, true);
        // doted ------
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);

        return bitmapDraw.getBitmap();
    }


    public static @NonNull Bitmap getBracAllCardReportDetails(CardSchemeReportModel cardSchemeReportModel) {
        BitmapDraw bitmapDraw = new BitmapDraw();

        bitmapDraw.text("", PrintSize.NORMAL, true, Paint.Align.LEFT);
        //VISA ONUS
        bitmapDraw.text(cardSchemeReportModel.getScheme().toUpperCase(), PrintSize.CARD_SCHEME_TITLE, true, Paint.Align.LEFT);
        // doted ------
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);
        // TXN TYPE     COUNT       AMOUNT
        bitmapDraw.text(getString(R.string.core_receipt_card_report_title), "AMOUNT", PrintSize.NORMAL, true);
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);
        // SALE      003     TK 1.03
        String count = formatNumber(cardSchemeReportModel.getSaleCount());
        String countVoid = formatNumber(cardSchemeReportModel.getVoidCount());
        bitmapDraw.text("SALE                     " + count, "TK:" + CardInfoUtility.formatAmount(cardSchemeReportModel.getSaleAmount()), PrintSize.NORMAL, true);
        bitmapDraw.text("VOID                     " + countVoid, "-TK:" + CardInfoUtility.formatAmount(cardSchemeReportModel.getVoidAmount()), PrintSize.NORMAL, true);
        // doted ------
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);
        // TOTAL      003     TK 1.03
        bitmapDraw.text("TOTAL                  " + count, "TK:" + CardInfoUtility.formatAmount(cardSchemeReportModel.getSaleAmount()), PrintSize.NORMAL, true);
        // doted ------
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);
        // doted ------

        return bitmapDraw.getBitmap();
    }


    public static @NonNull Bitmap getBracAllDetails2(String topReportTitle, String bottomReportTitle, String reportCode, List<Record> recordsData, List<SchemeGroup> schemeList) {

        BitmapDraw bitmapDraw = new BitmapDraw();
        Record recordDef = null;
        bitmapDraw = headerPart(bitmapDraw);


        if (recordsData.size() > 0) {
            recordDef = recordsData.get(0); // Consider first record as default
        } else {
            bitmapDraw.text("⚠\uFE0F", PrintSize.TRAN_TYPE, false, Paint.Align.CENTER);
            bitmapDraw.text("No records", PrintSize.LINE, false, Paint.Align.CENTER);
            bitmapDraw.text("-------------x----------------x-------------", PrintSize.LINE, false, Paint.Align.CENTER);
            return bitmapDraw.getBitmap();
        }

        boolean isShowStan = false;

        bitmapDraw = bracMerchantDetailsPart(bitmapDraw, recordDef, isShowStan);

        if (!reportCode.equals(ReportConstant.REPORT_ITEM_CODE_CARD_REPORT)) {
            bitmapDraw = getBracPreDetailsTopPart(bitmapDraw, topReportTitle);
            for (Record record : recordsData) {
                bitmapDraw.image(getBracAllDetails(record));
            }
            bitmapDraw.text("", PrintSize.NORMAL, true, Paint.Align.LEFT);
        }

        // Card report
        bitmapDraw.text("CARD REPORT", PrintSize.CARD_SCHEME_TITLE, true, Paint.Align.CENTER);
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);

        for (SchemeGroup schemeGroup : schemeList) {
            bitmapDraw.image(getBracAllCardReportDetails2(schemeGroup));
        }

        bitmapDraw.text(bottomReportTitle, PrintSize.CARD_SCHEME_TITLE, true, Paint.Align.CENTER);
        bitmapDraw.text("", PrintSize.NORMAL, true, Paint.Align.LEFT);
        //bitmapDraw = footerPart(bitmapDraw, recordDef);
        return bitmapDraw.getBitmap();
    }

    public static @NonNull Bitmap getBracAllDetails(String topReportTitle, String bottomReportTitle, String reportCode, List<Record> recordsData, List<CardSchemeReportModel> schemeList) {
        BitmapDraw bitmapDraw = new BitmapDraw();
        Record recordDef = null;
        bitmapDraw = headerPart(bitmapDraw);


        if (recordsData.size() > 0) {
            recordDef = recordsData.get(0); // Consider first record as default
            //tranName = TransUtils.getName(recordDef.getTransType());
        } else {
            bitmapDraw.text("⚠\uFE0F", PrintSize.TRAN_TYPE, false, Paint.Align.CENTER);
            bitmapDraw.text("No records", PrintSize.LINE, false, Paint.Align.CENTER);
            bitmapDraw.text("-------------x----------------x-------------", PrintSize.LINE, false, Paint.Align.CENTER);
            return bitmapDraw.getBitmap();
        }

        boolean isShowStan = false;

        bitmapDraw = bracMerchantDetailsPart(bitmapDraw, recordDef, isShowStan);

        if (!reportCode.equals(ReportConstant.REPORT_ITEM_CODE_CARD_REPORT)) {

            bitmapDraw = getBracPreDetailsTopPart(bitmapDraw, topReportTitle);

            for (Record record : recordsData) {
                bitmapDraw.image(getBracAllDetails(record));
            }
            bitmapDraw.text("", PrintSize.NORMAL, true, Paint.Align.LEFT);
        }
        // Card report
        bitmapDraw.text("CARD REPORT", PrintSize.CARD_SCHEME_TITLE, true, Paint.Align.CENTER);
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);
        CardSchemeReportModel grandTotalReportModel = new CardSchemeReportModel();
        for (CardSchemeReportModel cardSchemeReportModel : schemeList) {
            if (cardSchemeReportModel.getScheme().equalsIgnoreCase("Grand Total"))
                grandTotalReportModel = cardSchemeReportModel;
            else
                bitmapDraw.image(getBracAllCardReportDetails(cardSchemeReportModel));
        }
        bitmapDraw.image(getBracAllCardReportDetails(grandTotalReportModel));
        bitmapDraw.text(bottomReportTitle, PrintSize.CARD_SCHEME_TITLE, true, Paint.Align.CENTER);
        bitmapDraw.text("", PrintSize.NORMAL, true, Paint.Align.LEFT);
        bitmapDraw.text("", PrintSize.NORMAL, true, Paint.Align.LEFT);

        //      bitmapDraw = footerPart(bitmapDraw, recordDef);
        return bitmapDraw.getBitmap();
    }

    private static BitmapDraw headerPart(BitmapDraw bitmapDraw) {

        //Logo
        try {
            bitmapDraw.image(BitmapFactory.decodeStream(BaseApplication.getAppContext().getAssets().open(FileConst.LOGO_IMG)));
        } catch (IOException e) {
            LoggerUtils.e("decodeStream " + FileConst.LOGO_IMG + " failed!", e);
        }

        //Address
/*
        bitmapDraw.text("BRAC LIVE TEST POS", PrintSize.NORMAL, true, Paint.Align.CENTER);
        bitmapDraw.text("BRAC BANK PLC, ADC DEPARTMENT", PrintSize.NORMAL, true, Paint.Align.CENTER);
        bitmapDraw.text("TEJGAON I/A, DHAKA", PrintSize.NORMAL, true, Paint.Align.CENTER);
        bitmapDraw.text("", PrintSize.NORMAL, true, Paint.Align.CENTER);
*/
        bitmapDraw = getMerchantParamAddress(bitmapDraw);
        return bitmapDraw;
    }


    private static BitmapDraw bracSettleMerchantDetailsPart(BitmapDraw bitmapDraw, Merchant merchant, boolean isShowStan) {
        if (merchant == null) return bitmapDraw;

        String titleValueSeparator = ":";
//        String formatedDate = DateUtils.formatOnlyDate(record.getDate());
//        String formatedTime = DateUtils.formatOnlyTime(record.getTime());
        bitmapDraw.text(
                getString(R.string.core_receipt_date_title)
                        + titleValueSeparator + DateUtils.formatOnlyDate(merchant.getSettleDate()),
                getString(R.string.core_receipt_time_title)
                        + titleValueSeparator + DateUtils.formatOnlyTime(merchant.getSettleTime()), PrintSize.NORMAL, false);

        bitmapDraw.text(getString(R.string.core_receipt_merchant_id_title) + merchant.getMid(),
                getString(R.string.core_receipt_terminal_id_title) + merchant.getTid(),
                PrintSize.NORMAL, false);

        //bitmapDraw.text(getString(R.string.core_receipt_terminal_id_title), record.getTid(), PrintSize.SMALL, false);

        if (isShowStan) {
            bitmapDraw.text(
                    getString(R.string.core_receipt_batch_title) + merchant.getBatchNo(),
                    /*getString(R.string.core_receipt_trace_title) + merchant.getTraceNo()*/"",
                    PrintSize.NORMAL, false);
        } else {
            String host = "";
            //TODO: set Host logic for settlement report
            bitmapDraw.text(
                    getString(R.string.core_receipt_batch_title) + merchant.getBatchNo(),
                    host,
                    PrintSize.NORMAL, false);
        }

        String lTxt = "";
        String rText = "";
        /*
        if (!TextUtils.isEmpty(merchant.getOrigTraceNo())) {
            lTxt = getString(R.string.core_receipt_orig_trace_title) + record.getOrigTraceNo();

        }
        if (!TextUtils.isEmpty(record.getOrigAuthCode())) {
            rText = getString(R.string.core_receipt_orig_authcode_title) + record.getOrigAuthCode();
//            bitmapDraw.text(getString(R.string.core_receipt_orig_authcode_title), record.getOrigAuthCode(), PrintSize.NORMAL, false);
        }
*/
        if (!lTxt.isEmpty() || !rText.isEmpty())
            bitmapDraw.text(lTxt, rText, PrintSize.NORMAL, false);

        return bitmapDraw;
    }

    private static BitmapDraw bracMerchantDetailsPart(BitmapDraw bitmapDraw, Record record, boolean isShowStan) {
        if (record == null) return bitmapDraw;

        String titleValueSeparator = ":";
//        String formatedDate = DateUtils.formatOnlyDate(record.getDate());
//        String formatedTime = DateUtils.formatOnlyTime(record.getTime());
        bitmapDraw.text(
                getString(R.string.core_receipt_date_title)
                        + titleValueSeparator + DateUtils.formatOnlyDate(record.getDate()),
                getString(R.string.core_receipt_time_title)
                        + titleValueSeparator + DateUtils.formatOnlyTime(record.getTime()), PrintSize.NORMAL, false);

        bitmapDraw.text(getString(R.string.core_receipt_merchant_id_title) + record.getMid(),
                getString(R.string.core_receipt_terminal_id_title) + record.getTid(),
                PrintSize.NORMAL, false);

        //bitmapDraw.text(getString(R.string.core_receipt_terminal_id_title), record.getTid(), PrintSize.SMALL, false);

        if (isShowStan) {
            bitmapDraw.text(
                    getString(R.string.core_receipt_batch_title) + record.getBatchNo(),
                    getString(R.string.core_receipt_trace_title) + record.getTraceNo(),
                    PrintSize.NORMAL, false);
        } else {
            String host = "";
            //TODO: set Host logic for settlement report
            bitmapDraw.text(
                    getString(R.string.core_receipt_batch_title) + record.getBatchNo(),
                    host,
                    PrintSize.NORMAL, false);
        }
//        bitmapDraw.text(getString(R.string.core_receipt_trace_title), record.getTraceNo(), PrintSize.SMALL, false);

        //bitmapDraw.text(getString(R.string.core_receipt_refnum_title), record.getReferNo(), PrintSize.SMALL, false);
//        bitmapDraw.text(getString(R.string.core_receipt_authcode_title), record.getAuthCode(), PrintSize.SMALL, false);

        String lTxt = "";
        String rText = "";
        if (!TextUtils.isEmpty(record.getOrigTraceNo())) {
            lTxt = getString(R.string.core_receipt_orig_trace_title) + record.getOrigTraceNo();


            //bitmapDraw.text(getString(R.string.core_receipt_orig_trace_title), record.getOrigTraceNo(), PrintSize.NORMAL, false);
        }
        if (!TextUtils.isEmpty(record.getOrigAuthCode())) {
            rText = getString(R.string.core_receipt_orig_authcode_title) + record.getOrigAuthCode();
//            bitmapDraw.text(getString(R.string.core_receipt_orig_authcode_title), record.getOrigAuthCode(), PrintSize.NORMAL, false);
        }

        if (!lTxt.isEmpty() || !rText.isEmpty())
            bitmapDraw.text(lTxt, rText, PrintSize.NORMAL, false);

        return bitmapDraw;
    }

    private static BitmapDraw footerPart(BitmapDraw bitmapDraw, Record record) {

        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);

        //Card holder name
        String cardName = " / ";
        String cardHolderName = record.getCardHolderName(); //CardInfoUtility.getCardholderName(record.getField55(), record.getDate());
        if (cardHolderName != null && !cardHolderName.isEmpty()) {
            cardName = cardHolderName;
        }
        bitmapDraw.text(cardName, PrintSize.NORMAL, false, Paint.Align.CENTER);

        //get footer message from INI param
        ArrayList<String> footerLines = new ArrayList<>();// ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR1,"");
        footerLines.add(ParamsUtils.getString(ParamsConst.PARAMS_KEY_FOOTER1, ""));
        footerLines.add(ParamsUtils.getString(ParamsConst.PARAMS_KEY_FOOTER2, ""));
        footerLines.add(ParamsUtils.getString(ParamsConst.PARAMS_KEY_FOOTER3, ""));
        footerLines.add(ParamsUtils.getString(ParamsConst.PARAMS_KEY_FOOTER4, ""));

        for (String adrs : footerLines) {
            if (!adrs.isEmpty())
                bitmapDraw.text(adrs, PrintSize.SMALL, true, Paint.Align.CENTER);
        }

        //        bitmapDraw.text("I AGREE TO PAY THE ABOVE TOTAL AMOUNT", PrintSize.NORMAL, false, Paint.Align.CENTER);

        int receiptOwner = RECEIPT_OWNER_MERCHANT;

        switch (receiptOwner) {
            case RECEIPT_OWNER_MERCHANT:
                bitmapDraw.text(getString(R.string.core_receipt_merchant_copy), PrintSize.NORMAL, false, Paint.Align.CENTER);
                break;
            case RECEIPT_OWNER_CUSTOMER:
                bitmapDraw.text(getString(R.string.core_receipt_customer_copy), PrintSize.NORMAL, false, Paint.Align.CENTER);
                break;
            case RECEIPT_OWNER_BANK:
            default:
                bitmapDraw.text(getString(R.string.core_receipt_bank_copy), PrintSize.NORMAL, false, Paint.Align.CENTER);
                break;
        }

        //  bitmapDraw.text("", PrintSize.LINE, false, Paint.Align.CENTER);
        //  bitmapDraw.text("-------------x----------------x-------------", PrintSize.LINE, false, Paint.Align.CENTER);

        return bitmapDraw;
    }

    public static @NonNull Bitmap getBracQRImage(Record record) {
        Bitmap bitmap = null;
        BitmapDraw bitmapDraw = new BitmapDraw();
        String maskedCardNumb = "";
        if (record.getCardNo() != null && !record.getCardNo().isEmpty()) {
            maskedCardNumb = FormatUtils.maskCardNo(record.getCardNo());
        }

        String formatedDate = DateUtils.formatOnlyDate(record.getDate());
        String formatedTime = DateUtils.formatOnlyTime(record.getTime());

        StringBuilder qrCodeInfo = new StringBuilder();
        qrCodeInfo.append(formatAmount(record.getAmount() + record.getTipAmount())).append(",").append("\n");
        qrCodeInfo.append(record.getAuthCode()).append(",").append("\n");
        qrCodeInfo.append("BBPLC,").append("\n");
        qrCodeInfo.append(maskedCardNumb).append(",").append("\n");
        qrCodeInfo.append(record.getCardScheme()).append(",").append("\n");
        qrCodeInfo.append(formatedDate).append(",").append("\n");
        qrCodeInfo.append(formatedTime).append(",").append("\n");
        qrCodeInfo.append(record.getMid()).append(",").append("\n");
        qrCodeInfo.append(record.getTid()).append(",").append("\n");

        if (true || !TextUtils.isEmpty(record.getBizOrderNo())) {
//            Bitmap bitmap = QRCodeUtils.create2dCode(record.getBizOrderNo());
            bitmap = QRCodeUtils.create2dCode(qrCodeInfo.toString());
            if (bitmap != null) {
                bitmapDraw.image(bitmap);
                //bitmapDraw.text("", PrintSize.NORMAL, false, Paint.Align.CENTER);
            }
        }
        return bitmap;
    }

    private static @NonNull BitmapDraw getMerchantParamAddress(BitmapDraw bitmapDraw) {

        String mName = ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_NAME_NEW, "");
        if (mName != null && !mName.isEmpty())
            bitmapDraw.text(mName, PrintSize.MERCHANT_NAME_HEADER_TITLE, true, Paint.Align.CENTER);

        ArrayList<String> adrsLins = new ArrayList<>();// ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR1,"");
        adrsLins.add(ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR1, ""));
        adrsLins.add(ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR2, ""));
        adrsLins.add(ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR3, ""));
        adrsLins.add(ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR4, ""));
        adrsLins.add(ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR5, ""));

        for (String adrs : adrsLins) {
            if (!adrs.isEmpty())
                bitmapDraw.text(adrs, PrintSize.NORMAL, true, Paint.Align.CENTER);
        }

        return bitmapDraw;
    }


    //Brac PayFlex
    private static BitmapDraw payFlexPart(BitmapDraw bitmapDraw, Record record) {

        String f63 = record.getField63();
        if (f63 == null || f63.isEmpty()) return bitmapDraw;

        PayFlexField63ResponseModel payFlexField63ResponseModel = FieldDataParseUtility.parseField63ASci(f63);

        /*
        Program ID:
        INSTALLMENT PREOID:
        FIRST INSTALLMENT :
        MONTHLY INSTALLMENT:
        VENDOR ID:
        * */
        long fInsAmnt = 0;
        long mInsAmnt = 0;
        try {
            fInsAmnt = Integer.parseInt(payFlexField63ResponseModel.getFirstInstallmentAmount());
        } catch (Exception ex) {
            LoggerUtils.e("newCall Number formate exception in PrintViewModel payFlex FirstInstallmentAmount: " + ex.getMessage());
        }
        try {
            mInsAmnt = Integer.parseInt(payFlexField63ResponseModel.getMonthlyInstallmentAmount());
        } catch (Exception ex) {
            LoggerUtils.e("newCall Number formate exception in PrintViewModel payFlex MonthlyInstallmentAmount: " + ex.getMessage());
        }

        bitmapDraw.text("PROGRAM ID: ", payFlexField63ResponseModel.getProgramId(), PrintSize.NORMAL, false);
        bitmapDraw.text("INSTALLMENT PREOID: ", payFlexField63ResponseModel.getInstallmentPeriod(), PrintSize.NORMAL, false);
        bitmapDraw.text("FIRST INSTALLMENT: ", formatAmount(fInsAmnt), PrintSize.NORMAL, false);
        bitmapDraw.text("MONTHLY INSTALLMENT: ", formatAmount(mInsAmnt), PrintSize.NORMAL, false);
        bitmapDraw.text("VENDOR ID: ", payFlexField63ResponseModel.getVendorId(), PrintSize.NORMAL, false);
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);

        return bitmapDraw;
    }

    public static @NonNull Bitmap getBracReceipt(Record record, boolean isReprint, @IntRange(from = 0) int receiptOwner) {
        BitmapDraw bitmapDraw = new BitmapDraw();
        boolean isShowExpDate = false;
        bitmapDraw = headerPart(bitmapDraw);

//        try {
//            bitmapDraw.image(BitmapFactory.decodeStream(BaseApplication.getAppContext().getAssets().open(FileConst.LOGO_IMG)));
//        } catch (IOException e) {
//            LoggerUtils.e("decodeStream " + FileConst.LOGO_IMG + " failed!", e);
//        }

        String titleValueSeparator = ":";
        String formatedDate = DateUtils.formatOnlyDate(record.getDate());
        String formatedTime = DateUtils.formatOnlyTime(record.getTime());
        bitmapDraw.text(
                getString(R.string.core_receipt_date_title)
                        + titleValueSeparator + DateUtils.formatOnlyDate(record.getDate()),
                getString(R.string.core_receipt_time_title)
                        + titleValueSeparator + DateUtils.formatOnlyTime(record.getTime()), PrintSize.NORMAL, false);

        bitmapDraw.text(getString(R.string.core_receipt_merchant_id_title) + record.getMid(),
                getString(R.string.core_receipt_terminal_id_title) + record.getTid(),
                PrintSize.NORMAL, false);

        //bitmapDraw.text(getString(R.string.core_receipt_terminal_id_title), record.getTid(), PrintSize.SMALL, false);

        bitmapDraw.text(
                getString(R.string.core_receipt_batch_title) + record.getBatchNo(),
                getString(R.string.core_receipt_trace_title) + record.getTraceNo(),
                PrintSize.NORMAL, false);

//        bitmapDraw.text(getString(R.string.core_receipt_trace_title), record.getTraceNo(), PrintSize.SMALL, false);

        //bitmapDraw.text(getString(R.string.core_receipt_refnum_title), record.getReferNo(), PrintSize.SMALL, false);
//        bitmapDraw.text(getString(R.string.core_receipt_authcode_title), record.getAuthCode(), PrintSize.SMALL, false);

        String lTxt = "";
        String rText = "";
        if (!TextUtils.isEmpty(record.getOrigTraceNo())) {
            lTxt = getString(R.string.core_receipt_orig_trace_title) + record.getOrigTraceNo();


            //bitmapDraw.text(getString(R.string.core_receipt_orig_trace_title), record.getOrigTraceNo(), PrintSize.NORMAL, false);
        }
        if (!TextUtils.isEmpty(record.getOrigAuthCode())) {
            rText = getString(R.string.core_receipt_orig_authcode_title) + record.getOrigAuthCode();
//            bitmapDraw.text(getString(R.string.core_receipt_orig_authcode_title), record.getOrigAuthCode(), PrintSize.NORMAL, false);
        }

        if (!lTxt.isEmpty() || !rText.isEmpty())
            bitmapDraw.text(lTxt, rText, PrintSize.NORMAL, false);

        //This change for Brac PayFlex
        String title = TransUtils.getName(record.getTransType());
        if (record.getTransType().equalsIgnoreCase(TransType.TRANS_INSTALLMENT) || record.getTransType().equalsIgnoreCase(TransType.TRANS_VOID_INSTALLMENT)) {
            title = record.getDisplayTitle();
        }

        bitmapDraw.text(title.toUpperCase(), PrintSize.TRAN_TYPE, true, Paint.Align.CENTER);

//        bitmapDraw.text(record.getCardScheme(), PrintSize.NORMAL, false, Paint.Align.CENTER);
        /**Card number*/
        String maskedCardNumb = "";
        if (record.getCardNo() != null && !record.getCardNo().isEmpty()) {
            maskedCardNumb = FormatUtils.maskCardNo(record.getCardNo());
            bitmapDraw.text("CARD: " + maskedCardNumb, PrintSize.TRAN_TYPE, true, Paint.Align.CENTER);
        }
        bitmapDraw.text("CARD TYPE: " + record.getCardScheme(), EntryMode.getDescription(record.getEntryMode()), PrintSize.SMALL, false);

        String expDate = getString(R.string.core_receipt_card_exp_MASK);
        if (isShowExpDate && record.getExpDate() != null && !record.getExpDate().isEmpty()) {
            expDate = record.getExpDate();
            expDate = DateUtils.formatExpDate(expDate);
        }

        bitmapDraw.text(
                getString(R.string.core_receipt_card_exp) + expDate,
                "RRN: " + record.getReferNo(),
                PrintSize.SMALL, false);

//        bitmapDraw.text(EntryMode.getDescription(record.getEntryMode()), PrintSize.NORMAL, false, Paint.Align.CENTER);
//        bitmapDraw.text(FormatUtils.maskCardNo(record.getCardNo()), PrintSize.NORMAL, false, Paint.Align.CENTER);

        String inV = record.getOrigTraceNo();
        if (inV == null || inV.isEmpty()) inV = record.getTraceNo();
        bitmapDraw.text("APPROVAL CODE: " + record.getAuthCode(), "", PrintSize.NORMAL, false);
        if (inV == null) inV = "";
        bitmapDraw.text("INVOICE NO: " + inV, "", PrintSize.NORMAL, false);

        //app name
        if (record.getEntryMode() == EntryMode.INSERT || record.getEntryMode() == EntryMode.TAP) {
            SparseArray<byte[]> emvTlvs = TlvUtils.getTlvList(BytesUtils.hexToBytes(record.getEmvPrintData()));
            boolean appNamePrinted = false;
            if (emvTlvs != null) {
                for (int i = 0; i < emvTlvs.size(); i++) {
                    int tag = emvTlvs.keyAt(i);
                    byte[] value = emvTlvs.get(tag);
                    switch (tag) {
                        case EmvTag.TAG_9F12_IC_APPNAME:
                        case EmvTag.TAG_50_IC_APPLABEL:
                            if (!appNamePrinted) {
                                String emvAppName = new String(value);
                                //check ASCII
                                if (emvAppName.matches("\\A\\p{ASCII}*\\z")) {
                                    bitmapDraw.text(getString(R.string.core_receipt_emv_app_title) + emvAppName, "", PrintSize.SMALL, false);
                                    appNamePrinted = true;
                                }
                            }
                            break;
                        case EmvTag.TAG_4F_IC_AID:
                            bitmapDraw.text(getString(R.string.core_receipt_emv_aid_title) + BytesUtils.bcdToString(value), "", PrintSize.SMALL, false);
                            break;
                        case EmvTag.TAG_95_TM_TVR:
                            bitmapDraw.text(getString(R.string.core_receipt_emv_tvr_title) + BytesUtils.bcdToString(value), "", PrintSize.SMALL, false);
                            break;
                        case EmvTag.TAG_9B_TM_TSI:
                            bitmapDraw.text(getString(R.string.core_receipt_emv_tsi_title) + BytesUtils.bcdToString(value), "", PrintSize.SMALL, false);
                            break;
                        default:
                            break;
                    }
                }
            }

        }
        if (isReprint) {
            bitmapDraw.text(getString(R.string.core_receipt_reprint_flag), PrintSize.NORMAL, false, Paint.Align.CENTER);
        }


        // Before amount separator line
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);

        String currency = CurrencyUtils.getCurrencySymbol(record.getCurrencyCode());

        if (record.getCashAmount() != 0) {
            //Cash back
            bitmapDraw.text(getString(R.string.core_receipt_purchase_amount), currency + formatAmount(record.getBaseAmount()), PrintSize.NORMAL, true);
            bitmapDraw.text(getString(R.string.core_receipt_cash_amount), currency + formatAmount(record.getCashAmount()), PrintSize.NORMAL, true);
            bitmapDraw.text(getString(R.string.core_receipt_total_amount), PrintSize.NORMAL, true, Paint.Align.LEFT);
            bitmapDraw.text(currency + formatAmount(record.getAmount()), PrintSize.AMOUNT, true, Paint.Align.RIGHT);
        } else if (record.getTipAmount() != 0) {
            // TIP
            bitmapDraw.text(getString(R.string.core_receipt_base_amount), currency + formatAmount(record.getBaseAmount()), PrintSize.NORMAL, true);
            bitmapDraw.text(getString(R.string.core_receipt_tip_amount), currency + formatAmount(record.getTipAmount()), PrintSize.NORMAL, true);
            // change for Brac
            //bitmapDraw.text(getString(R.string.core_receipt_total_amount), PrintSize.NORMAL, true, Paint.Align.LEFT);
            //bitmapDraw.text(currency + formatAmount(record.getAmount()), PrintSize.AMOUNT, true, Paint.Align.RIGHT);
            //--------
        } else if (record.getForeignAmount() != 0) {
            //DCC
            //foreign amount
            String foreignCurrency = CurrencyUtils.getCurrencySymbol(record.getForeignCurrency());
            bitmapDraw.text(getString(R.string.core_receipt_purchase_amount), PrintSize.NORMAL, true, Paint.Align.LEFT);
            bitmapDraw.text(foreignCurrency + formatAmount(record.getForeignAmount()), PrintSize.AMOUNT, true, Paint.Align.RIGHT);
            //local currency amount
            CurrencyUtils.CurrencyBean currencyBean = CurrencyUtils.getCurrency(record.getCurrencyCode());
            bitmapDraw.text(getString(R.string.core_receipt_local_currency_amount_format, currencyBean.getAlphaCode()), formatAmount(record.getAmount()), PrintSize.NORMAL, false);
            if (!TextUtils.isEmpty(record.getMarkupRate())) {
                bitmapDraw.text(getString(R.string.core_receipt_mark_up_rate), record.getMarkupRate(), PrintSize.NORMAL, false);
            }
            if (!TextUtils.isEmpty(record.getConversionRate())) {
                bitmapDraw.text(getString(R.string.core_receipt_conversion_rate), record.getConversionRate(), PrintSize.NORMAL, false);
            }
        } else {
            //Normal transaction
            //bitmapDraw.text(currency + FormatUtils.formatAmount(record.getAmount()), PrintSize.AMOUNT, true, Paint.Align.CENTER);
//            bitmapDraw.text(currency + FormatUtils.formatAmount(record.getAmount()), PrintSize.AMOUNT, true, Paint.Align.CENTER);
            bitmapDraw.text(getString(R.string.core_receipt_amount), formatAmount(record.getAmount()), PrintSize.NORMAL, true);
        }

        // After amount separator line
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);

        //Total amount
        if (record.getTransType().equalsIgnoreCase(TransType.TRANS_SALE)
                || record.getTransType().equalsIgnoreCase(TransType.TRANS_TIP_SALE)
        ) {
            bitmapDraw.text(getString(R.string.core_receipt_total_tk_amount), formatAmount(record.getAmount()), PrintSize.NORMAL, true);
            bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);
        }

        //This change for Brac PayFlex
        if (record.getTransType().equalsIgnoreCase(TransType.TRANS_INSTALLMENT) || record.getTransType().equalsIgnoreCase(TransType.TRANS_VOID_INSTALLMENT)) {
            bitmapDraw = payFlexPart(bitmapDraw, record);
        }

        //PIN VERIFIED
        //------------

        switch (record.getTransType()) {
            case TransType.TRANS_QR_CODE:
            case TransType.TRANS_QR_REFUND:
            case TransType.TRANS_SCAN_PAY:
            case TransType.TRANS_HCE_SALE:
                break;
            default:
                //signature
                if (receiptOwner == RECEIPT_OWNER_MERCHANT) {
                    //merchant copy
                    if (record.isFreeSign()) {
                        bitmapDraw.feedPaper(PrintSize.NORMAL);
                        bitmapDraw.text(getString(R.string.core_receipt_no_signature), PrintSize.NORMAL, false, Paint.Align.CENTER);
                    } else {
                        if (!TextUtils.isEmpty(record.getSignPath())) {
                            bitmapDraw.image(BitmapFactory.decodeFile(record.getSignPath()));
                        } else {
                            bitmapDraw.feedPaper(PrintSize.SIGN_FEED);
                        }
                        bitmapDraw.text(getString(R.string.core_receipt_signature_line), PrintSize.NORMAL, false, Paint.Align.CENTER);
                    }
                }
                break;
        }

        if (record.getRemarks() != null && !record.getRemarks().isEmpty())
            bitmapDraw.text(record.getRemarks(), PrintSize.NORMAL, false, Paint.Align.CENTER);

        bitmapDraw = footerPart(bitmapDraw, record);

/*
        //Line separator
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);
        //Card holder name
        bitmapDraw.text(" / ", PrintSize.NORMAL, false, Paint.Align.CENTER);

        //bottom message
        bitmapDraw.text("I AGREE TO PAY THE ABOVE TOTAL AMOUNT", PrintSize.NORMAL, false, Paint.Align.CENTER);


        //type of print copy
        switch (receiptOwner) {
            case RECEIPT_OWNER_MERCHANT:
                bitmapDraw.text(getString(R.string.core_receipt_merchant_copy), PrintSize.NORMAL, false, Paint.Align.CENTER);
                break;
            case RECEIPT_OWNER_CUSTOMER:
                bitmapDraw.text(getString(R.string.core_receipt_customer_copy), PrintSize.NORMAL, false, Paint.Align.CENTER);
                break;
            case RECEIPT_OWNER_BANK:
            default:
                bitmapDraw.text(getString(R.string.core_receipt_bank_copy), PrintSize.NORMAL, false, Paint.Align.CENTER);
                break;
        }

        bitmapDraw.text("", PrintSize.LINE, false, Paint.Align.CENTER);

*/

        //fro the QR test

        /*
        * 2.00,
        320738,
        UCB,
        462870****8098,
        VISA,
        16/02/2026,
        10:15:37,
        700000000009999,
        80000001
        **/

        if (record.getTransType().equalsIgnoreCase(TransType.TRANS_SALE) && ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_ENABLE_QR_RECEIPT_SLIP, false)) {
            Bitmap qrBit = getBracQRImage(record);

            if (qrBit != null) {
                bitmapDraw.image(qrBit);
                bitmapDraw.text("", PrintSize.NORMAL, false, Paint.Align.CENTER);
            }
            /*
            StringBuilder qrCodeInfo = new StringBuilder();
            qrCodeInfo.append(formatAmount(record.getAmount() + record.getTipAmount())).append(",").append("\n");
            qrCodeInfo.append(record.getAuthCode()).append(",").append("\n");
            qrCodeInfo.append("BBPLC,").append("\n");
            qrCodeInfo.append(maskedCardNumb).append(",").append("\n");
            qrCodeInfo.append(record.getCardScheme()).append(",").append("\n");
            qrCodeInfo.append(formatedDate).append(",").append("\n");
            qrCodeInfo.append(formatedTime).append(",").append("\n");
            qrCodeInfo.append(record.getMid()).append(",").append("\n");
            qrCodeInfo.append(record.getTid()).append(",").append("\n");
            if (true || !TextUtils.isEmpty(record.getBizOrderNo())) {
//            Bitmap bitmap = QRCodeUtils.create2dCode(record.getBizOrderNo());
                Bitmap bitmap = QRCodeUtils.create2dCode(qrCodeInfo.toString());
                if (bitmap != null) {
                    bitmapDraw.image(bitmap);
                    bitmapDraw.text("", PrintSize.NORMAL, false, Paint.Align.CENTER);
                }
            }
            */
        }

        bitmapDraw.text("-------------x----------------x-------------", PrintSize.LINE, false, Paint.Align.CENTER);
        return bitmapDraw.getBitmap();
    }

    /**
     * print settle ticket.
     */
    public void printSettlement(SettleReceiptBean settleReceiptBean) {
        status.postValue(new PrtStatus(STATUS_READY));
        prompt.postValue(R.string.core_print_progress_settlement);
        //settlement receipt path
        Bitmap bitmap = getSettleTicket(settleReceiptBean);
        receipt.postValue(bitmap);
        printer.print(bitmap, true, new IPrinter.PrintCallback() {
            @Override
            public void onFinish() {
                //finish
                if (supportCut) {
                    printer.cutPaper();
                }
                status.postValue(new PrtStatus(STATUS_SUCCESS));
            }

            @Override
            public void onError(String message) {
                status.postValue(new PrtStatus(STATUS_ERROR, message));
            }

            @Override
            public void onOutOfPaper() {
                status.postValue(new PrtStatus(STATUS_OUT_OF_PAPER));
            }
        });
    }

    public void printBracSettlement(SettleReceiptBean settleReceiptBean, List<Record> recordsData) {
        status.postValue(new PrtStatus(STATUS_READY));
        prompt.postValue(R.string.core_print_progress_settlement);
        //settlement receipt path
        Bitmap bitmap = getSettleBracTicket(settleReceiptBean);
        receipt.postValue(bitmap);
        printer.print(bitmap, true, new IPrinter.PrintCallback() {
            @Override
            public void onFinish() {
                //finish
                if (supportCut) {
                    printer.cutPaper();
                }
                status.postValue(new PrtStatus(STATUS_SUCCESS));
            }

            @Override
            public void onError(String message) {
                status.postValue(new PrtStatus(STATUS_ERROR, message));
            }

            @Override
            public void onOutOfPaper() {
                status.postValue(new PrtStatus(STATUS_OUT_OF_PAPER));
            }
        });
    }

    /**
     * generate settle ticket bitmap
     */
    public static Bitmap getSettleTicket(SettleReceiptBean settleReceiptBean) {
        BitmapDraw bitmapDraw = new BitmapDraw();
        try {
            bitmapDraw.image(BitmapFactory.decodeStream(BaseApplication.getAppContext().getAssets().open(FileConst.LOGO_IMG)));
        } catch (IOException e) {
            LoggerUtils.e("decodeStream " + FileConst.LOGO_IMG + " failed!", e);
        }
        bitmapDraw.text(getString(R.string.core_receipt_merchant_title), ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_merchant_type), settleReceiptBean.getMerchantType(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_merchant_id_title), settleReceiptBean.getMid(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_terminal_id_title), settleReceiptBean.getTid(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_date_time_title), DateUtils.formatTime(settleReceiptBean.getSettleDate() + settleReceiptBean.getSettleTime())
                , PrintSize.NORMAL, false);
        if (settleReceiptBean.isSettleEqual()) {
            bitmapDraw.text(getString(R.string.core_receipt_settle_balance_title), PrintSize.NORMAL, false, Paint.Align.CENTER);
        } else {
            bitmapDraw.text(getString(R.string.core_receipt_settle_unbalance_title), PrintSize.NORMAL, false, Paint.Align.CENTER);
        }
        bitmapDraw.text(getString(R.string.core_receipt_settle_flag), PrintSize.NORMAL, true, Paint.Align.CENTER);
        bitmapDraw.text(getString(R.string.core_receipt_transaction_column), getString(R.string.core_receipt_count_column), getString(R.string.core_receipt_amount_column), PrintSize.NORMAL, false);
        List<TransactionSummary> transactionSummaries = settleReceiptBean.getTransactionSummaries();
        long totalAmt = 0;
        int totalNum = 0;
        for (TransactionSummary transactionSummary : transactionSummaries) {
            long amt = transactionSummary.getAmount();
            int num = transactionSummary.getCount();
            int settleAttr = TransUtils.getSettleAttr(transactionSummary.getTransType());
            if (settleAttr == SettleAttr.PLUS) {
                totalAmt += amt;
            } else if (settleAttr == SettleAttr.REDUCE) {
                totalAmt -= amt;
            } else {
                continue;
            }
            bitmapDraw.text(TransUtils.getName(transactionSummary.getTransType()), num + "", formatAmount(amt)
                    , PrintSize.NORMAL, false);
            totalNum += num;
        }
        bitmapDraw.text(getString(R.string.core_receipt_total), totalNum + "", formatAmount(totalAmt), PrintSize.NORMAL, false);
        bitmapDraw.text("-------------x----------------x-------------", PrintSize.LINE, false, Paint.Align.CENTER);
        return bitmapDraw.getBitmap();
    }

    public static Bitmap getSettleBracTicket(SettleReceiptBean settleReceiptBean/*, List<Record> recordsData*/) {
        BitmapDraw bitmapDraw = new BitmapDraw();
      /*  try {
            bitmapDraw.image(BitmapFactory.decodeStream(BaseApplication.getAppContext().getAssets().open(FileConst.LOGO_IMG)));
        } catch (IOException e) {
            LoggerUtils.e("decodeStream " + FileConst.LOGO_IMG + " failed!", e);
        }*/

      /*
        if (recordsData != null && !recordsData.isEmpty()) recordDef = recordsData.get(0);
        */
        boolean isShowStan = false;

        bitmapDraw = headerPart(bitmapDraw);
        //lbitmapDraw.text("SETTLEMENT REPORT", PrintSize.REPORT_TITLE, true, Paint.Align.CENTER);
        bitmapDraw.text("", PrintSize.NORMAL, true, Paint.Align.CENTER);

        Merchant merchant = new Merchant();
        merchant.setMerchantName(settleReceiptBean.getMerchantName());
        merchant.setMid(settleReceiptBean.getMid());
        merchant.setTid(settleReceiptBean.getTid());
        merchant.setSettleDate(settleReceiptBean.getSettleDate());
        merchant.setSettleTime(settleReceiptBean.getSettleTime());
        merchant.setBatchNo(settleReceiptBean.getBatch());
        bitmapDraw = bracSettleMerchantDetailsPart(bitmapDraw, merchant, isShowStan);

        /*
        bitmapDraw.text(getString(R.string.core_receipt_merchant_title), ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_merchant_type), settleReceiptBean.getMerchantType(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_merchant_id_title), settleReceiptBean.getMid(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_terminal_id_title), settleReceiptBean.getTid(), PrintSize.NORMAL, false);
        bitmapDraw.text(getString(R.string.core_receipt_date_time_title), DateUtils.formatTime(settleReceiptBean.getSettleDate() + settleReceiptBean.getSettleTime())
                , PrintSize.NORMAL, false);
        */

        bitmapDraw.text("", PrintSize.SMALL, false, Paint.Align.CENTER);

        bitmapDraw.text(settleReceiptBean.getPrintStartTitle(), PrintSize.REPORT_TITLE, true, Paint.Align.CENTER);

        bitmapDraw.text(getString(R.string.core_receipt_transaction_column), getString(R.string.core_receipt_count_column), getString(R.string.core_receipt_amount_column), PrintSize.NORMAL, false);
        List<TransactionSummary> transactionSummaries = settleReceiptBean.getTransactionSummaries();
        long totalAmt = 0;
        int totalNum = 0;

        long voidTotalAmount = 0;
        int voidTotalNum = 0;

        for (TransactionSummary transactionSummary : transactionSummaries) {
            long amt = transactionSummary.getAmount();
            int num = transactionSummary.getCount();
            int settleAttr = TransUtils.getSettleAttr(transactionSummary.getTransType());
            if (settleAttr == SettleAttr.PLUS) {
                totalAmt += amt;
            } else if (settleAttr == SettleAttr.REDUCE) {
                totalAmt -= amt;
            } else {
                continue;
            }

            String voidTitle = transactionSummary.getTransType();
            String useNegative = "";

            if (voidTitle != null && voidTitle.toLowerCase().contains("void"))
                useNegative = "-";

            if (transactionSummary.getTransType().equalsIgnoreCase(TransType.TRANS_VOID_SALE)) {
                voidTotalAmount += amt;
                voidTotalNum += num;
            }

            bitmapDraw.text(TransUtils.getName(transactionSummary.getTransType()), num + "", useNegative + formatAmount(amt)
                    , PrintSize.NORMAL, false);
            // doted ------
            bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);
            //Add tip line here, If Want
            /*bitmapDraw.text("TIP", num + "", useNegative+formatAmount(amt)
                    , PrintSize.NORMAL, false);*/

            totalNum += num;
        }//end for

        //TOTAL
        int netTotal = totalNum - voidTotalNum;
        if (netTotal < 0) netTotal = netTotal * -1;
        bitmapDraw.text(getString(R.string.core_receipt_total), netTotal + "", formatAmount(totalAmt + voidTotalAmount), PrintSize.NORMAL, false);
        // doted ------
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);
        bitmapDraw.text("SETTLEMENT SUCCESSFUL", PrintSize.CARD_SCHEME_TITLE, true, Paint.Align.CENTER);

        /*bitmapDraw.text(STER_DOTED_SEPARATOR, PrintSize.CARD_SCHEME_TITLE, true, Paint.Align.CENTER);
        bitmapDraw.text(
                "AppIn Release  : ",
                "22/10/2024",
                PrintSize.REPORT_TITLE, true);
        bitmapDraw.text(STER_DOTED_SEPARATOR, PrintSize.CARD_SCHEME_TITLE, true, Paint.Align.CENTER);*/

        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);
        bitmapDraw.text("", PrintSize.SMALL, true, Paint.Align.CENTER);
        bitmapDraw.text("", PrintSize.SMALL, true, Paint.Align.CENTER);
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);

        // Card report
        //bitmapDraw.text("TRANSECTION REPORT", PrintSize.CARD_SCHEME_TITLE, true, Paint.Align.CENTER);
        //bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);

        /*
        CardSchemeReportModel grandTotalReportModel = new CardSchemeReportModel();
        for(CardSchemeReportModel cardSchemeReportModel :schemeList) {
            if(cardSchemeReportModel.getScheme().equalsIgnoreCase("Grand Total"))
                grandTotalReportModel = cardSchemeReportModel;
            else
                bitmapDraw.image(getBracAllCardReportDetails(cardSchemeReportModel));
        }
        bitmapDraw.image(getBracAllCardReportDetails(grandTotalReportModel));
        */

        List<SchemeGroup> schemeGroupList = settleReceiptBean.getSchemeGroupList();
        for (SchemeGroup schemeGroup : schemeGroupList) {
            bitmapDraw.image(getBracAllCardReportDetails2(schemeGroup));
        }

        bitmapDraw.text("", PrintSize.NORMAL, true, Paint.Align.LEFT);

        bitmapDraw.text(settleReceiptBean.getPrintEndTitle(), PrintSize.CARD_SCHEME_TITLE, true, Paint.Align.CENTER);
        bitmapDraw.text("", PrintSize.SMALL, true, Paint.Align.LEFT);


        bitmapDraw.text("-------------x----------------x-------------", PrintSize.LINE, false, Paint.Align.CENTER);
        return bitmapDraw.getBitmap();
    }

    private final Bitmap RECEIPT_END = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

    /**
     * print all transaction record detail
     */
    public void printDetail() {
        prompt.postValue(R.string.core_print_progress_detail);
        status.postValue(new PrtStatus(STATUS_READY));
        BlockingQueue<Bitmap> queue = new LinkedBlockingQueue<>();
        ThreadPool.execute(() -> offerDetailBitmap(queue));
        ThreadPool.execute(() -> printDetailQueue(queue));
    }

    public void printAllDetails(String topReportTitle, String bottomReportTitle, String reportCode, List<Record> recordsData, List<CardSchemeReportModel> schemeList) {
        prompt.postValue(R.string.core_print_progress_detail);
        status.postValue(new PrtStatus(STATUS_READY));
        Bitmap bitmap = getBracAllDetails(topReportTitle, bottomReportTitle, reportCode, recordsData, schemeList);
        receipt.postValue(bitmap);
        printer.print(bitmap, true, new IPrinter.PrintCallback() {
            @Override
            public void onFinish() {
                //finish
                if (supportCut) {
                    printer.cutPaper();
                }
                status.postValue(new PrtStatus(STATUS_SUCCESS));
            }

            @Override
            public void onError(String message) {
                status.postValue(new PrtStatus(STATUS_ERROR, message));
            }

            @Override
            public void onOutOfPaper() {
                status.postValue(new PrtStatus(STATUS_OUT_OF_PAPER));
            }
        });

//        BlockingQueue<Bitmap> queue = new LinkedBlockingQueue<>();
//        ThreadPool.execute(() -> offerDetailBitmap(queue));
//        ThreadPool.execute(() -> printDetailQueue(queue));
    }

    public void printAllDetailsReport2(String topReportTitle, String bottomReportTitle, String reportCode, List<Record> recordsData, List<SchemeGroup> schemeList) {
        prompt.postValue(R.string.core_print_progress_detail);
        status.postValue(new PrtStatus(STATUS_READY));
        Bitmap bitmap = getBracAllDetails2(topReportTitle, bottomReportTitle, reportCode, recordsData, schemeList);
        receipt.postValue(bitmap);
        printer.print(bitmap, true, new IPrinter.PrintCallback() {
            @Override
            public void onFinish() {
                //finish
                if (supportCut) {
                    printer.cutPaper();
                }
                status.postValue(new PrtStatus(STATUS_SUCCESS));
            }

            @Override
            public void onError(String message) {
                status.postValue(new PrtStatus(STATUS_ERROR, message));
            }

            @Override
            public void onOutOfPaper() {
                status.postValue(new PrtStatus(STATUS_OUT_OF_PAPER));
            }
        });

//        BlockingQueue<Bitmap> queue = new LinkedBlockingQueue<>();
//        ThreadPool.execute(() -> offerDetailBitmap(queue));
//        ThreadPool.execute(() -> printDetailQueue(queue));
    }

    /**
     * detail bitmap
     */
    private void offerDetailBitmap(Queue<Bitmap> queue) {
        queue.clear();
        RecordRepository recordRepository = new RecordRepository();
        BitmapDraw bitmapDraw = new BitmapDraw();
        bitmapDraw.text(getString(R.string.core_receipt_detail), PrintSize.NORMAL, false, Paint.Align.CENTER);
        bitmapDraw.text(getString(R.string.core_receipt_transaction_column), getString(R.string.core_receipt_card_column), getString(R.string.core_receipt_amount_column), getString(R.string.core_receipt_trace_column), PrintSize.SMALL, false);
        // print all records
        int count = recordRepository.getCount();
        for (int index = 0; index < count; index++) {
            if (queue.peek() == RECEIPT_END) {
                //'printQueue()'(maybe failed) close the queue
                return;
            }
            Record record = recordRepository.findByIndex(index);
            if (record == null) {
                continue;
            }
            //add item
            String transType = TransUtils.getName(record.getTransType());
            String pan = FormatUtils.maskCardNo(record.getCardNo());
            if (pan.length() > 7) {
                pan = pan.substring(pan.length() - 7);
            }
            String amount = CurrencyUtils.getCurrencySymbol(record.getCurrencyCode()) + formatAmount(record.getAmount());
            String traceNo = record.getTraceNo();
            int[] percents = new int[]{25, 25, 25, 25};
            String[] texts = new String[]{transType, pan, amount, traceNo};
            Paint.Align[] aligns = new Paint.Align[]{Paint.Align.LEFT, Paint.Align.CENTER, Paint.Align.RIGHT, Paint.Align.RIGHT};
            float[] textSizes = new float[]{PrintSize.SMALL, PrintSize.SMALL, PrintSize.SMALL, PrintSize.SMALL};
            boolean[] bolds = new boolean[]{false, false, false, false};
            bitmapDraw.textMulti(percents, texts, aligns, textSizes, bolds);
            if (index > 0 && index % 60 == 0) {
                //print data
                queue.offer(bitmapDraw.getBitmap());
                bitmapDraw = new BitmapDraw();
            }
        }
        bitmapDraw.text("-------------x----------------x-------------", PrintSize.LINE, false, Paint.Align.CENTER);
        queue.offer(bitmapDraw.getBitmap());
        queue.offer(RECEIPT_END);

    }

    /**
     * detail printing queue
     */
    private void printDetailQueue(BlockingQueue<Bitmap> queue) {
        try {
            Bitmap bitmap = queue.take();
            if (bitmap == RECEIPT_END) {
                //finish
                if (supportCut) {
                    printer.cutPaper();
                }
                status.postValue(new PrtStatus(STATUS_SUCCESS));
                return;
            }
            final Bitmap finalBitmap = bitmap;
            ThreadPool.postOnMain(() -> receipt.setValue(finalBitmap));
            boolean autoFeed = queue.peek() == RECEIPT_END;
            //print
            printer.print(bitmap, autoFeed, new IPrinter.PrintCallback() {
                @Override
                public void onFinish() {
                    printDetailQueue(queue);
                }

                @Override
                public void onError(String message) {
                    queue.offer(RECEIPT_END);
                    status.postValue(new PrtStatus(STATUS_ERROR, message));
                }

                @Override
                public void onOutOfPaper() {
                    queue.offer(RECEIPT_END);
                    status.postValue(new PrtStatus(STATUS_OUT_OF_PAPER));
                }
            });
        } catch (InterruptedException e) {
            LoggerUtils.e("print detail queue interrupted.", e);
            queue.offer(RECEIPT_END);
            status.postValue(new PrtStatus(STATUS_ERROR, e.getMessage()));
        }
    }

    private static String getString(@StringRes int resId, Object... formatArgs) {
        return BaseApplication.getAppString(resId, formatArgs);
    }

    public void printBracAboutInfo(List<DeviceItem> mAboutInfpItems) {
        status.postValue(new PrtStatus(STATUS_READY));
        prompt.postValue(R.string.core_print_about_info);
        //settlement receipt path
        Bitmap bitmap = getAboutInfoBracTicket( mAboutInfpItems);
        receipt.postValue(bitmap);
        printer.print(bitmap, true, new IPrinter.PrintCallback() {
            @Override
            public void onFinish() {
                //finish
                if (supportCut) {
                    printer.cutPaper();
                }
                status.postValue(new PrtStatus(STATUS_SUCCESS));
            }

            @Override
            public void onError(String message) {
                status.postValue(new PrtStatus(STATUS_ERROR, message));
            }

            @Override
            public void onOutOfPaper() {
                status.postValue(new PrtStatus(STATUS_OUT_OF_PAPER));
            }
        });
    }

    private Bitmap getAboutInfoBracTicket( List<DeviceItem> mAboutInfpItems) {
        BitmapDraw bitmapDraw = new BitmapDraw();

       // bitmapDraw = headerPart(bitmapDraw);

        bitmapDraw.text("APPLICATION INFORMATION", PrintSize.CARD_SCHEME_TITLE, true, Paint.Align.CENTER);
        // doted ------
        bitmapDraw.text(LINE_DOTED_SEPARATOR, PrintSize.NORMAL, true, Paint.Align.CENTER);

        for (int i = 0; i < mAboutInfpItems.size(); i++) {
            int titleId = mAboutInfpItems.get(i).getTitle();
            if (titleId == 0) {
                bitmapDraw.text(mAboutInfpItems.get(i).getSTitle(), mAboutInfpItems.get(i).getContent(), PrintSize.NORMAL, false);
            } else {
                bitmapDraw.text(getString(mAboutInfpItems.get(i).getTitle()), mAboutInfpItems.get(i).getContent(), PrintSize.NORMAL, false);
            }
        }

        // doted ------
        bitmapDraw.text("-------------x----------------x-------------", PrintSize.LINE, false, Paint.Align.CENTER);

        return bitmapDraw.getBitmap();
    }

    public static class PrtStatus {
        private final int status;
        private String message;

        public PrtStatus(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public PrtStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
