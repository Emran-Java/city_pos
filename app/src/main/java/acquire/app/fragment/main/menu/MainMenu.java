package acquire.app.fragment.main.menu;


import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.zztl.pos.ucb.R;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.TransType;
import acquire.core.fragment.receipt.ReceiptProvider;
import acquire.core.tools.TransUtils;
import acquire.sdk.device.BDevice;

/**
 * Main Menu Configuration
 *
 * @author Janson
 * @date 2020/6/8 10:19
 */
public class MainMenu {
    /**
     * menu items
     */
    private List<MenuItem> menu = new ArrayList<>();
    /**
     * {@link ParamsConst} that indicates whether to support this item.
     */
    private final Map<String, Boolean> mLastParamsMap = new ArrayMap<>();
    private boolean lastSupportPrinter;
    private boolean lastSupportReceipt;
    private static volatile MainMenu instance;
    private Boolean isChangeMainMenu = false;

    private MainMenu() {
    }

    public static MainMenu getInstance() {
        if (instance == null) {
            synchronized (MainMenu.class) {
                if (instance == null) {
                    instance = new MainMenu();
                }
            }
        }
        return instance;
    }

    /**
     * get items
     */
    private List<MenuItem> getMainFeatureItems() {
        List<MenuItem> items = new ArrayList<>();

        String paraSaleVal = ParamsUtils.getString(ParamsConst.PARAMS_KEY_SALE, "0");
        LoggerUtils.d("ParamKey: : "+paraSaleVal );

        //1,2,3
        if (ParamsUtils.getString(ParamsConst.PARAMS_KEY_SALE, "0").equals("1"))
            items.add(new MenuItem(TransType.TRANS_SALE, R.drawable.ic_sale, R.color.app_menu_light_white_background));
        if (ParamsUtils.getString(ParamsConst.PARAMS_KEY_CASH_PAYFLEX, "0").equals("1"))
            items.add(new MenuItem(TransType.TRANS_INSTALLMENT_MENU, R.drawable.ic_app_payflex, R.color.app_menu_light_white_background));
        if (ParamsUtils.getString(ParamsConst.PARAMS_KEY_VOID, "0").equals("1"))
            items.add(new MenuItem(TransType.TRANS_VOID_SALE, R.drawable.ic_app_void, R.color.app_menu_light_white_background));

        //Print
       /* if (BDevice.supportPrint() || ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
            List<MenuItem> folder = new ArrayList<>();
            if (ReceiptProvider.hasSupportedReceipts()){
                folder.add(new MenuItem(TransType.TRANS_REPRINT_LAST_RECEIPT, R.drawable.app_menu_reprint_last_receipt, R.color.app_menu_light_white_background));
            }
            folder.add(new MenuItem(TransType.TRANS_REPRINT_RECEIPT, R.drawable.app_menu_reprint_receipt, R.color.app_menu_light_white_background));
            folder.add(new MenuItem(TransType.TRANS_REPRINT_SETTLE, R.drawable.app_menu_reprint_settle, R.color.app_menu_light_white_background));
            folder.add(new MenuItem(TransType.TRANS_PRINT_DETAIL, R.drawable.app_menu_print_detail, R.color.app_menu_light_white_background));

            //4
            items.add(new MenuItem(folder, R.string.app_menu_print_folder_title, R.drawable.ic_app_reprint, R.color.app_menu_light_white_background));
        }else{
            items.add(new MenuItem(TransType.TRANS_REPRINT_RECEIPT, R.drawable.app_menu_print_folder, R.color.app_menu_light_white_background));
        }*/
        //------------------

        //4
        items.add(new MenuItem(TransType.TRANS_REPRINT_RECEIPT_MENU, R.drawable.ic_app_reprint, R.color.app_menu_light_white_background));

        //5 new add, Reports Print
        //items.add(new MenuItem(TransType.TRANS_REPRINT_RECEIPT, R.drawable.app_menu_print_folder, R.color.app_menu_light_white_background));
        //items.add(new MenuItem(TransType.TRANS_REPRINT_LAST_RECEIPT, R.drawable.app_menu_reprint_last_receipt, R.color.app_menu_light_white_background));

        //items.add(new MenuItem(TransType.TRANS_SETTLE, R.drawable.ic_app_batch, R.color.app_menu_light_white_background));
        items.add(new MenuItem(TransType.TRANS_PRINT_DETAIL, R.drawable.ic_app_report, R.color.app_menu_light_white_background));
        //-----------

        //6
        items.add(new MenuItem(TransType.TRANS_SETTLE, R.drawable.ic_app_batch, R.color.app_menu_light_white_background));

        List<MenuItem> preFolder = new ArrayList<>();
        preFolder.add(new MenuItem(TransType.TRANS_PRE_AUTH, R.drawable.ic_pre_auth, R.color.app_menu_light_white_background));
        preFolder.add(new MenuItem(TransType.TRANS_AUTH_COMPLETE, R.drawable.app_menu_auth_complete, R.color.app_menu_light_white_background));
        preFolder.add(new MenuItem(TransType.TRANS_VOID_PRE_AUTH, R.drawable.app_menu_void_pre_auth, R.color.app_menu_light_white_background));
        preFolder.add(new MenuItem(TransType.TRANS_VOID_AUTH_COMPLETE, R.drawable.app_menu_void_auth_complete, R.color.app_menu_light_white_background));

        //7
        if (ParamsUtils.getString(ParamsConst.PARAMS_KEY_PREAUTH, "0").equals("1")) {
//            items.add(new MenuItem(preFolder, R.string.app_menu_pre_auth_folder_title, R.drawable.ic_pre_auth, R.color.app_menu_light_white_background));
            items.add(new MenuItem(TransType.TRANS_PRE_AUTH_MENU, R.drawable.ic_pre_auth, R.color.app_menu_light_white_background));
        }

        //8,9 New item add
        items.add(new MenuItem(TransType.TRANS_LOG_ON, R.drawable.ic_signin, R.color.app_menu_light_white_background));
        items.add(new MenuItem(TransType.TRANS_TEST_TRX, R.drawable.ic_app_echo, R.color.app_menu_light_white_background));


        List<MenuItem> scanFolder = new ArrayList<>();
        scanFolder.add(new MenuItem(TransType.TRANS_SCAN_PAY, R.drawable.app_menu_scan_pay, R.color.app_menu_light_white_background));

        scanFolder.add(new MenuItem(TransType.TRANS_QR_CODE, R.drawable.app_menu_qr_code, R.color.app_menu_light_white_background));
        scanFolder.add(new MenuItem(TransType.TRANS_QR_REFUND, R.drawable.app_menu_qr_refund, R.color.app_menu_light_white_background));
        //items.add(new MenuItem(scanFolder,R.string.app_menu_qr_folder_title,R.drawable.app_menu_scan_pay,R.color.app_menu_light_white_background));

        //Brac Add
        String tipEnable = ParamsUtils.getString(ParamsConst.PARAMS_KEY_TIP_ADJUST_ENABLE_FLAG, "0");
        if (tipEnable.equals("1")) {
            items.add(new MenuItem(TransType.TRANS_TIP_SALE, R.drawable.ic_app_tips, R.color.app_menu_light_white_background));
        }
        //-----------

        if (BDevice.supportHCE()) {
            items.add(new MenuItem(TransType.TRANS_HCE_SALE, R.drawable.app_menu_hce_sale, R.color.app_menu_light_white_background));
        }

        //items.add(new MenuItem(TransType.TRANS_CASH_BACK, R.drawable.app_menu_cash_back, R.color.app_menu_light_white_background));
        //items.add(new MenuItem(TransType.TRANS_CASH_ADVANCE, R.drawable.app_menu_cash_advance, R.color.app_menu_light_white_background));

        //items.add(new MenuItem(TransType.TRANS_REFUND, R.drawable.app_menu_refund, R.color.app_menu_light_white_background));

        // items.add(new MenuItem(TransType.TRANS_BALANCE, R.drawable.app_menu_balance, R.color.app_menu_light_white_background));

        items.add(new MenuItem(TransType.TRANS_ABOUT, R.drawable.ic_app_version, R.color.app_menu_light_white_background));
        if (ParamsUtils.getString(ParamsConst.PARAMS_KEY_HELP_CENTER, "0").equals("1"))
            items.add(new MenuItem(TransType.TRANS_HELP_CENTER, R.drawable.ic_app_help, R.color.app_menu_light_white_background));
        items.add(new MenuItem(TransType.TRANS_SETTINGS, R.drawable.ic_app_manage, R.color.app_menu_light_white_background));

        //items.add(new MenuItem(TransType.TRANS_VOID_INSTALLMENT, R.drawable.app_menu_void_installment, R.color.app_menu_light_white_background));


        //items.add(new MenuItem(TransType.TRANS_LOGIN, R.drawable.app_menu_login, R.color.app_menu_light_white_background));

        /* items.add(new MenuItem(activity -> new MessageDialog.Builder(activity)
                .setMessage(R.string.app_exit_prompt)
                .setConfirmButton(dialog -> activity.finish())
                .setCancelButton(dialog -> {})
                .show(),R.string.app_menu_exit,R.drawable.app_menu_exit,R.color.app_menu_light_white_background));
        */
        return items;
    }

    /**
     * get the main menu
     */
    public List<MenuItem> getMenu() {
        if (menu.isEmpty() || isChanged()) {
            menu = getMainFeatureItems();
            checkSupport(menu);
            LoggerUtils.d("init main menu");
            for (MenuItem item : menu) {
                LoggerUtils.d(item.toString());
            }
        }
        return menu;
    }

    /**
     * If the Transaction support status was changed, return true.
     */
    public boolean isChanged() {
        boolean supportPrinter = BDevice.supportPrint() || ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL);
        if (lastSupportPrinter != supportPrinter) {
            return true;
        }
        if (lastSupportReceipt != ReceiptProvider.hasSupportedReceipts()) {
            return true;
        }
        //for brac
        if(isChangeMainMenu){
            return true;
        }

        for (Map.Entry<String, Boolean> entry : mLastParamsMap.entrySet()) {
            String key = entry.getKey();
            Boolean lastParamValue = mLastParamsMap.get(key);
            if (lastParamValue == null || lastParamValue != ParamsUtils.getBoolean(key, true)) {
                //Trans support status was changed
                return true;
            }
        }


        return false;
    }

    /**
     * Check item support
     */
    private void checkSupport(List<MenuItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Iterator<MenuItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            MenuItem item = iterator.next();
            List<MenuItem> subItems = item.getSubItems();
            if (subItems != null) {
                checkSupport(subItems);
                if (subItems.isEmpty()) {
                    iterator.remove();
                }
                continue;
            }
            //Check whether to support this item.
            //Save item ParamsConst key.
            String paramsKey = TransUtils.getParamsKey(item.getTransType());
            boolean support = ParamsUtils.getBoolean(paramsKey, true);
            mLastParamsMap.put(paramsKey, support);
            if (!support) {
                iterator.remove();
            }
        }
        lastSupportPrinter = BDevice.supportPrint() || ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL);
        lastSupportReceipt = ReceiptProvider.hasSupportedReceipts();
    }

    public void changeBracMenu(Boolean isChangeMainMenu) {
        this.isChangeMainMenu = isChangeMainMenu;
    }
}
