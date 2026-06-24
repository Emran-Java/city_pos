package acquire.core.tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acquire.base.BaseApplication;
import acquire.base.utils.AppUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.crypto.ShaUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.dialog.progress.ProgressDialog;
import acquire.core.R;
import acquire.core.constant.ParamsConst;
import acquire.database.model.Merchant;
import acquire.database.repository.MerchantRepository;
import acquire.sdk.ExtServiceHelper;
import acquire.sdk.FlyParameterHelper;
import acquire.sdk.ServiceHelper;
import acquire.sdk.emv.BEmvParamLoader;
import acquire.sdk.emv.BExtEmvParamLoader;
import acquire.sdk.emv.IEmvParamLoader;

/**
 * A remote params updater based on TOMS FlyParameters.
 *
 * @author Janson
 * @date 2022/7/26 15:27
 */
public class RemoteParamsUpdater {
    private ProgressDialog progressDialog;

    /**
     * receuve Fly Parameters data and parse it.
     */
    public void updateParams(Context context) {
        ThreadPool.postOnMain(() -> {
            Dialog dlg =  new AlertDialog.Builder(BaseApplication.getAppContext())
                    .setTitle(AppUtils.getAppName(context))
                    .setMessage(R.string.core_fly_parameter_dialog_updating_title)
                    .setPositiveButton(R.string.base_ok, (dialog, which) -> update(context))
                    .setNegativeButton(R.string.base_cancel, (dialog, which) -> {})
                    .create();
//            MessageDialog dlg = new MessageDialog.Builder(context)
//                    .setMessage(R.string.core_fly_parameter_dialog_updating_title)
//                    .setConfirmButton(dialog -> update(context))
//                    .setCancelButton(dialog -> {})
//                    .create();
            Window window = dlg.getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
            dlg.show();
        });


    }

    private void update(Context context) {
        LoggerUtils.d("FlyParameter request");
        progressDialog = new ProgressDialog.Builder(context)
                .setContent(R.string.core_fly_parameter_updating)
                .setTimeout(3*60*1000,dialog->{})
                .create();
        if (!(context instanceof Activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
        }
        progressDialog.show();

        FlyParameterHelper.getInstance().fetchParameters(new FlyParameterHelper.FlyParameterCallback() {
            @Override
            public void onReceive(Map<String, Object> map) {
                if (map != null){
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        try {
                            switch (key){
                                case "Merchants":
                                    //Merchants
                                    if (value instanceof List){
                                        setMerchant((List) value);
                                    }else{
                                        LoggerUtils.e("Merchant value format error.");
                                    }
                                    break;
                                case "Newland_L3_configuration":
                                    //EMV config file
                                    LoggerUtils.d("Found Newland_L3_configuration");
                                    if (value instanceof File){
                                        if (!parseEmv((File) value)) {
                                            ToastUtils.showToast(context.getString(R.string.core_fly_parameter_parse_emv_failed_format, key));
                                        }
                                    }else{
                                        LoggerUtils.e("Newland_L3_configuration value format error.");
                                    }
                                    break;
                                default:
                                    //Settings
                                    if (isValidSettingKey(key)) {
                                        if (ParamsConst.PARAMS_KEY_PASSWORD_ADMIN.equals(key)
                                                || ParamsConst.PARAMS_KEY_PASSWORD_SYSTEM_ADMIN.equals(key)
                                                ||ParamsConst.PARAMS_KEY_PASSWORD_SECURITY.equals(key)){
                                            value = ShaUtils.sha256(key.getBytes());
                                        }
                                        ParamsUtils.setObject(key, value);
                                    }else{
                                        LoggerUtils.e("invalid key: "+key);
                                    }
                                    break;
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                LoggerUtils.d("FlyParameter success");
                ToastUtils.showToast(R.string.core_fly_parameter_update_success);
                ThreadPool.postOnMain(() -> progressDialog.dismiss());
                //send the main ui update broadcast
                Intent intent = new Intent();
                String packageName = context.getPackageName();
                intent.setPackage(packageName);
                intent.setAction(packageName);
                context.sendBroadcast(intent);
            }

            @Override
            public void onError(int errorCode, String message) {
                LoggerUtils.e("Receive FlyParameter failed.Error code = " + errorCode + ",message = " + message);
                ToastUtils.showToast(message);
                ThreadPool.postOnMain(() -> progressDialog.dismiss());
            }
        });
    }

    private static List<String> settingsKeys;
    private boolean isValidSettingKey(String key){
        if (settingsKeys == null){
            settingsKeys = new ArrayList<>();
            for (Field field : ParamsConst.class.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (field.getType() == String.class
                        && (Modifier.PUBLIC & modifiers) != 0
                        && (Modifier.FINAL & modifiers) != 0
                        && (Modifier.STATIC & modifiers) != 0) {
                    try {
                        String filedValue = (String) field.get(ParamsConst.class);
                        settingsKeys.add(filedValue);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return settingsKeys.contains(key);
    }
    private void setMerchant(List merchantParentNode){
        MerchantRepository merchantRepository = new MerchantRepository();
        Map<String,Object> merchantMap = (Map<String,Object>) merchantParentNode.get(0);
        for (Object list : merchantMap.values()) {
            List merchants = (List) list;
            if (merchants.isEmpty()){
                continue;
            }
            for (Object object : merchants) {
                Map<String,Object> fields = (Map<String, Object>) object;
                Merchant merchant = new Merchant();
                for (Map.Entry<String, Object> entry : fields.entrySet()) {
                    String tag = entry.getKey();
                    Object value = entry.getValue();
                    for (Field field : Merchant.class.getDeclaredFields()) {
                        field.setAccessible(true);
                        if (tag.equals(field.getName())){
                            try {
                                field.set(merchant,value);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                Merchant exitMerchant = merchantRepository.findByType(merchant.getType());
                if (exitMerchant == null){
                    LoggerUtils.d("FlyParameter add merchant: "+merchant);
                    merchantRepository.add(merchant);
                }else{
                    for (Field field : Merchant.class.getDeclaredFields()) {
                        field.setAccessible(true);
                        try {
                            field.set(exitMerchant,field.get(merchant));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    LoggerUtils.d("FlyParameter update merchant: "+merchant);
                    merchantRepository.update(merchant);
                }
            }


        }
    }
    /**
     * Parse EMV AID and CAPKS
     */
    private boolean parseEmv(File xmlFile) {
        boolean external = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL);
        IEmvParamLoader loader;
        if (external) {
            loader = new BExtEmvParamLoader();
            if (!ExtServiceHelper.getInstance().isInit()) {
                return false;
            }
        } else {
            loader = new BEmvParamLoader();
            if (!ServiceHelper.getInstance().isInit()) {
                return false;
            }
        }
        try (InputStream is = new FileInputStream(xmlFile)) {
            boolean result = EmvConfigXmlParser.parseXml(is, loader);
            if (result) {
                ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_EMV_AID_CAPK, true);
            }
            return result;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }
} 
