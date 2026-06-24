package acquire.core.trans.pack;

import android.text.TextUtils;

import java.util.List;
import java.util.Locale;

import acquire.core.bean.PubBean;
import acquire.core.constant.SettleAttr;
import acquire.core.tools.TransUtils;
import acquire.database.bean.TransactionSummary;
import acquire.database.repository.RecordRepository;
import acquire.sdk.emv.constant.EntryMode;

/**
 * @author Janson
 * @date 2024/4/12 11:36
 */
public class PackField {
    public static String packField22(PubBean pubBean) {
        String panEntryMode, pinFlag;
        switch (pubBean.getEntryMode()) {
            case EntryMode.MAG:
                // magnetic
                panEntryMode = "02";
                break;
            case EntryMode.INSERT:
                // insert
                panEntryMode = "05";
                break;
            case EntryMode.TAP:
                // tap
                panEntryMode = "07";
                break;
            case EntryMode.MANUAL:
                // manual
                panEntryMode = "01";
                break;
            default:
                // unspecified
                panEntryMode = "00";
                break;
        }

        String pinBlock = pubBean.getPinBlock();
        if (TextUtils.isEmpty(pinBlock)) {
            // no PIN
            //Date: 20260512 2340: We set 1 because consider PIN capable this device (N910S and above)
            //pinFlag = "2";
            pinFlag = "1";
        } else {
            //has PIN
            pinFlag = "1";
        }
        return panEntryMode + pinFlag;
    }

    public static String packField63(String mid, String tid) {
        long[]numAmt = packField63Detail(mid,tid);
        long debitNum = numAmt[0];
        long debitAmount = numAmt[1];
        long creditNum = numAmt[2];
        long creditAmount = numAmt[3];
        return
                String.format(Locale.getDefault(), "%03d", debitNum)
                +String.format(Locale.getDefault(), "%012d", debitAmount)
                //+ String.format(Locale.getDefault(), "%012d", creditAmount)
                //+ String.format(Locale.getDefault(), "%03d", creditNum)
                ;
    }

    /**
     * return [debitNum,debitAmount,creditNum,creditAmount]
     */
    public static long[] packField63Detail(String mid, String tid) {
        long debitNum = 0;
        long debitAmount = 0;
        long creditNum = 0;
        long creditAmount = 0;
        List<TransactionSummary> transactionSummaries= new RecordRepository().getTransactionSummary(mid,tid);
        for (TransactionSummary transactionSummary : transactionSummaries) {
            if (transactionSummary.getCount() == 0) {
                continue;
            }
            int settleAttr = TransUtils.getSettleAttr(transactionSummary.getTransType());
            if (settleAttr == SettleAttr.PLUS) {
                debitAmount += transactionSummary.getAmount();
                debitNum += transactionSummary.getCount();
            } else if (settleAttr == SettleAttr.REDUCE) {
                creditAmount += transactionSummary.getAmount();
                creditNum += transactionSummary.getCount();
            }
        }
        return new long[]{debitNum,debitAmount,creditNum,creditAmount};
    }
}
