package acquire.core.fragment.common;

import android.os.Bundle;
import android.view.LayoutInflater;

import acquire.base.activity.BaseActivity;
import acquire.core.R;
import acquire.core.databinding.ActivityReportsBinding;
import acquire.core.fragment.common.submenu.CoreFeatureSubMenuListFragment;
import acquire.core.fragment.common.submenu.CoreSubMenuAdapter;
import acquire.core.model.FeatureSubMenuModel;
import acquire.core.fragment.common.report.AllDetailsReportFragment;
import acquire.core.trans.impl.print_detail.report.ReportDetailsFragment;

//TODO: this activity will be remove. also remove related files
public class ReportsActivity extends BaseActivity
        implements CoreSubMenuAdapter.OnItemClickListener {

    private String mExtraToolbarTitle="";

    @Override
    public int attachFragmentResId() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityReportsBinding binding = ActivityReportsBinding.inflate(LayoutInflater.from(this));

        setContentView(binding.getRoot());
        //binding.toolbar.setBackListener(v-> onBackPressed());
        //binding.toolbar.setTitle("Prints Report".toUpperCase());
        //set immersed status bar.
       // DisplayUtils.immersedStatusAndNavigationBar(getWindow());

//        mExtraToolbarTitle = getIntent().getStringExtra("reportTitle");

        setNaveAndStatusBarColor(getWindow());

        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putString("reportTitle", mExtraToolbarTitle);

            CoreFeatureSubMenuListFragment fragment =new CoreFeatureSubMenuListFragment();

            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public void onItemClick(FeatureSubMenuModel model) {

        Bundle bundle = new Bundle();
        String toolbarTitle = model.getTitle();
        if(toolbarTitle==null || toolbarTitle.isEmpty()){
            toolbarTitle = mExtraToolbarTitle;
        }
        bundle.putString("reportTitle", toolbarTitle);

        if (model.isHasChild() && model.getChildData() != null
                && model.getChildData().size() > 0) {

            CoreFeatureSubMenuListFragment fragment =
                    CoreFeatureSubMenuListFragment.newInstance("", model.getChildData(), null);

            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();

        }
        else if(model.getCode().equalsIgnoreCase("DTL_RPT")){ //Details Report
            AllDetailsReportFragment fragment =
                    AllDetailsReportFragment.newInstance(model);

            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
        else {

            ReportDetailsFragment fragment =
                    ReportDetailsFragment.newInstance(model, null);

            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}