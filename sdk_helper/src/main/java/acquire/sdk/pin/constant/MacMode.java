package acquire.sdk.pin.constant;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mac algorithm
 *
 * @author Janson
 * @date 2022/7/18 10:55
 */
public class MacMode {

    /**
     * 9606
     */
    public final static int TYPE_9606 = 0;

    /**
     * X99
     */
    public final static int TYPE_X99 = 1;

    /**
     * X919
     */
    public final static int TYPE_X919 = 2;

    /**
     * ECB
     */
    public final static int TYPE_ECB = 3;


    @IntDef(value = {TYPE_9606, TYPE_X99, TYPE_X919, TYPE_ECB})
    @Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MacModeTypeDef {
    }


}
