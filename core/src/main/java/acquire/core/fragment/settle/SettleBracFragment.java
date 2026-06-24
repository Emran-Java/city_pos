package acquire.core.fragment.settle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import acquire.base.BaseApplication;
import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ToastUtils;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentSettleBracBinding;
import acquire.core.fragment.common.DataLoader;
import acquire.core.model.FeatureSubMenuModel;
import acquire.core.model.SchemeGroup;
import acquire.core.report_data_factiry.CardDataFactory;
import acquire.core.trans.impl.print_detail.report.SchemeGroupAdapter;
import acquire.database.model.Merchant;
import acquire.database.model.Record;
import acquire.database.repository.MerchantRepository;
import acquire.database.repository.RecordRepository;

/**
 * A settle {@link androidx.fragment.app.Fragment}
 *
 * @author Janson
 * @date 2021/7/20 10:07
 */
public class SettleBracFragment extends BaseFragment {


    private final RecordRepository recordRepository = new RecordRepository();
    private final MerchantRepository merchantRepository = new MerchantRepository();
    private CoreFragmentSettleBracBinding binding;
    private FragmentCallback<List<Merchant>> callback;

    //emi

    private List<Record> emiRecordsData = new ArrayList<>();
    private List<SchemeGroup> emiSchemeGroupsData = new ArrayList<>();
    //--------

    private boolean isPresentDefaultMerchant = false, isPresentEmiMerchant = false;

    private List<Record> recordsData = new ArrayList<>();
    private List<SchemeGroup> defaultSchemeGroups;
    private SettleGrandTotalAdapter defaultMerchantGrandTotalAdapter;
    private String reportCode;
    private Merchant defaultMerchantDta;

    boolean isDefaultDataLoadDone = false;
    boolean isEMIDataLoadDone2 = false;

    public static SettleBracFragment newInstance(FragmentCallback<List<Merchant>> callback) {
        SettleBracFragment fragment = new SettleBracFragment();
        fragment.callback = callback;
        return fragment;
    }

    boolean isRecordDataEmpty = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentSettleBracBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle());
        binding.toolbar.setBackListener(v -> mActivity.getOnBackPressedDispatcher().onBackPressed());

        DataLoader.getInstance().show(
                requireContext(),
                "Loading Settlement data set",
                "Fetching transaction details..."
        );

        defaultMerchantDta = getMerchantData();
        //Set Merchant data
        setDefMerchantView(defaultMerchantDta);

        defaultMerchantGrandTotalAdapter = new SettleGrandTotalAdapter(new ArrayList<>());
        binding.rvDefaultMerchantsGrandTotal.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDefaultMerchantsGrandTotal.setAdapter(defaultMerchantGrandTotalAdapter);

        binding.rvCardScheme.setLayoutManager(new LinearLayoutManager(requireContext()));
        SchemeGroupAdapter schemeAdapter = new SchemeGroupAdapter(new ArrayList<>());
        binding.rvCardScheme.setAdapter(schemeAdapter);
        //-------------------

        //EMI
        //PayFlex
        SettleGrandTotalAdapter emiReportsAdapter = new SettleGrandTotalAdapter(new ArrayList<>());
        binding.incEmiDetails.rvEmiTranType.setLayoutManager(new LinearLayoutManager(requireContext()));
        //binding.incEmiDetails.rvEmiTranType.setNestedScrollingEnabled(false);
        binding.incEmiDetails.rvEmiTranType.setAdapter(emiReportsAdapter);

        SchemeGroupAdapter emiSchemeAdapter = new SchemeGroupAdapter(new ArrayList<>());
        binding.incEmiDetails.rvEmiCardScheme.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.incEmiDetails.rvEmiCardScheme.setAdapter(emiSchemeAdapter);
        //-----------------------

        //Hide EMI part
        shoHidePayFlexUI(false);

        BaseApplication.SINGLE_EXECUTOR.execute(() -> {
            if (recordRepository.getBracSettleCount() == 0) {
                isPresentDefaultMerchant = false;
            } else {
                isPresentDefaultMerchant = true;
                defaultSchemeGroups = CardDataFactory.getSettleSchemeGroups();
            }

            List<SchemeGroup> defMrchnGrandTotal = CardDataFactory.getSettleTypeWiseTotal();

            //EMI
            if (recordRepository.getBracSettleCountEmi() == 0) {
                isPresentEmiMerchant = false;
            } else {
                isPresentEmiMerchant = true;
            }

            List<SchemeGroup> emiMrchnGrandTotal = CardDataFactory.getSettleEmiTypeWizeTotal();
            List<SchemeGroup> emiSchemeGroups = CardDataFactory.getEmiSchemeGroups();

            requireActivity().runOnUiThread(() -> {
                defaultMerchantGrandTotalAdapter.updateData(defMrchnGrandTotal);
                schemeAdapter.updateData(defaultSchemeGroups);

                emiReportsAdapter.updateData(emiMrchnGrandTotal);
                emiSchemeAdapter.updateData(emiSchemeGroups);


                binding.tvCardReportTitle.setVisibility(View.VISIBLE);

                //EMI
                shoHidePayFlexUI(!emiMrchnGrandTotal.isEmpty() || !emiSchemeGroups.isEmpty());

                isDefaultDataLoadDone = true;
                isEMIDataLoadDone2 = true;

                hideLoader();
            });
        });

        binding.incMerchant.mcvRootMarchant.setVisibility(View.VISIBLE);

        setEmiMerchantView();
        //binding.tvCardReportTitle.setVisibility(View.GONE);
        //-------------------

        clickListener();
        //-------------
        return binding.getRoot();
    }

    private void hideLoader() {
        if (isEMIDataLoadDone2 && isDefaultDataLoadDone)
            DataLoader.getInstance().dismiss();
    }

    private void clickListener() {

        binding.btnSettle.setOnClickListener(v -> {

            getMerchantsToCallBack();

        });
    }

    private void getMerchantsToCallBack() {
        if (!isPresentDefaultMerchant && !isPresentEmiMerchant) {
            ToastUtils.showToast(R.string.core_settle_no_record_to_settled);
            return;
        }

        List<Merchant> merchants = new ArrayList<>();
        if (isPresentDefaultMerchant) {
            List<Merchant> merchantsDef = merchantRepository.findDefaultMerchant();
            merchants.addAll(merchantsDef);
        }

        if (isPresentEmiMerchant) {
            List<Merchant> merchantsEmi = merchantRepository.findEmiMerchant();
            merchants.addAll(merchantsEmi);
        }

        callback.onSuccess(merchants);

    }

    private void shoHidePayFlexUI(boolean isShow) {
        if (isShow) {
            binding.incEmiDetails.llEmiRootDetails.setVisibility(View.VISIBLE);
        } else {
            binding.incEmiDetails.llEmiRootDetails.setVisibility(View.GONE);
        }
    }

    private ArrayList<Record> getEmiRecordData() {

        FeatureSubMenuModel featureSubMenuModel;
        RecordRepository repoObj = new RecordRepository();
//        Merchant mrcntObj = mrntRpo.findByType("DEFAULT");
        List<Record> recordsObj = new ArrayList<Record>();

        recordsObj = repoObj.findAllPayFlex();

        if (recordsObj != null) {
            LoggerUtils.i("newCall, Records: " + recordsObj.size());

         /*   for(int i=0; i<recordsObj.size();i++){
                Gson gson2 = new Gson();
                String json = gson2.toJson(recordsObj.get(i));
                LoggerUtils.i("newCall, Records: " + json);
            }*/

        } else LoggerUtils.i("newCall, Records: null ");

        return (ArrayList<Record>) recordsObj;
    }

    private ArrayList<Record> getRecordData() {
        FeatureSubMenuModel featureSubMenuModel;
        RecordRepository repoObj = new RecordRepository();
//        Merchant mrcntObj = mrntRpo.findByType("DEFAULT");
        List<Record> recordsObj = new ArrayList<Record>();

        recordsObj = repoObj.findAllSalePreAuthComplete();

        if (recordsObj != null) {
            LoggerUtils.i("newCall, Records: " + recordsObj.size());

         /*   for(int i=0; i<recordsObj.size();i++){
                Gson gson2 = new Gson();
                String json = gson2.toJson(recordsObj.get(i));
                LoggerUtils.i("newCall, Records: " + json);
            }*/

        } else LoggerUtils.i("newCall, Records: null ");

        return (ArrayList<Record>) recordsObj;
    }


    private void setEmiMerchantView() {
        MerchantRepository mrntRpo = new MerchantRepository();
        Merchant merchant = mrntRpo.findByType("EMI");

        if (merchant == null) return;

        binding.incEmiDetails.incEmiMerchant.tvMrcnNameValue.setText(
                merchant.getMerchantName() != null ? merchant.getMerchantName() : "");

        binding.incEmiDetails.incEmiMerchant.tvMrcnIdVal.setText(
                merchant.getMid() != null ? merchant.getMid() : "");

        binding.incEmiDetails.incEmiMerchant.tvTerminalIdVal.setText(
                merchant.getTid() != null ? merchant.getTid() : "");

        binding.incEmiDetails.incEmiMerchant.tvBtcIdVal.setText(
                merchant.getBatchNo() != null ? merchant.getBatchNo() : "");

        shoHidePayFlexUI(true);
    }


    private Merchant getMerchantData() {
        MerchantRepository mrntRpo = new MerchantRepository();
        Merchant mrcntObj = mrntRpo.findByType("default");
        if (mrcntObj != null)
            LoggerUtils.i("newCall, Merchant: " + mrcntObj.toString());
        else LoggerUtils.i("newCall, Merchant: Default null ");

        return mrcntObj;
    }

    private void setDefMerchantView(Merchant mrcnDta) {

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DataLoader.getInstance().dismiss();
        binding = null;
    }

    @Override
    public FragmentCallback<List<Merchant>> getCallback() {
        return callback;
    }
/*

    class SettleMerchantAdapter extends BaseBindingRecyclerAdapter<CoreSettleMerchantItemBinding> {
        private final List<Merchant> merchants;
        private final List<Merchant> selects = new ArrayList<>();

        public SettleMerchantAdapter(List<Merchant> merchants) {
            this.merchants = merchants;
        }

        public void setAll(boolean checked) {
            selects.clear();
            if (checked) {
                selects.addAll(merchants);
            }
            notifyDataSetChanged();
        }

        public List<Merchant> getSelects() {
            return selects;
        }

        private List<String> getTransTypes() {
            List<String> result = new ArrayList<>();
            for (Field field : TransType.class.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if ((Modifier.PUBLIC & modifiers) != 0 && (Modifier.STATIC & modifiers) != 0) {
                    try {
                        String transType = (String) field.get(TransType.class);
                        if ((transType.equalsIgnoreCase(TransType.TRANS_SALE) || transType.equalsIgnoreCase(TransType.TRANS_AUTH_COMPLETE) ) && TransUtils.getSettleAttr(transType) != SettleAttr.NONE) {
                            result.add(transType);
                        }
                    } catch (IllegalAccessException e) {
                        LoggerUtils.e(field+" get "+TransType.class.getSimpleName()+" failed!",e);
                    }
                }
            }
            result.sort((t1, t2) -> {
                if (t1.equals(TransType.TRANS_SALE)) {
                    return -1;
                } else if (t2.equals(TransType.TRANS_SALE)) {
                    return 1;
                }
                return t1.compareTo(t2);
            });
            return result;
        }

        @Override
        protected void bindItemData(CoreSettleMerchantItemBinding itemBinding, int position) {
            Merchant merchant = merchants.get(position);
            itemBinding.cbType.setText(merchant.getType());
            itemBinding.cbType.setVisibility(View.GONE);
            //Add transaction to be settled
            List<String> transTypes = getTransTypes();
            //Count the total amount of these transactions
            long totalAmount = 0;
            int totalNumber = 0;

            long totalVoidAmount = 0;
            int totalVoidNumber = 0;


//            List<TransactionSummary> transactionSummaries = recordRepository.getTransactionSummary(merchant.getMid(),merchant.getTid());
            List<TransactionSummary> transactionSummaries = recordRepository.getBracTransactionSummary(merchant.getMid(),merchant.getTid());

            List<TransItem> items = new ArrayList<>();
            for (String transType : transTypes) {
                TransactionSummary summary = new TransactionSummary();
                for (TransactionSummary transactionSummary : transactionSummaries) {
                    if (transType.equals(transactionSummary.getTransType())){
                        summary = transactionSummary;
                    }
                }
                String name = TransUtils.getName(transType);

                if(transType.contains("Void")){
                    totalVoidAmount += summary.getAmount();
                    totalVoidNumber += summary.getCount();
                }

                int settleAttr = TransUtils.getSettleAttr(transType);
                switch (settleAttr) {
                    case SettleAttr.PLUS:
                        totalAmount += summary.getAmount();
                        break;
                    case SettleAttr.REDUCE:
                        totalAmount -= summary.getAmount();
                        summary.setAmount(-summary.getAmount());
                        break;
                    default:
                        continue;
                }
                items.add(new TransItem(name, summary.getCount(), summary.getAmount()));
                totalNumber += summary.getCount();
            }
            int countTotal = totalNumber-totalVoidNumber;
            if(countTotal<0) countTotal=countTotal*-1;

            items.add(new TransItem("GRAND TOTAL", countTotal, totalAmount+totalVoidAmount));

            //below lines test developing time,
            */
/*SettleData amountObj = SettleData.getInstance();
            amountObj.setTotalAmount(totalAmount);
            amountObj.setTotalNumber(totalNumber);*//*

            //---------------------------------


            //Transaction items
            itemBinding.rvRecords.setAdapter(new SettleAdapter(items));

            itemBinding.cbType.setChecked(selects.contains(merchant));
            itemBinding.cbType.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selects.contains(merchant)) {
                        selects.add(merchant);
                        if (selects.size() == merchants.size()) {
                          //  binding.cbAll.setChecked(true);
                        }
                    }
                } else {
                    selects.remove(merchant);
                 //   binding.cbAll.setChecked(false);
                }
            });
        }

        @Override
        public int getItemCount() {
            return merchants.size();
        }
    }


    */
/**
 * Settle adapter
 *
 * @author Janson
 * @date 2021/3/15 13:48
 *//*

    static class SettleAdapter extends BaseBindingRecyclerAdapter<CoreSettleItemBinding> {
        private final List<TransItem> items;

        SettleAdapter(List<TransItem> items) {
            this.items = items;
        }

        @Override
        protected void bindItemData(CoreSettleItemBinding itemBinding, int position) {
            */
/*if (position % 2 == 0) {
                itemBinding.getRoot().setBackgroundResource(R.color.core_settle_item_gray);
            }
            *//*

            //itemBinding.getRoot().setBackgroundResource(R.color.core_settle_item_gray);
            TransItem item = items.get(position);
            itemBinding.tvName.setText(item.name);
            itemBinding.tvNumber.setText(String.format(Locale.getDefault(), "%d", item.number));
            itemBinding.tvAmount.setText(FormatUtils.formatAmount(item.amount));

            int txtColor = 0xFF666666;

            if(items.size()==position+1){
                itemBinding.tvName.setTypeface(null, Typeface.BOLD);
                itemBinding.tvName.setTextColor(0xff267D47);
                itemBinding.tvNumber.setTypeface(null, Typeface.BOLD);
                itemBinding.tvNumber.setTextColor(0xff267D47);
                itemBinding.tvAmount.setTypeface(null, Typeface.BOLD);
                itemBinding.tvAmount.setTextColor(0xff267D47);
            }
            else{
                itemBinding.tvName.setTypeface(null, Typeface.NORMAL);
                itemBinding.tvNumber.setTypeface(null, Typeface.NORMAL);
                itemBinding.tvAmount.setTypeface(null, Typeface.NORMAL);

                itemBinding.tvName.setTextColor(txtColor);
                itemBinding.tvNumber.setTextColor(txtColor);
                itemBinding.tvAmount.setTextColor(txtColor);
            }
        }


        @Override
        public int getItemCount() {
            return items.size();
        }
    }
*/

    /**
     * Transaction settle info
     *
     * @author Janson
     * @date 2020/6/23 19:16
     */
/*    static class TransItem {
        private final String name;
        private final long number;
        private final long amount;

        TransItem(String name, long number, long amount) {
            this.name = name;
            this.number = number;
            this.amount = amount;
        }
    }*/
}
