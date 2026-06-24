package acquire.core.constant;

import java.lang.reflect.Field;

import acquire.base.utils.LoggerUtils;
import acquire.core.BindTag;

/**
 * Transaction status
 *
 * @author Janson
 * @date 2021/7/8 16:16
 */
public class TransStatus {
    /**
     * Normal
     */
    @BindTag("SUCCESS")
    public static final int SUCCESS = 0;
    /**
     * Cancelled
     */
    @BindTag("CANCELLED")
    public static final int CANCELLED = 1;
    /**
     * Refunded
     */
    @BindTag("REFUNDED")
    public static final int REFUNDED = 2;
    /**
     * Auth-completion
     */
    @BindTag("COMPLETED")
    public static final int COMPLETED = 3;

    public static String getDescription(int status){
        Field[] fields = TransStatus.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.getInt(TransStatus.class) == status){
                    BindTag description = field.getAnnotation(BindTag.class);
                    if (description != null){
                        return description.value();
                    }
                }
            } catch (IllegalAccessException e) {
                LoggerUtils.e("getDescription "+status+" failed!",e);
            }
        }
        return "";
    }
}
