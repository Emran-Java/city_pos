package acquire.core.constant;


import androidx.annotation.IntDef;
import androidx.annotation.Keep;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The settle attribute that indicates whether the transaction should be added to settlement total
 *
 * @author Janson
 * @date 2019/8/5 10:21
 */
@Keep
public class SettleAttr {
    /**
     * Nothing to do with settlement
     */
    public final static int NONE = 0;
    /**
     * Add it to the settlement total
     */
    public final static int PLUS = 1;
    /**
     * Subtract it from the settlement total
     */
    public final static int REDUCE = -1;

    @IntDef(value = {NONE,PLUS,REDUCE})
    @Target({ElementType.PARAMETER,ElementType.METHOD,ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SettleAttrDef{}

}
