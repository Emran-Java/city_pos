package acquire.sdk.emv.bean;


import androidx.annotation.NonNull;

import com.newland.nsdk.core.api.internal.emvl2.type.EmvConfig;

import java.util.Date;
import java.util.Locale;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.DateUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.TlvUtils;
import acquire.sdk.emv.constant.EmvTransType;
import acquire.sdk.emv.constant.EntryMode;

/**
 * emv launch parameters
 *
 * @author Janson
 * @date 2019/10/21 9:24
 */
public class EmvLaunchParam {
    /**
     * entry type.
     *
     * @see EntryMode
     */
    private int entryMode;

    private int timeoutSec;

    private byte[] transData;

    private EmvLaunchParam() {
    }
    /**
     * Get card type
     *
     * @return EMV card type.
     * @see EntryMode
     */
    public int getEntryMode() {
        return entryMode;
    }

    public int getTimeoutSec() {
        return timeoutSec;
    }

    public byte[] getTransData() {
        return transData;
    }


    @NonNull
    @Override
    public String toString() {
        return "EmvLaunchParam{" +
                "entryMode=" + entryMode +
                ", timeoutSec=" + timeoutSec +
                ", transData=" + BytesUtils.bcdToString(transData) +
                '}';
    }

    public static class Builder {
        private int entryMode;
        private long amount;
        private long cashAmount;
        private long unionPayQpsLimitAmount;
        private boolean forceOnline;
        private final int emvTransType;

        private int retryTimesToFallback;

        private int timeoutSec = 60;

        public Builder(int emvTransType) {
            this.emvTransType = emvTransType;
        }

        /**
         * set time out
         *
         * @return <code>this</code>
         */
        public Builder timeout(int timeoutSec) {
            this.timeoutSec = timeoutSec;
            return this;
        }

        /**
         * set card type
         *
         * @return <code>this</code>
         * @see EntryMode
         */
        public Builder entryMode(int entryMode) {
            this.entryMode = entryMode;
            return this;
        }

        /**
         * set money
         *
         * @param amount money
         * @return <code>this</code>
         */
        public Builder amount(long amount) {
            this.amount = amount;
            return this;
        }

        /**
         * set cash money
         *
         * @param cashAmount cash money
         * @return <code>this</code>
         */
        public Builder cashAmount(long cashAmount) {
            this.cashAmount = cashAmount;
            return this;
        }

        /**
         * set online
         *
         * @param forceOnline true online ; false offline
         * @return <code>this</code>
         */
        public Builder forceOnline(boolean forceOnline) {
            this.forceOnline = forceOnline;
            return this;
        }

        /**
         * set UnionPay qps limit Amount (only valid for china UnionPay)
         *
         * @param unionQpsLimitAmount money
         * @return <code>this</code>
         */
        public Builder unionPayQpsLimitAmount(long unionQpsLimitAmount) {
            this.unionPayQpsLimitAmount = unionQpsLimitAmount;
            return this;
        }

        public Builder retryTimesToFallback(int retryTimes){
            this.retryTimesToFallback = retryTimes;
            return this;
        }
        /**
         * create EMV param bean
         *
         * @return EMV param {@link EmvLaunchParam}
         */
        public EmvLaunchParam create() {
            LoggerUtils.d(toString());
            EmvLaunchParam emvLaunchParam = new EmvLaunchParam();
            emvLaunchParam.entryMode = entryMode;
            emvLaunchParam.timeoutSec = timeoutSec;
            TlvUtils.PackTlv pack = TlvUtils.newPackTlv();
            //Amount (total amount )
            String amountStr = String.format(Locale.US, "%012d", amount);
            pack.append(EmvConfig._EMVPARAM_9F02_AUTHAMNTN, BytesUtils.hexToBytes(amountStr));
            if (cashAmount > 0) {
                //cash
                String cashAmountStr = String.format(Locale.US, "%012d", cashAmount);
                pack.append(EmvConfig._EMVPARAM_9F03_OTHERAMNTN, BytesUtils.hexToBytes(cashAmountStr));
            }
            //Transaction type
            pack.append(EmvConfig._EMVPARAM_9C_TRANSTYPE, new byte[]{(byte) emvTransType});
            if (emvTransType == EmvTransType.REFUND || emvTransType == EmvTransType.VOID) {
                //Simple process
                pack.append(0x1F8128, new byte[]{1});
            }
            //Date
            String strDate = DateUtils.formatTime(new Date(),DateUtils.YYMMDD);
            pack.append(EmvConfig._EMVPARAM_9A_TRANSDATE, BytesUtils.hexToBytes(strDate));
            //Time
            String strTime = DateUtils.formatTime(new Date(),DateUtils.HHMMSS);
            pack.append(EmvConfig._EMVPARAM_9F21_TRANSTIME, BytesUtils.hexToBytes(strTime));
            if (forceOnline) {
                //Force online request
                pack.append(0x1F8126, new byte[]{1});
            } else {
                //Card devices online/offline request
                pack.append(0x1F8126, new byte[]{0});
            }
            /*
             * Only valid on the external card reader
             * make the UI(card confirm,aid selection) show on the external card reader
             */
//            pack.append(0x1F8139, new byte[]{(byte) 0xFF, 0x00, 0x00});
            pack.append(0x1F8139, new byte[]{(byte) 0x08, 0x00, 0x00});
            pack.append(0x1F8130, new byte[]{0x03});
            /*
             * fallback configure
             */
            pack.append(0x1F8122, new byte[]{0x00, (byte) 0x9F, 0x00});
            /*
             * UL JCB case 340 02a.
             * Contactless to Contact or MSR Fallback when transaction amount exceed contactless limit amount
             */
            pack.append(0x1F8123, new byte[]{(byte)0x80});
            //UnionPay QPS limit amount(free pin/sign)
            if (unionPayQpsLimitAmount >0) {
                String unionPayQpsStr = String.format(Locale.US, "%012d", unionPayQpsLimitAmount);
                pack.append(0x1F8124, BytesUtils.hexToBytes(unionPayQpsStr));
                //e.g. pack.append(0x1F8124, new byte[]{ 0x00, 0x00, 0x00, 0x10, 0x00, 0x00});
            }
            //Retry Times before Fallback
            pack.append(0x1F8127, new byte[]{(byte) retryTimesToFallback});
            emvLaunchParam.transData = pack.pack();
            return emvLaunchParam;
        }

        @NonNull
        @Override
        public String toString() {
            return "Builder{" +
                    "entryMode=" + entryMode +
                    ", amount=" + amount +
                    ", cashAmount=" + cashAmount +
                    ", unionPayQpsLimitAmount=" + unionPayQpsLimitAmount +
                    ", forceOnline=" + forceOnline +
                    ", emvTransType=" + emvTransType +
                    ", retryTimesToFallback=" + retryTimesToFallback +
                    ", timeoutSec=" + timeoutSec +
                    '}';
        }
    }
}
