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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zztl.pos.city.R;
import com.zztl.pos.city.databinding.ActivitySplashBinding;

import java.util.concurrent.CountDownLatch;

import acquire.app.fragment.splash.SplashFragment;
import acquire.base.BaseApplication;
import acquire.base.activity.BaseActivity;
import acquire.base.activity.callback.SimpleCallback;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.TransActivity;
import acquire.core.constant.CoreContent;
import acquire.core.constant.FileConst;
import acquire.core.constant.OnUsBinMap;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.TransTag;
import acquire.core.constant.TransType;
import acquire.core.tools.JsonUtils;
import acquire.core.tools.LoadMenuData;
import acquire.core.tools.SelfCheckHelper;
import acquire.sdk.ServiceHelper;
import acquire.sdk.system.BSystem;

public class SplashActivity extends BaseActivity {

    private final int STORAGE_PERMISSION_CODE = 10909;

    @Override
    public int attachFragmentResId() {
        return R.id.fragment_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        //set immersed status bar.
        DisplayUtils.immersedStatusBar(getWindow());
        splashAnimation();

        //only use in developing time
        //devRnDTest();


        /*
         * must be executed after SelfCheckHelper.initAppConfig(context) in App.class,
         * so use BaseApplication.SINGLE_EXECUTOR
         */
        BaseApplication.SINGLE_EXECUTOR.execute(() -> {
            SelfCheckHelper.initDevice(this);
            //disable task and home button
            BSystem.setTaskButton(false);
            BSystem.setHomeButton(false);
            loadFeatureMenuData();
            try {
                Thread.sleep(1500); // Delay before updating UI
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            //Wait for the splash animation to end
            splashWaitFinish();
            //Enter the main fragment
//            mSupportDelegate.switchContent(MainFragment.newInstance());
//            mSupportDelegate.switchContent(MainBracFragment.newInstance());
            Intent intent = new Intent(getApplicationContext(), CityHostActivity.class);
            //intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_SALE);
            startActivity(intent);
            finish();
        });

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
                LoggerUtils.e("splashLatch wait interrupted!",e);
            }
        }
    }

    private void loadFeatureMenuData() {

        String json = JsonUtils.loadJSONFromAsset(this, FileConst.MENU_FILE_FEATURE_MAIN_MENU);

        if (json != null) {
            ParamsUtils.setString(ParamsConst.PARAMS_KEY_MENU_FEATURE_MAIN, json);
        } else {
            //TODO: when main feature menu can't load
        }

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

}