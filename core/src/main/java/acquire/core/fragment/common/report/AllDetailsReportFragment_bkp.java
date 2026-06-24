package acquire.core.fragment.common.report;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.core.constant.IntentParamKeyContent;
import acquire.core.constant.ReportConstant;
import acquire.core.databinding.CoreFragmentAllDetailsReportBinding;
import acquire.core.fragment.print.PrintViewModel;
import acquire.core.model.CardSchemeReportModel;
import acquire.core.model.FeatureSubMenuModel;
import acquire.core.model.SchemeGroup;
import acquire.core.report_data_factiry.CardDataFactory;
import acquire.core.trans.impl.print_detail.report.ReportsAdapter;
import acquire.core.trans.impl.print_detail.report.SchemeGroupAdapter;
import acquire.database.bean.TransactionSummary;
import acquire.database.model.Merchant;
import acquire.database.model.Record;
import acquire.database.repository.HistorySummaryRepository;
import acquire.database.repository.MerchantRepository;
import acquire.database.repository.RecordRepository;


public class AllDetailsReportFragment_bkp extends BaseFragment {

    private Button mBtnPrint;
    private CoreFragmentAllDetailsReportBinding binding;
    private Merchant mrcnDta;
    //private SimpleCallback callback;

    private String mTopReportTitle="";
    private String mBottomReportTitle="";


    public static AllDetailsReportFragment_bkp newInstance(FeatureSubMenuModel model) {

        AllDetailsReportFragment_bkp fragment = new AllDetailsReportFragment_bkp();
        Bundle bundle = new Bundle();
        //bundle.putSerializable(KEY, model);
        //bundle.putString(IntentParamKeyContent.TRANS_TITLE_TEXT, model.getTitle());
        //bundle.putString(KEY, model.getCode());
        fragment.setArguments(bundle);
        return fragment;
    }

    private Merchant getMerchantData() {
        MerchantRepository mrntRpo = new MerchantRepository();
//        Merchant mrcntObj = mrntRpo.findByType("DEFAULT");
        Merchant mrcntObj = mrntRpo.findByType("default");
        if (mrcntObj != null)
            LoggerUtils.i("newCall, Merchant: " + mrcntObj.toString());
        else LoggerUtils.i("newCall, Merchant: null ");

        return mrcntObj;
    }

    private List<Record> recordsData = new ArrayList<>();
    private List<CardSchemeReportModel> schemeList;
    private List<SchemeGroup> schemeGroupsData;
    private String reportCode;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = CoreFragmentAllDetailsReportBinding.inflate(inflater, container, false);

        setViewData();

        setAdapter();

        //listener
        initListener();

        return binding.getRoot();
    }

    private void setAdapter() {
        //Get record for report
        ReportsAdapter adapter = new ReportsAdapter(recordsData);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(requireContext());
        layoutManager1.setAutoMeasureEnabled(true);

        binding.rvTranType.setLayoutManager(layoutManager1);
        binding.rvTranType.setNestedScrollingEnabled(false);
        binding.rvTranType.setAdapter(adapter);

        recordsData.addAll(getRecordData(reportCode));
        adapter.notifyDataSetChanged();

        //Set List height
        int height = dpToPx(getContext(), 135*recordsData.size());
        binding.rvTranType.setMinimumHeight(height);

        binding.rvCardScheme.setLayoutManager(new LinearLayoutManager(requireContext()));
        schemeGroupsData = CardDataFactory.getSchemeGroups();
        SchemeGroupAdapter adapterScheme = new SchemeGroupAdapter(schemeGroupsData);
        binding.rvCardScheme.setAdapter(adapterScheme);

    }

    private void setViewData() {

        binding.toolbar.setBackListener(v -> getParentFragmentManager().popBackStack());

        String titleBarText = (String) getArguments().getString(IntentParamKeyContent.TRANS_TITLE_TEXT);
        FeatureSubMenuModel featureSubMenuModel = (FeatureSubMenuModel) getArguments().getSerializable(IntentParamKeyContent.TRANS_REPORT_MODEL_KEY);

        reportCode = featureSubMenuModel.getCode();
        if (titleBarText == null || titleBarText.isEmpty())
            titleBarText = "";
        if(reportCode.equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_PRE_AUTH_REPORT)){
            binding.tvTranTypeTitle.setText(titleBarText);
            binding.toolbar.setTitle("Details Report");
        }else {
            binding.toolbar.setTitle(titleBarText);
        }

        binding.rvTranType.setVisibility(View.VISIBLE);
        binding.tvTranTypeTitle.setVisibility(View.VISIBLE);
        binding.incMerchant.mcvRootMarchant.setVisibility(View.VISIBLE);


        //Set Merchant data
        mrcnDta = getMerchantData();
        setMerchantView(mrcnDta);

    }

    public int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }


/*    public List<CardSchemeReportModel> moveGrandTotalToBottom(List<CardSchemeReportModel> schemeList) {

        CardSchemeReportModel grandTotalObj = null;

        for (CardSchemeReportModel model : schemeList) {
            if ("GRAND TOTAL".equalsIgnoreCase(model.getScheme())) {
                grandTotalObj = model;
                break;
            }
        }

        if (grandTotalObj != null) {
            schemeList.remove(grandTotalObj);
            schemeList.add(grandTotalObj);
        }
        return schemeList;
    }

    public List<CardSchemeReportModel> generateSchemeReport(List<Record> records) {

        Map<String, CardSchemeReportModel> map = new HashMap<>();

        String grandTotalTitle = "GRAND TOTAL";
        long grandTotalSaleAmount = 0;
        long grandTotalVoidSaleAmount = 0;
        int grandTotalSaleCount = 0;
        int grandTotalVoidSaleCount = 0;
        int grandTotalCount = 0;
        long grandTotalAmount = 0;

        for (Record r : records) {
            String scheme = r.getCardScheme();
            boolean isOnUs = false;
            try {
                String ben = r.getCardNo().substring(0, 6);
                String cardTitle = OnUsBinMap.REPORT_CARD_ONUS_MAP.get(ben).getCardTitle();
                if (cardTitle != null && !cardTitle.isEmpty()) {
                    isOnUs = true;
                }
            } catch (Exception ex) {

            }

            if (isOnUs) scheme = scheme + " ONUS";
            else scheme = scheme + " OFFUS";
            if (scheme != null) {
                if (!map.containsKey(scheme)) {
                    CardSchemeReportModel m = new CardSchemeReportModel();
                    m.setScheme(scheme);
                    map.put(scheme, m);
                }
                CardSchemeReportModel model = map.get(scheme);
                if ("SALE".equalsIgnoreCase(r.getTransType())) {
                    ++grandTotalSaleCount;
                    ++grandTotalCount;
                    model.setSaleCount(model.getSaleCount() + 1);
                    grandTotalSaleAmount += r.getAmount();
                    model.setSaleAmount(model.getSaleAmount() + r.getAmount());
                }
                if ("VoidSale".equalsIgnoreCase(r.getTransType())
                        || "VOID SALE".equalsIgnoreCase(r.getTransType())) {
                    ++grandTotalVoidSaleCount;
                    ++grandTotalCount;
                    grandTotalVoidSaleAmount += r.getAmount();
                    model.setVoidCount(model.getVoidCount() + 1);
                    model.setVoidAmount(model.getVoidAmount() + r.getAmount());
                }
            }
        }

        CardSchemeReportModel m = new CardSchemeReportModel();
        m.setScheme(grandTotalTitle);

        m.setSaleCount(grandTotalSaleCount);
        m.setSaleAmount(grandTotalSaleAmount);

        m.setVoidCount(grandTotalVoidSaleCount);
        m.setVoidAmount(grandTotalVoidSaleAmount);

        //grandTotalAmount = grandTotalSaleAmount - grandTotalVoidSaleAmount;
        m.setVoidAmount(grandTotalVoidSaleAmount);

        map.put(grandTotalTitle, m);

        return new ArrayList<>(map.values());
    }*/

    private ArrayList<Record> getRecordData(String reportCode) {
        this.reportCode =reportCode;
        FeatureSubMenuModel featureSubMenuModel;
        RecordRepository repoObj = new RecordRepository();
//        Merchant mrcntObj = mrntRpo.findByType("DEFAULT");
        List<Record> recordsObj = new ArrayList<Record>();
        if(reportCode.equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_DETAILS_REPORT)){
            mTopReportTitle = "DETAILS REPORT(SALE)";
            mBottomReportTitle = "END OF DETAILS REPORT(SALE)";
            recordsObj = repoObj.findAllSalePreAuthAndVoid();
            //recordsObj = repoObj.findAllSaleAndVoid();
//            recordsObj = repoObj.findAll();
        }
        else if(reportCode.equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_BATCH_TOTALS)){
            //recordsObj = repoObj.findAllReport();
        }
        else if(reportCode.equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_VOID_REPORT)){
            mTopReportTitle = "DETAILS REPORT(VOID SALE)";
            mBottomReportTitle = "END OF VOID-SALE REPORT";
            recordsObj = repoObj.findAllVoidReport();;
        }
        else if(reportCode.equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_CARD_REPORT)){
            binding.rvTranType.setVisibility(View.GONE);
            binding.tvTranTypeTitle.setVisibility(View.GONE);
            mTopReportTitle = "DETAILS REPORT(SALE & VOID)";
            mBottomReportTitle = "END OF CARD REPORT";
//            binding.incMerchant.mcvRootMarchant.setVisibility(View.GONE);
            recordsObj = repoObj.findAllReport();
        }
        else if(reportCode.equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_PRE_AUTH_REPORT)){
            mTopReportTitle = "PRE-AUTH REPORT";
            mBottomReportTitle = "END OF PRE-AUTH REPORT";
            recordsObj = repoObj.findAllPreAuth();
            //recordsObj = repoObj.findAll();
        }
        else if(reportCode.equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_LAST_SETTLE)){
            //recordsObj = repoObj.findAllReport();
        }
        else if(reportCode.equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_REVERSAL_REPORT)){
            //recordsObj = repoObj.findAllReport();
        }

        else if(reportCode.equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_DECLINE_REPORT)){
            //recordsObj = repoObj.findAllReport();
        }

        else if(reportCode.equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_RRE_AUTH_VOID_REPORT)){
            mTopReportTitle = "VOID PRE-AUTH REPORT";
            mBottomReportTitle = "END OF VOID PRE-AUTH REPORT";
            recordsObj = repoObj.findAllVoidPreAuth();
        }
        else if(reportCode.equalsIgnoreCase(ReportConstant.REPORT_ITEM_CODE_RRE_AUTH_OTHER)){
            //recordsObj = repoObj.findAllReport();
        }


        if (recordsObj != null) {
            LoggerUtils.i("newCall, Records: " + recordsObj.size());

         /*   for(int i=0; i<recordsObj.size();i++){
                Gson gson2 = new Gson();
                String json = gson2.toJson(recordsObj.get(i));
                LoggerUtils.i("newCall, Records: " + json);
            }*/

        } else LoggerUtils.i("newCall, Records: null ");

        return (ArrayList<Record>)recordsObj;
    }

    private FeatureSubMenuModel getHistoryData(Merchant mrcnDta) {
        FeatureSubMenuModel featureSubMenuModel;
        HistorySummaryRepository repoObj = new HistorySummaryRepository();
//        Merchant mrcntObj = mrntRpo.findByType("DEFAULT");
        List<TransactionSummary> historyMsg = repoObj.getTransactionSummaries(mrcnDta);
        if (historyMsg != null)
            LoggerUtils.i("newCall, historyMsg: " + historyMsg.size());
        else LoggerUtils.i("newCall, Records: null ");

        return null;
    }
/*    private ReportModel getHistoryData(Merchant mrcnDta) {
        ReportModel reportModel;
        HistorySummaryRepository repoObj = new HistorySummaryRepository();
//        Merchant mrcntObj = mrntRpo.findByType("DEFAULT");
        List<TransactionSummary> historyMsg = repoObj.getTransactionSummaries(mrcnDta);
        if(historyMsg!=null)
            LoggerUtils.i("newCall, historyMsg: "+historyMsg.size());
        else LoggerUtils.i("newCall, Records: null ");

        return null;
    }*/

    private void setMerchantView(Merchant mrcnDta) {

        binding.incMerchant.tvMrcnNameValue.setText("");
        if (mrcnDta.getMerchantName() != null) {
            binding.incMerchant.tvMrcnNameValue.setText(mrcnDta.getMerchantName() + "");
        }

        binding.incMerchant.tvMrcnIdVal.setText("");
        if (mrcnDta.getMid() != null) {
            binding.incMerchant.tvMrcnIdVal.setText(mrcnDta.getMid() + "");
        }

        binding.incMerchant.tvTerminalIdVal.setText("");
        if (mrcnDta.getTid() != null) {
            binding.incMerchant.tvTerminalIdVal.setText(mrcnDta.getTid() + "");
        }

        binding.incMerchant.tvBtcIdVal.setText("");
        if (mrcnDta.getBatchNo() != null) {
            binding.incMerchant.tvBtcIdVal.setText(mrcnDta.getBatchNo() + "");
        }


    }

    private void initListener() {
        binding.btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printReport(recordsData, schemeList);
            }
        });
    }



    private void printReport(List<Record> recordsData, List<CardSchemeReportModel> schemeList) {

        if (recordsData == null && schemeList == null) {

            return;
        }

        PrintViewModel printViewModel;
        printViewModel = new ViewModelProvider(this).get(PrintViewModel.class);
        printViewModel.printAllDetails(mTopReportTitle, mBottomReportTitle, this.reportCode, recordsData, schemeList);

        printViewModel.getReceipt().observe(getViewLifecycleOwner(), bitmap -> {

        });
    }

    @Override
    public FragmentCallback getCallback() {
        return null;
    }

    /*@Override
    public FragmentCallback getCallback() {
        return null;//callback;
    }*/
}