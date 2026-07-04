package acquire.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;

import com.zztl.pos.city.R;
import com.zztl.pos.city.databinding.ActivityCityHostBinding;

import java.util.concurrent.CountDownLatch;

import acquire.app.brac.models.FeatureMainMenuModel;
import acquire.app.brac.ui.new_home.ActivityCallback;
import acquire.app.fragment.splash.SplashFragment;
import acquire.base.BaseApplication;
import acquire.base.activity.BaseActivity;
import acquire.base.activity.callback.SimpleCallback;
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

public class CityHostActivity extends BaseActivity implements ActivityCallback {

    private ActivityCityHostBinding _binding;
    private NavController _navController;
    private NavGraph _navGraph;

    public static final String KEY_FROM_SCREEN = "_key_from_screen";
    public static final String FROM_SAVED_RECIPIENTS = "_saved_recipients";

    @Override
    public int attachFragmentResId() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        _binding = ActivityCityHostBinding.inflate(LayoutInflater.from(this));
        setContentView(_binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        setNaveAndStatusBarColor(getWindow());

        loadInitialDeviceReq();


    }

    private void setNavigationGraph() {

        _navController = Navigation.findNavController(
                this,
                R.id.nav_host_fragment_activity_city_host
        );

        _navGraph = _navController.getNavInflater()
                .inflate(R.navigation.nav_graph_dash_board);
//        _navGraph.setStartDestination(R.id.citySplashFragment);
        _navGraph.setStartDestination(R.id.cityHomeFragment);

        /*if (PayoutDataController.isOpenForEmptyView) {
            navGraph.setStartDestination(R.id.beneficiaryListFragment);

        } else if (PayoutDataController.isOpenForCountryList) {
            navGraph.setStartDestination(R.id.cashOutCountryListFragment);

        } else if (PayoutDataController.isOpenFromIndiaAmount) {
            navGraph.setStartDestination(R.id.cashOutIndAmountFragment);

        } else if (PayoutDataController.isOpenFromPhpAmount) {
            navGraph.setStartDestination(R.id.cashOutEraAmountFragment);

        } else {
            navGraph.setStartDestination(R.id.cashOutRecipientRootFragment);
        }*/

        _navController.setGraph(_navGraph);

    }


    private void loadInitialDeviceReq() {

        BaseApplication.SINGLE_EXECUTOR.execute(() -> {
            //SelfCheckHelper.initDevice(this);
            //disable task and home button
            BSystem.setTaskButton(false);
            BSystem.setHomeButton(false);
            //checkDeviceStatus();

            //Enter the main fragment
            //mSupportDelegate.switchContent(                                                                                                                                                                                                                                                         MainFragment.newInstance());
            runOnUiThread(() -> {
                initClickListener();
                setNavigationGraph();
                /*// Clear everything from the back stack
                _navController.popBackStack(_navController.getGraph().getStartDestinationId(), true);
                // Change start destination
                _navGraph.setStartDestination(R.id.cityHomeFragment);
                _navController.setGraph(_navGraph);*/


/*                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(_navController.getCurrentDestination().getId(), true)
                        .build();
                _navController.navigate(R.id.cityHomeFragment, null, navOptions);*/

            });

        });
    }

    private void initClickListener() {
        _binding.btnSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_SALE);
                startActivity(intent);
            }
        });

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

    @Override
    public void onSwitchFeature(FeatureMainMenuModel featureMainMenuModel) {

        if (featureMainMenuModel.getCode() == null) return;

        switch (featureMainMenuModel.getCode()) {

            case "SALE": {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_SALE);
                startActivity(intent);
                break;
            }
            case "EMI": {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_INSTALLMENT_MENU);
                startActivity(intent);
                break;
            }
            case "SETTLEMENT": {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_SETTLE);
                startActivity(intent);
                break;
            }
            case "ABOUT": {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_ABOUT);
                startActivity(intent);
                break;
            }
            case "TEST_TRAN": {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_TEST_TRX);
                startActivity(intent);
                break;
            }
            case "PRINT": {
                /*if (featureMainMenuModel.isHasChild()) {

                    List<FeatureMainMenuModel> childList = featureMainMenuModel.getChildData();

                    // TODO: Open child screen
                }*/
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_REPORTS_PRINT);
                startActivity(intent);
                break;
            }
            default:
                break;
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