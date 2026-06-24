package acquire.sdk.emv.bean;

import com.newland.sdk.emvl3.api.common.EmvL3Const.UICard;

import acquire.sdk.emv.constant.EntryMode;

/**
 * Emv entry status and mode
 *
 * @author Janson
 * @date 2022/8/19 9:27
 */
public class EmvReadyBean {
    public final static int NORMAL = 0, FALLBACK = 1, USE_CHIP = 2, AGAIN = 3;
    /**
     * @see acquire.sdk.emv.constant.EntryMode
     */
    private final int supportEntries;

    /**
     * entry status. It is one of {@link #NORMAL} or {@link #FALLBACK} or {@link #USE_CHIP}
     */
    private int status;

    /**
     * EMV present card step information
     *
     * @param lastEntry  last entry mode. {@link EntryMode}
     * @param eventData0 emv data byte 0
     */
    public EmvReadyBean(int lastEntry, byte eventData0) {
        status = EmvReadyBean.NORMAL;
        switch (eventData0) {
            case UICard.UI_KEYIN:
                supportEntries = EntryMode.MANUAL;
                break;
            case UICard.UI_STRIPE:
                supportEntries = EntryMode.MAG;
                break;
            case UICard.UI_INSERT:
                supportEntries = EntryMode.INSERT;
                break;
            case UICard.UI_TAP:
                supportEntries = EntryMode.TAP;
                break;
            case UICard.UI_INSERTC_TAP:
                supportEntries = EntryMode.INSERT | EntryMode.TAP;
                break;
            case UICard.UI_STRIPE_TAP:
                supportEntries = EntryMode.MAG | EntryMode.TAP;
                break;
            case UICard.UI_STRIPE_INSERT:
                supportEntries = EntryMode.MAG | EntryMode.INSERT;
                break;
            case UICard.UI_STRIPE_INSERT_TAP:
                supportEntries = EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP;
                break;
            case UICard.UI_STRIPE_INSERT_TAP_MANUAL:
                supportEntries = EntryMode.MAG | EntryMode.INSERT | EntryMode.TAP | EntryMode.MANUAL;
                break;
            case UICard.UI_PRESENTCARD_AGAIN:
                supportEntries = lastEntry;
                status = AGAIN;
                break;
            case UICard.UI_USE_CHIP:
                supportEntries = lastEntry & ~EntryMode.MAG;
                status = EmvReadyBean.USE_CHIP;
                break;
            case UICard.UI_FALLBACK_CT:
                supportEntries = EntryMode.MAG;
                status = EmvReadyBean.FALLBACK;
                break;
            case UICard.UI_FALLBACK_CLSS:
                supportEntries = EntryMode.MAG | EntryMode.INSERT;
                status = EmvReadyBean.FALLBACK;
                break;
            case UICard.UI_STRIPE_INSERT_MANUAL:
                supportEntries = EntryMode.MAG | EntryMode.INSERT | EntryMode.MANUAL;
                break;
            case UICard.UI_STRIPE_TAP_MANUAL:
                supportEntries = EntryMode.MAG | EntryMode.TAP | EntryMode.MANUAL;
                break;
            case UICard.UI_INSERT_TAP_MANUAL:
                supportEntries = EntryMode.INSERT | EntryMode.TAP | EntryMode.MANUAL;
                break;
            case UICard.UI_STRIPE_MANUAL:
                supportEntries = EntryMode.MAG | EntryMode.MANUAL;
                break;
            case UICard.UI_INSERT_MANUAL:
                supportEntries = EntryMode.INSERT | EntryMode.MANUAL;
                break;
            case UICard.UI_TAP_MANUAL:
                supportEntries = EntryMode.TAP | EntryMode.MANUAL;
                break;
            case UICard.UI_SWIPE_AGAIN:
                supportEntries = lastEntry;
                status = AGAIN;
                break;
            default:
                supportEntries = 0;
                break;
        }
    }


    public int getSupportEntries() {
        return supportEntries;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "EmvReadyBean{" +
                "supportEntries=" + supportEntries +
                ", status=" + status +
                '}';
    }
}
