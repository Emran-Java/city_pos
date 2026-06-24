package acquire.core.config;


import androidx.annotation.StringRes;

import acquire.base.BaseApplication;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.SettleAttr;
import acquire.core.constant.SettleAttr.SettleAttrDef;
import acquire.core.trans.AbstractTrans;

/**
 * Transaction configuration parameters
 *
 * @author Janson
 * @date 2019/1/16 11:33
 */
public class Param {
    /**
     * Settle attribute
     * @see SettleAttr
     */
    private @SettleAttrDef int settleAttr;
    /**
     * Transaction name
     */
    private String name;
    /**
     * Transaction supported key
     */
    private String switcher;
    /**
     * Transaction entity
     */
    private Class<? extends AbstractTrans> trans;

    private Param() {
    }

    public int getSettleAttr() {
        return settleAttr;
    }


    public String getName() {
        return name;
    }


    public String getSwitcher() {
        return switcher;
    }

    public Class<? extends AbstractTrans> getTrans() {
        return trans;
    }


    public static class Builder {
        private int settleAttr;
        private String name;
        private String switcher;
        private final Class<? extends AbstractTrans> trans;

        public Builder(Class<? extends AbstractTrans> trans) {
            this.trans = trans;
        }

        /**
         * Set the settle attribute.
         */
       public Builder settleAttr(@SettleAttrDef int settleAttr) {
            this.settleAttr = settleAttr;
            return this;
        }

        /**
         * Set transaction name
         */
        public Builder name(@StringRes int textResId) {
            this.name = BaseApplication.getAppString(textResId);
            return this;
        }


        /**
         * Set transaction {@link ParamsConst}
         */
        public Builder switcher(String switcher) {
            this.switcher = switcher;
            return this;
        }

        /**
         * Create a {@link Param} with the arguments supplied to this builder.
         */
        public Param create() {
            Param param = new Param();
            param.switcher = switcher;
            param.name = name;
            param.settleAttr = settleAttr;
            param.trans = trans;
            return param;
        }

    }

}
