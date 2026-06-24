package acquire.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;


import com.zztl.pos.ucb.R;
import acquire.app.dev_rnd.TestActivity;
import acquire.base.activity.bottom_sheet.MessageBottomSheet;
import acquire.core.constant.CoreContent;
import acquire.app.brac.ui.menu.BankMenuItemsFragment;
import acquire.app.brac.ui.home.MainBracFragment;
import acquire.app.brac.ui.home.MainViewPagerAdapter;
import com.zztl.pos.ucb.databinding.AppActivityMainBinding;
import acquire.app.fragment.splash.SplashFragment;
import acquire.base.BaseApplication;
import acquire.base.activity.BaseActivity;
import acquire.base.activity.callback.SimpleCallback;
import acquire.base.utils.AppUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.OnUsBinMap;

import acquire.core.constant.ParamsConst;
import acquire.core.fragment.common.DataLoader;
import acquire.core.model.GroupByTranType;
import acquire.core.model.PayFlexField63ResponseModel;
import acquire.core.model.SchemeGroup;
import acquire.core.report_data_factiry.CardDataFactory;
import acquire.core.tools.FieldDataParseUtility;
import acquire.core.tools.RecordExportUtil;
import acquire.core.tools.SelfCheckHelper;
import acquire.core.tools.LoadMenuData;
import acquire.core.tools.sim.NetworkStatusManager;
import acquire.core.tools.sim.SendUssdRequest;
import acquire.database.model.Merchant;
import acquire.database.model.Record;
import acquire.database.repository.MerchantRepository;
import acquire.database.repository.RecordRepository;
import acquire.database.repository.ReversalDataRepository;
import acquire.sdk.ServiceHelper;
import acquire.sdk.system.BSystem;

/**
 * The main Activity
 *
 * @author Janson
 * @date 2018/10/4 18:13
 */
public class MainActivity extends BaseActivity {

    private ViewPager2 vpLandingSlider;
    private MainViewPagerAdapter adapter;

    private final int STORAGE_PERMISSION_CODE = 10909;

    @Override
    public @IdRes
    int attachFragmentResId() {
        return R.id.fragment_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.base_colorPrimary));

        AppActivityMainBinding binding = AppActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        //set immersed status bar.
        //DisplayUtils.immersedStatusBar(getWindow());
        //splashAnimation();

        //ffor luncher aplication
        /*try {
            startLockTask();
        } catch (Exception e) {
            LoggerUtils.e(e.getMessage());
        }*/

        setNaveAndStatusBarColor(getWindow());

        checkStoragePermissionAndRead();

        binding.incldSplash.tvVersion.setText(String.format(Locale.getDefault(), "%s %s", getString(R.string.app_version), AppUtils.getAppVersionName(this)));

        /*
         * must be executed after SelfCheckHelper.initAppConfig(context) in App.class,
         * so use BaseApplication.SINGLE_EXECUTOR
         */

        vpLandingSlider = findViewById(R.id.vpLandingSlider);
        adapter = new MainViewPagerAdapter(this);


        BaseApplication.SINGLE_EXECUTOR.execute(() -> {
            SelfCheckHelper.initDevice(this);
            //disable task and home button
            BSystem.setTaskButton(false);
            BSystem.setHomeButton(false);
            checkDeviceStatus();
            loadFeatureMenuData();

            //only use in developing time
            devRnDTest();

            //Enter the main fragment
            //mSupportDelegate.switchContent(                                                                                                                                                                                                                                                         MainFragment.newInstance());
            runOnUiThread(() -> {
                binding.rlSplash.setVisibility(View.GONE);
                vpLandingSlider.setVisibility(View.VISIBLE);
            });

        });


        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_FIRST_RUN, true)
                && !ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_IS_PRESENT_INI_FILE, false)) {
            checkIniParamsAndInitialFragments();
        } else {

            // Add fragments dynamically
            adapter.addFragment(MainBracFragment.newInstance());
            adapter.addFragment(BankMenuItemsFragment.newInstance());

            vpLandingSlider.setAdapter(adapter);

            vpLandingSlider.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
            vpLandingSlider.setOffscreenPageLimit(1);
        }
    }

    private void checkIniParamsAndInitialFragments() {
        MessageBottomSheet sheet =
                MessageBottomSheet.newInstance(
                        "You want to load initial Params file fist",
                        R.drawable.ic_param_file_load,
                        false,
                        true,
                        "NO",
                        "OK"
                );

        sheet.setActionListener(new MessageBottomSheet.BottomSheetActionListener() {
            @Override
            public void onLeftButtonClick() {

            }

            @Override
            public void onRightButtonClick() {
                ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_FIRST_RUN, true);
                ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_IS_PRESENT_INI_FILE, false);

            }
        });
        sheet.setCancelable(false);
        sheet.show(
                getSupportFragmentManager(),
                "message_sheet"
        );
    }

    private void checkDeviceStatus() {
        boolean simDataAvailable =
                NetworkStatusManager.getInstance()
                        .isSimDataAvailable(this);

        if (simDataAvailable) {
            ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_IS_OPERATOR_AVAILABLE, true);
        } else {
            // No SIM Data
            ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_IS_OPERATOR_AVAILABLE, false);
        }

        // TODO: check is first run and check INI param fle available

        // TODO: Get check paper available


    }

    private void devRnDTest() {

        int logoClickCount = ParamsUtils.getInt("MainLogoClickCount", 0);
        LoggerUtils.d("newCall , MainLogoClickCount; " + logoClickCount);
        //testCode();
        //exportRecordData();

        //getPhoneNumber();

        if (logoClickCount == 3) {
            exportRecordData();//export json in Download
        } else if (logoClickCount == 4) {
            merchantLog();//show merchant log
        } else if (logoClickCount == 5) {
            Intent intObje = new Intent(this, TestActivity.class);//show merchant log
            startActivity(intObje);//start test activity
        } else if (logoClickCount == 10) {
            insertDummyRecord();// insert dummy data
        } else if (logoClickCount == 11) {
            getCardData(); //only Card log
        } else if (logoClickCount == 12) {//remove all records
            removeRecordData();
        } else if (logoClickCount == 13) {//remove all reversal
            removeReversalData();
        } else if (logoClickCount == 14) {//remove all records, and reversal
            removeRecordData();
            removeReversalData();
        }
        //modifyRecordData();
        //------------------------------

        //Wait for the splash animation to end
        //splashWaitFinish();
    }

    private void getPhoneNumber() {

        //TODO: We implement thi in App version or About feature .
        SendUssdRequest.sendUssdRequest(this, "*2#", new SendUssdRequest.ListenUssdResponse() {
            @Override
            public void hideLoader(boolean isHideLoader) {
                LoggerUtils.d("newCall , Is Loader Show: " + !isHideLoader);
            }

            @Override
            public void getUssdResponse(String response) {
                LoggerUtils.d("newCall , Ussd Response: " + response);

            }

            @Override
            public void getUssdErrorResponse(String errorResponse) {
                LoggerUtils.d("newCall , Ussd errorResponse: " + errorResponse);
            }

            @Override
            public void getUssdPermissionException(String permissionResponse) {

                LoggerUtils.d("newCall , Ussd Permission: " + permissionResponse);

            }
        });

    }

    private void testCode() {
        String f6 = "30303132313230303030303030303030313530303030303030303030303030303430303030303030303030303030303030303130303030303030303131313131313139393939";
        PayFlexField63ResponseModel payFlexField63ResponseModel = FieldDataParseUtility.parseField63(f6);
        LoggerUtils.d("newCall: merchant findAllByMType: " + payFlexField63ResponseModel.toString());
    }

    private void merchantLog() {

        List<Merchant> settleMerchants = new MerchantRepository().findEmiMerchant();
        LoggerUtils.d("newCall: merchant findAllByMType: " + settleMerchants.size());
        for (Merchant merchant : settleMerchants) {
            LoggerUtils.d("newCall: ------------ merchant Type: " + merchant.getType());
        }
        LoggerUtils.d("newCall: merchant findAllByMType: " + settleMerchants.size());
        LoggerUtils.d("newCall: ------------------------------------------------ ");
        List<Merchant> settleMerchants2 = new MerchantRepository().findAll();
        for (Merchant merchant : settleMerchants2) {
            LoggerUtils.d("newCall: ------------ merchant Type: " + merchant.getType());
        }

        LoggerUtils.d("newCall: merchant findAll: " + settleMerchants2.size());
    }

    private void getCardData() {

        List<SchemeGroup> schemeGroups = CardDataFactory.getSchemeGroups();
        LoggerUtils.i("newCall, schemeGroups: " + schemeGroups.size());
        for (SchemeGroup sg : schemeGroups) {
            LoggerUtils.i("newCall, schemeGroups Title: " + sg.getSchemeTitle());
            LoggerUtils.i("newCall, Total Count: " + sg.getTotalSchemeWiseTranCount());
            LoggerUtils.i("newCall, Total Amount: " + sg.getTotalSchemeWiseTranAmount());
            LoggerUtils.i("newCall, Internal record Size: " + sg.getGroupByTranType().size());
            for (GroupByTranType gbtt : sg.getGroupByTranType()) {
                LoggerUtils.i("newCall ------------------ TranType: " + gbtt.getTranType());
                LoggerUtils.i("newCall, ----------------- Title: " + gbtt.getTitle());
                LoggerUtils.i("newCall, ----------------- Count: " + gbtt.getCount());
                LoggerUtils.i("newCall, ----------------- Amount: " + gbtt.getAmount());
                LoggerUtils.i("newCall, ----------------- TipAmount: " + gbtt.getTipAmount());
            }
        }
    }

    private void exportRecordData() {
        String result = RecordExportUtil.exportRecordsToJson(this);
        LoggerUtils.i("newCall, Record data save: " + result);
        //Toast.makeText(this, result, Toast.LENGTH_LONG).show();

    }

    private void loadFeatureMenuData() {
        CoreContent.REPORT_MENU = LoadMenuData.loadReportMenuItems();
        CoreContent.PRE_AUTH_MENU = LoadMenuData.loadPreAuthMenuItems();
        CoreContent.PAY_FLEX_MENU = LoadMenuData.loadPayFlexMenuItems();
        CoreContent.PRINT_RECEIPT_MENU = LoadMenuData.loadPrintReceiptMenuItems();

        String cardTypeBin = ParamsUtils.getString("CARDTYPEBIN", null);
        if (cardTypeBin != null) {
            CoreContent.REPORT_CARD_ONUS_LIST = LoadMenuData.parseCardBins(cardTypeBin);
            // CoreContent.REPORT_CARD_ONUS_MAP = LoadMenuData.parseCardBinsMap(cardTypeBin);
            OnUsBinMap.REPORT_CARD_ONUS_MAP = LoadMenuData.parseCardBinsMap(cardTypeBin);
        }
    }

/*    private static void setNaveAndStatusBarColor(@NonNull Window window) {

        window.setStatusBarColor(ContextCompat.getColor(window.getContext(), acquire.base.R.color.status_bar_color));

        View decor = window.getDecorView();
        decor.setSystemUiVisibility(0);

        window.setNavigationBarColor(
                ContextCompat.getColor(window.getContext(), acquire.base.R.color.nav_bar_color)
        );
    }*/

    private void insertDummyRecord() {
        //if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_FIRST_RUN, true)) {
        RecordRepository recordRepository = new RecordRepository();
        ArrayList<Record> dummyRecords = LoadMenuData.loadDummyRecords(this);

        for (Record record : dummyRecords) {
            //DataConverter.pubBeanToRecord(pubBean, record);
            String eUuid = UUID.randomUUID().toString();
            record.setUuid(eUuid);
            boolean result = recordRepository.add(record);
            Log.d("MainActivity", "result" + result);
            Log.d("MainActivity", "Record: " + record.toString());
        }
        //}
    }

    private void removeRecordData() {
        RecordRepository recordRepository = new RecordRepository();
        // ArrayList<Record> dummyRecords = LoadMenuData.loadDummyRecords(this);
        boolean result = recordRepository.deleteAll();
        Log.d("MainActivity", "result" + result);
    }

    private void modifyRecordData() {
        RecordRepository recordRepository = new RecordRepository();
        int cumberOfUpdates = recordRepository.updateIsOnUs();
        Log.d("MainActivity", "cumberOfUpdates: " + cumberOfUpdates);
        List<Record> records = recordRepository.findAll();
        Log.d("MainActivity", "Updated Size: " + records.size());
        long totalAmount = 0;
        long totalTipAmount = 0;
        int totalCount = 0;
        for (Record record : records) {
            if (!record.getTransType().equalsIgnoreCase("TestTxn")) {

                //Set all transection inOnUs=true
                /*record.setOnUs(true);
                Log.d("MainActivity", "record type: " + record.getTransType());
                boolean iffect = recordRepository.update(record);
                Log.d("MainActivity", "record Update: " + iffect);
                Log.d("MainActivity", "record is ONUS: " + record.isOnUs());*/
                //----------

                //total count, amount, based on isOnUs = true
                Log.d("MainActivity", "record type: " + record.getTransType());
                if (record.isOnUs()) {
                    ++totalCount;
                    totalTipAmount += record.getTipAmount();
                    totalAmount += record.getAmount();
                    Log.d("MainActivity", "record TipAmount: " + record.getTipAmount());
                    Log.d("MainActivity", "record Amount: " + record.getAmount());
                }

            }
        }// end records
        Log.d("MainActivity", "record Total TipAmount: " + totalTipAmount);
        Log.d("MainActivity", "record Total Amount: " + totalAmount);
        Log.d("MainActivity", "record Total Count: " + totalCount);
    }

    private void removeReversalData() {
        ReversalDataRepository recordRepository = new ReversalDataRepository();
        // ArrayList<Record> dummyRecords = LoadMenuData.loadDummyRecords(this);
        boolean result = recordRepository.deleteAllReversalData(); //deleteAll
        Log.d("MainActivity", "result" + result);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //readFiles();
            } else {
                Log.d("MainActivity", "Storage permission denied");
            }
        }
    }


    @Override
    protected void onDestroy() {
        if (ServiceHelper.getInstance().isInit()) {
            //restore task and home button
            BSystem.setTaskButton(true);
            BSystem.setHomeButton(true);
        }
        super.onDestroy();
    }

    private void checkStoragePermissionAndRead() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                //readFiles();
            }
        } else {
            // Android 6–10
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE
                );
            } else {
                //readFiles();
            }
        }
    }


    private static CountDownLatch splashLatch;

    private void splashAnimation() {
        if (splashLatch == null) {
            splashLatch = new CountDownLatch(1);
            mSupportDelegate.switchContent(SplashFragment.newInstance(new SimpleCallback() {
                @Override
                public void result() {
                    splashLatch.countDown();
                }
            }));
        } else {
            splashLatch.countDown();
        }
    }

    private void splashWaitFinish() {
        if (splashLatch != null) {
            try {
                splashLatch.await();
            } catch (InterruptedException e) {
                LoggerUtils.e("splashLatch wait interrupted!", e);
            }
        }
    }
}
