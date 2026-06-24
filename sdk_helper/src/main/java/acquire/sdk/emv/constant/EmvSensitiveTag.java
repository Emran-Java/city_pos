package acquire.sdk.emv.constant;

import com.newland.sdk.emvl3.api.common.EmvL3Const;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import acquire.base.utils.emv.EmvTag;

/**
 * @author Janson
 * @since 2025/9/16
 */
public class EmvSensitiveTag {
    public static final Set<Integer> SENSITIVE_EMV_TAG = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            EmvTag.TAG_5A_IC_PAN,
            EmvTag.TAG_57_IC_TRACK2EQUDATA,
            EmvTag.TAG_9F20_IC_TRACK2DATA,
            EmvTag.TAG_9F1F_IC_TRACK1DATA,
            EmvTag.TAG_5F24_IC_APPEXPIREDATE
    )));
    public static final Set<Integer> SENSITIVE_EMV_L3_TAG = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            EmvL3Const.L3_DATA.PAN,
            EmvL3Const.L3_DATA.TRACK1,
            EmvL3Const.L3_DATA.TRACK2,
            EmvL3Const.L3_DATA.TRACK3,
            EmvL3Const.L3_DATA.DD_CARD_TRACK1,
            EmvL3Const.L3_DATA.DD_CARD_TRACK2,
            EmvL3Const.L3_DATA.EXPIRE_DATE,
            EmvL3Const.L3_DATA.SERVICE_CODE,
            EmvL3Const.L3_DATA.CARDHOLDER_NAME
    )));
}
