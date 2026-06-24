package acquire.core.constant;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Password type
 *
 *
 * @author Janson
 * @date 2022/9/14 14:02
 */
public class PasswordType {
    /**
     * used when inputting important information
     */
    public final static int SECURITY = 0;
    /**
     * used when system admin logins.
     */
    public final static int SYSTEM_ADMIN = 1;
    /**
     * used when doing some cancelled transaction
     */
    public final static int ADMIN = 2;


    @IntDef(value = {SECURITY, SYSTEM_ADMIN, ADMIN})
    @Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TypeDef {
    }
} 
