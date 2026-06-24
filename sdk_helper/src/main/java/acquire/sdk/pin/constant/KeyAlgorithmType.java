package acquire.sdk.pin.constant;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Key Type
 * @author Xulf
 * @date 2021/3/9 16:15
 */
public class KeyAlgorithmType {
    /**
     *DUKPT
     */
    public static final int DUKPT = 0x00;
    /**
     * MKSK
     */
    public static final int MKSK = 0x01;

    @IntDef(value = {DUKPT, MKSK})
    @Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AlgorithmTypeDef {
    }
}
