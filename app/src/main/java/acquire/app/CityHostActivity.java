package acquire.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;

import com.zztl.pos.city.R;
import com.zztl.pos.city.databinding.ActivityCityHostBinding;
import com.zztl.pos.city.databinding.AppActivityMainBinding;

import acquire.base.BaseApplication;
import acquire.base.activity.BaseActivity;
import acquire.base.utils.ParamsUtils;
import acquire.core.TransActivity;
import acquire.core.constant.CoreContent;
import acquire.core.constant.OnUsBinMap;
import acquire.core.constant.TransTag;
import acquire.core.constant.TransType;
import acquire.core.tools.LoadMenuData;
import acquire.core.tools.SelfCheckHelper;
import acquire.sdk.system.BSystem;

public class CityHostActivity extends BaseActivity {

    private ActivityCityHostBinding binding;

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

        binding = ActivityCityHostBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadInitialDeviceReq();

        initClickListener();
        setNavigationGraph();
    }

    private void setNavigationGraph() {

        NavController navController = Navigation.findNavController(
                this,
                R.id.nav_host_fragment_activity_city_host
        );

        NavGraph navGraph = navController.getNavInflater()
                .inflate(R.navigation.nav_graph_dash_board);
        navGraph.setStartDestination(R.id.citySplashFragment);
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

        navController.setGraph(navGraph);
    }


    private void loadInitialDeviceReq() {

        BaseApplication.SINGLE_EXECUTOR.execute(() -> {
            SelfCheckHelper.initDevice(this);
            //disable task and home button
            BSystem.setTaskButton(false);
            BSystem.setHomeButton(false);
            //checkDeviceStatus();
            loadFeatureMenuData();

            //only use in developing time
            //devRnDTest();

            //Enter the main fragment
            //mSupportDelegate.switchContent(                                                                                                                                                                                                                                                         MainFragment.newInstance());
            runOnUiThread(() -> {
                //binding.rlSplash.setVisibility(View.GONE);
                //vpLandingSlider.setVisibility(View.VISIBLE);
            });

        });
    }

    private void initClickListener() {
        binding.btnSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_SALE);
                startActivity(intent);
            }
        });

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
}