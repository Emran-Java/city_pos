package acquire.core.tools;

import androidx.annotation.NonNull;

import acquire.base.utils.LoggerUtils;
import acquire.core.config.Param;
import acquire.core.config.TransFactory;
import acquire.core.constant.SettleAttr;
import acquire.core.constant.TransType;
import acquire.core.trans.AbstractTrans;

/**
 * Transaction params utils by {@link TransFactory}
 *
 * @author Janson
 * @date 2018/10/26 17:33
 */
public class TransUtils {

    private static final TransFactory TRANS_FACTORY = new TransFactory();


    /**
     * Get transaction params key by {@link TransType}
     */
    public static String getParamsKey(String transType) {
        return getParam(transType).getSwitcher();
    }

    /**
     * Get transaction name by {@link TransType}
     */
    public static String getName(String transType) {
        Param param = getParam(transType);
        return param.getName();
    }

    /**
     * Get settle attr by {@link TransType}
     *
     * @return {@link SettleAttr}
     */
    public static int getSettleAttr(String transType) {
        Param param = getParam(transType);
        return param.getSettleAttr();
    }

    /**
     * Get {@link AbstractTrans} by {@link TransType}
     */
    public static Class<? extends AbstractTrans> getTrans(String transType) {
        return getParam(transType).getTrans();
    }


    /**
     * Get transaction configuration params by {@link TransType}
     */
    @NonNull
    private static Param getParam(String transType) {
        Param param = TRANS_FACTORY.getParam(transType);
        if (param == null) {
            LoggerUtils.e("There is not transaction[transType= "+transType+"] in TransFactory.");
            return new Param.Builder(null).create();
        }
        return param;
    }

}
