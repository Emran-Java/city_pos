package acquire.core.tools;


import android.content.Context;
import android.util.Log;

import java.util.Locale;

import acquire.base.BaseApplication;
import acquire.base.utils.AppUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.iso8583.ISO8583;
import acquire.base.utils.network.NetworkUtils;
import acquire.core.BuildConfig;
import acquire.core.R;
import acquire.core.SchedulerService;
import acquire.core.constant.FileConst;
import acquire.core.constant.ParamsConst;
import acquire.sdk.ExtServiceHelper;
import acquire.sdk.FlyParameterHelper;
import acquire.sdk.ServiceHelper;
import acquire.sdk.device.BDevice;
import acquire.sdk.emv.BEmvParamLoader;
import acquire.sdk.emv.BExtEmvParamLoader;
import acquire.sdk.emv.IEmvParamLoader;

/**
 * Self check. Deal some initial tasks.
 *
 * @author Janson
 * @date 2018/3/26
 */
public class SelfCheckHelper {

    private static void loadPrintReceiptMenu(Context context) {
        String json = JsonUtils.loadJSONFromAsset(context, FileConst.MENU_FILE_PRINT_RECEIPT);
        ParamsUtils.setString(FileConst.MENU_KEY_PRINT_RECEIPT, json);
        LoggerUtils.i("newCall Done load Print-Receipt: "+json);
    }

    private static void loadPayFlexMenu(Context context) {
        String json = JsonUtils.loadJSONFromAsset(context, FileConst.MENU_FILE_PAY_FLEX);
        ParamsUtils.setString(FileConst.MENU_KEY_PAY_FLEX, json);
        LoggerUtils.i("newCall Done load PayFlexMenu: "+json);
    }

    private static void loadPreAuthMenu(Context context) {
        String json = JsonUtils.loadJSONFromAsset(context, FileConst.MENU_FILE_PRE_AUTH);
        ParamsUtils.setString(FileConst.MENU_PREF_KEY_PRE_AUTH, json);
        LoggerUtils.i("newCall Done load PreAuthMenu: "+json);
    }

    private static void loadReportMenu(Context context) {

        String json = JsonUtils.loadJSONFromAsset(context, FileConst.MENU_FILE_REPORT);
        ParamsUtils.setString(FileConst.MENU_PREF_KEY_REPORT, json);
        LoggerUtils.i("newCall Done load ReportMenu: "+json);
    }


    /**
     * init application configuration.
     */
    public static void initAppConfig(Context context) {

        //open log
        LoggerUtils.configPrint(true);
        LoggerUtils.setCustomTagPrefix("bankdemo"+ BuildConfig.TEMPLATE_VERSION);

        LoggerUtils.i("Check application parameters start.");
        //check first run
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_FIRST_RUN, true)) {
            try {
                loadReportMenu(context);
                loadPreAuthMenu(context);
                loadPayFlexMenu(context);
                loadPrintReceiptMenu(context);

                String opName = NetworkUtils.getOperatorName(context);
                LoggerUtils.i("newCall: "+ "opName: "+opName);

                LoggerUtils.d("App first run.");
                // init default params.properties
                AppParamsImporter.initDefaultAppParams(opName);
//                AppParamsImporter.initDefaultMerchants();
                //AppParamsImporter.initBracDefaultMerchants(opName);

                //set first run false
                //ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_FIRST_RUN, false);
                LoggerUtils.i("App first over.");
            } catch (Exception e) {
                LoggerUtils.e("first run failed!",e);
            }
        }

        //load 8583 configuration xml
        LoggerUtils.d("init 8583.xml");
        boolean loadSuccess = ISO8583.getDefault().loadXmlFile(context, FileConst.CUPS8583);
        if (!loadSuccess) {
            LoggerUtils.e("ISO8583 configuration xml file parses failed.");
            ToastUtils.showToast(R.string.core_parse_8583_configuration_failed);
            return;
        }
        //init sound
        LoggerUtils.d("init sound resource");
        SoundPlayer.getInstance().init();
        LoggerUtils.i("Check application parameters over.");
    }

    /**
     * init NSDK and EMV.
     */
    public static void initDevice(Context context) {
        LoggerUtils.i("Check device start.");
        LoggerUtils.d("Version name: " + AppUtils.getAppVersionName(context));
        boolean isConnectNsdk;
        //init NSDK
        isConnectNsdk = ServiceHelper.getInstance().init(context);
        if (!isConnectNsdk) {
            ToastUtils.showToast(R.string.core_sdk_init_failed);
            return;
        }
        LoggerUtils.i("SN: " + BDevice.getSn());
        //check external flag
        boolean external = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL);
        if (!external && (!BDevice.isExistSecurityModule() || BDevice.isCpos())) {
            LoggerUtils.d("No Built-in Security Module!");
            ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL, true);
            external = true;
        }
        if (external) {
            //external PIN pad
            int connectMode = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL_CONNECT_MODE);
            isConnectNsdk = ExtServiceHelper.getInstance().init(context,connectMode);
            if (!isConnectNsdk) {
                ToastUtils.showToast(R.string.core_device_ext_pinpad_init_failed);
            }
        }
        if (isConnectNsdk){
            //EMV config
            if (!loadEmvConfig(context, external,false)){
                ToastUtils.showToast(R.string.core_device_load_emv_configurations);
            }
        }

        //init fly parameter service
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_TOMS_FLY_PARAMETERS)) {
            LoggerUtils.d("register TOMS Fly Parameter service");
            boolean result = FlyParameterHelper.getInstance().bind(context);
            if (result) {
                FlyParameterHelper.getInstance().setParameterWatcher(context, () ->
                    new RemoteParamsUpdater().updateParams(BaseApplication.getAppContext())
                );
            }else{
                ToastUtils.showToast(R.string.core_device_fly_parameter_init_failed);
            }
        }else{
            FlyParameterHelper.getInstance().unbind();
        }

        //auto settle
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_AUTO_SETTLE_OPEN)){
            SchedulerService.scheduleAutoSettle();
        }

        LoggerUtils.i("Check device over.");
    }


    /**
     * init EMV config
     */
    public static boolean loadEmvConfig(Context context, boolean external,boolean forceLoad) {
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
        boolean loadAidCapk = !ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_EMV_AID_CAPK)
                || forceLoad
                || loader.isCapkLoss()
                || loader.isCtAidLoss()
                || loader.isClessAidLoss();
        if (loadAidCapk) {
            LoggerUtils.d(String.format(Locale.getDefault(),"init %s emv ",external?"external":"built-in"));
            boolean loadSucc = EmvConfigXmlParser.parseXml(context, FileConst.EMV_CONFIG, loader);
            if (loadSucc) {
                ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_EMV_AID_CAPK, true);
            }
            return loadSucc;
        }
        return true;
    }
}
