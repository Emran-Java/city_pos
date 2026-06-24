package acquire.core.fragment.common.report;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.core.constant.IntentParamKeyContent;
import acquire.core.constant.ReportConstant;
import acquire.core.databinding.CoreFragmentAllDetailsReportBinding;
import acquire.core.fragment.common.DataLoader;
import acquire.core.fragment.print.PrintViewModel;
import acquire.core.model.FeatureSubMenuModel;
import acquire.core.model.SchemeGroup;
import acquire.core.trans.impl.print_detail.report.ReportsAdapter;
import acquire.core.trans.impl.print_detail.report.SchemeGroupAdapter;
import acquire.database.model.Merchant;
import acquire.database.model.Record;

public class AllDetailsReportFragment extends BaseFragment {

    private CoreFragmentAllDetailsReportBinding binding;
    private AllDetailsReportViewModel viewModel;

    private List<Record> recordsData = new ArrayList<>();
    private List<Record> emiRecordsData = new ArrayList<>();
    private List<SchemeGroup> schemeGroupsData = new ArrayList<>();
    private List<SchemeGroup> emiSchemeGroupsData = new ArrayList<>();

    private ReportsAdapter reportsAdapter, emiReportsAdapter;
    private SchemeGroupAdapter schemeAdapter, emiSchemeAdapter;
//    private SchemeGroupAdapter grandTotalAdapter;

    private FeatureSubMenuModel featureSubMenuModel;
    private String titleBarText = "";
    private String mTopReportTitle = "";
    private String mBottomReportTitle = "";
    private String reportCode;

    private boolean mIsPayFlexEnable = false;
    private boolean isEmiDataPrint = false;

    private boolean mIsPayFlexReportShow = false;


    public static AllDetailsReportFragment newInstance(FeatureSubMenuModel model) {

        AllDetailsReportFragment fragment = new AllDetailsReportFragment();
        Bundle bundle = new Bundle();
        //bundle.putSerializable(KEY, model);
        //bundle.putString(IntentParamKeyContent.TRANS_TITLE_TEXT, model.getTitle());
        //bundle.putString(KEY, model.getCode());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = CoreFragmentAllDetailsReportBinding.inflate(inflater, container, false);

        initArgs();
        setupRecycler();
        setupToolbar();
        setupViewModel();
        initListener();

        return binding.getRoot();
    }

    private void initArgs() {
        Bundle args = getArguments();

        if (args != null) {
            titleBarText = args.getString(IntentParamKeyContent.TRANS_TITLE_TEXT, "");
            featureSubMenuModel = (FeatureSubMenuModel)
                    args.getSerializable(IntentParamKeyContent.TRANS_REPORT_MODEL_KEY);
        }
    }

    private void setupRecycler() {
        reportsAdapter = new ReportsAdapter(new ArrayList<>());
        binding.rvTranType.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTranType.setNestedScrollingEnabled(false);
        binding.rvTranType.setAdapter(reportsAdapter);

        schemeAdapter = new SchemeGroupAdapter(new ArrayList<>());
        binding.rvCardScheme.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCardScheme.setAdapter(schemeAdapter);

        //PayFlex
        emiReportsAdapter = new ReportsAdapter(new ArrayList<>());
        binding.incEmiDetails.rvEmiTranType.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.incEmiDetails.rvEmiTranType.setNestedScrollingEnabled(false);
        binding.incEmiDetails.rvEmiTranType.setAdapter(emiReportsAdapter);

        emiSchemeAdapter = new SchemeGroupAdapter(new ArrayList<>());
        binding.incEmiDetails.rvEmiCardScheme.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.incEmiDetails.rvEmiCardScheme.setAdapter(emiSchemeAdapter);
        //-------------------
    }

    private void setupToolbar() {
        binding.toolbar.setBackListener(v ->
                getParentFragmentManager().popBackStack()
        );
    }

    private void setupViewModel() {

        DataLoader.getInstance().show(
                requireContext(),
                "Loading Report",
                "Fetching transaction details..."
        );

        viewModel = new ViewModelProvider(this).get(AllDetailsReportViewModel.class);

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            setupToolbarTitle(state);
            setMerchantView(state.merchant);
            setEmiMerchantView(state.emiMerchant);

            recordsData.clear();
            recordsData.addAll(state.records);
            reportsAdapter.updateData(state.records);

            //Set List height
            /*int dataSize = state.records.size();
            int height = dpToPx(requireContext(), (int) ((135 + (dataSize * 0.0158)) * dataSize));
            binding.rvTranType.setMinimumHeight(height);*/

            schemeAdapter.updateData(state.schemeGroups);
            schemeGroupsData.clear();
            schemeGroupsData.addAll(state.schemeGroups);

            binding.rvTranType.setVisibility(
                    state.showTranType ? View.VISIBLE : View.GONE);

            binding.tvTranTypeTitle.setVisibility(
                    state.showTranType ? View.VISIBLE : View.GONE);

            //PayFlex
            mIsPayFlexEnable = state.isPayFlexEnable;
            if (mIsPayFlexEnable) {
                emiReportsAdapter.updateData(state.emiRecords);
                emiRecordsData.clear();
                emiRecordsData.addAll(state.emiRecords);

                emiSchemeAdapter.updateData(state.emiSchemeGroups);
                emiSchemeGroupsData.clear();
                emiSchemeGroupsData.addAll(state.emiSchemeGroups);

                //When open PayFlex all other card will be hide
                mIsPayFlexReportShow = state.isPayFlexReport;
                shoHideAllCardDetails(state.isPayFlexReport);
            }
            //------------

            mTopReportTitle = state.topTitle;
            mBottomReportTitle = state.bottomTitle;

            // Hide Loader after UI update
            binding.rvTranType.post(() -> {
                DataLoader.getInstance().dismiss();
            });
        });

        if (featureSubMenuModel != null) {
            viewModel.loadReport(featureSubMenuModel, titleBarText);
        }
    }

    private void shoHideAllCardDetails(boolean isPayFlexReport) {
        if (!isPayFlexReport) {
            binding.tvTranTypeTitle.setVisibility(View.VISIBLE);
            binding.incMerchant.mcvRootMarchant.setVisibility(View.VISIBLE);
            binding.rvTranType.setVisibility(View.VISIBLE);
            binding.tvCardReportTitle.setVisibility(View.VISIBLE);
            binding.rvCardScheme.setVisibility(View.VISIBLE);
        } else {
            binding.tvTranTypeTitle.setVisibility(View.GONE);
            binding.incMerchant.mcvRootMarchant.setVisibility(View.GONE);
            binding.rvTranType.setVisibility(View.GONE);
            binding.tvCardReportTitle.setVisibility(View.GONE);
            binding.rvCardScheme.setVisibility(View.GONE);
        }
    }

    private void setupToolbarTitle(AllDetailsReportUiState state) {

        if (featureSubMenuModel != null &&
                ReportConstant.REPORT_ITEM_CODE_PRE_AUTH_REPORT.equalsIgnoreCase(featureSubMenuModel.getCode())) {

            binding.tvTranTypeTitle.setText(state.toolbarTitle);
            binding.toolbar.setTitle("Details Report");

        } else {
            binding.toolbar.setTitle(state.toolbarTitle);
        }
    }

    private void initListener() {
        binding.btnPrint.setOnClickListener(v -> {
            // print from adapter data or latest state
            isEmiDataPrint = false;
            if (!mIsPayFlexReportShow) {
                printReport(recordsData, schemeGroupsData);
            } else {
                printEmiData();
            }
        });
    }

    private void printReport(List<Record> recordsData, List<SchemeGroup> schemeList) {

        if (recordsData == null && schemeList == null) {
            return;
        }

        PrintViewModel printViewModel;
        printViewModel = new ViewModelProvider(this).get(PrintViewModel.class);
        printViewModel.printAllDetailsReport2(mTopReportTitle, mBottomReportTitle, featureSubMenuModel.getCode(), recordsData, schemeList);
        printViewModel.getStatus().observe(getViewLifecycleOwner(), data -> {
            if (data.getStatus() == PrintViewModel.STATUS_SUCCESS && mIsPayFlexEnable && !emiRecordsData.isEmpty() && !isEmiDataPrint) {
                printEmiData();
            }
        });
/*        printViewModel.getReceipt().observe(getViewLifecycleOwner(), bitmap -> {

        });
        */
    }

    private void printEmiData() {
        isEmiDataPrint = true;
        mTopReportTitle = ReportConstant.REPORT_TITLE_TOP_PAY_FLEX;
        mBottomReportTitle = ReportConstant.REPORT_TITLE_BOTTOM_PAY_FLEX;
        printReport(emiRecordsData, emiSchemeGroupsData);
    }

    private void setMerchantView(Merchant merchant) {

        if (merchant == null) return;

        binding.incMerchant.tvMrcnNameValue.setText(
                merchant.getMerchantName() != null ? merchant.getMerchantName() : "");

        binding.incMerchant.tvMrcnIdVal.setText(
                merchant.getMid() != null ? merchant.getMid() : "");

        binding.incMerchant.tvTerminalIdVal.setText(
                merchant.getTid() != null ? merchant.getTid() : "");

        binding.incMerchant.tvBtcIdVal.setText(
                merchant.getBatchNo() != null ? merchant.getBatchNo() : "");
    }


    private void setEmiMerchantView(Merchant merchant) {

        if (merchant == null) return;

        binding.incEmiDetails.incEmiMerchant.tvMrcnNameValue.setText(
                merchant.getMerchantName() != null ? merchant.getMerchantName() : "");

        binding.incEmiDetails.incEmiMerchant.tvMrcnIdVal.setText(
                merchant.getMid() != null ? merchant.getMid() : "");

        binding.incEmiDetails.incEmiMerchant.tvTerminalIdVal.setText(
                merchant.getTid() != null ? merchant.getTid() : "");

        binding.incEmiDetails.incEmiMerchant.tvBtcIdVal.setText(
                merchant.getBatchNo() != null ? merchant.getBatchNo() : "");
    }

    public int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        DataLoader.getInstance().dismiss();

        binding = null;
    }

    @Override
    public FragmentCallback getCallback() {
        return null;
    }
}