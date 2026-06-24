package acquire.core.fragment.receipt;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import acquire.base.BaseApplication;
import acquire.base.utils.ParamsUtils;
import acquire.core.R;
import acquire.core.constant.ParamsConst;
import acquire.sdk.device.BDevice;

public class ReceiptTypeItem {

    private final int receiptType;
    private final String name;
    private final @DrawableRes int icon;

    public ReceiptTypeItem(int receiptType, @StringRes int nameResId, @DrawableRes int icon) {
        this.receiptType = receiptType;
        this.name = BaseApplication.getAppString(nameResId);
        this.icon = icon;
    }

    public int getReceiptType() {
        return receiptType;
    }

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    public static final int MERCHANT_PAPER = 1;
    public static final int CUSTOMER_PAPER = 2;
    public static final int FLY_RECEIPT = 3;
    public static final int E_RECEIPT_NPI_DEMO = 4;
    public static final int E_RECEIPT_PIXCEL = 5;

    public static List<ReceiptTypeItem> getReceiptTypeList() {
        List<ReceiptTypeItem> list = new ArrayList<>();
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_RECEIPT_MERCHANT_PAPER) && (BDevice.supportPrint() || ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL))) {
            list.add(new ReceiptTypeItem(MERCHANT_PAPER, R.string.core_receipt_merchant_paper, R.drawable.core_receipt_type_merchant_paper));
        }
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_RECEIPT_CUSTOMER_PAPER) && (BDevice.supportPrint() || ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL))) {
            list.add(new ReceiptTypeItem(CUSTOMER_PAPER, R.string.core_receipt_customer_paper, R.drawable.core_receipt_type_customer_paper));
        }
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_RECEIPT_FLY_RECEIPT)) {
            list.add(new ReceiptTypeItem(FLY_RECEIPT, R.string.core_receipt_fly_receipt, R.drawable.core_receipt_type_fly_receipt));
        }
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_RECEIPT_E_NPI_DEMO) && BDevice.supportHCE()) {
            list.add(new ReceiptTypeItem(E_RECEIPT_NPI_DEMO, R.string.core_receipt_e_npidemo, R.drawable.core_receipt_type_e_npi_demo));
        }
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_RECEIPT_E_PIXCEL) && BDevice.supportHCE()) {
            list.add(new ReceiptTypeItem(E_RECEIPT_PIXCEL, R.string.core_receipt_e_pixcel, R.drawable.core_receipt_type_e_pixcel));
        }

        return list;
    }


}
