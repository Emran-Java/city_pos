package acquire.core.fragment.settle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.core.R;
import acquire.core.constant.SettleAttr;
import acquire.core.constant.TransType;
import acquire.core.databinding.CoreFragmentSettleBinding;
import acquire.core.databinding.CoreSettleItemBinding;
import acquire.core.databinding.CoreSettleMerchantItemBinding;
import acquire.core.tools.TransUtils;
import acquire.database.bean.TransactionSummary;
import acquire.database.model.Merchant;
import acquire.database.repository.MerchantRepository;
import acquire.database.repository.RecordRepository;

/**
 * A settle {@link androidx.fragment.app.Fragment}
 *
 * @author Janson
 * @date 2021/7/20 10:07
 */
public class SettleFragment_bkp extends BaseFragment {
    private final RecordRepository recordRepository = new RecordRepository();
    private final MerchantRepository merchantRepository = new MerchantRepository();
    private CoreFragmentSettleBinding binding;
    private FragmentCallback<List<Merchant>> callback;

    public static SettleFragment_bkp newInstance(FragmentCallback<List<Merchant>> callback) {
        SettleFragment_bkp fragment = new SettleFragment_bkp();
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentSettleBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle());
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        List<Merchant> merchants = merchantRepository.findAll();
        SettleMerchantAdapter adapter = new SettleMerchantAdapter(merchants);
        binding.rvMerchants.setAdapter(adapter);

        binding.btnSettle.setOnClickListener(v -> {
            if (recordRepository.getCount() == 0 || adapter.getSelects().isEmpty()) {
                ToastUtils.showToast(R.string.core_settle_no_record_to_settled);
                return;
            }
            callback.onSuccess(adapter.getSelects());
        });
        binding.cbAll.setOnClickListener(v -> adapter.setAll(binding.cbAll.isChecked()));
        return binding.getRoot();
    }

    @Override
    public FragmentCallback<List<Merchant>> getCallback() {
        return callback;
    }

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
                        if (TransUtils.getSettleAttr(transType) != SettleAttr.NONE) {
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
            //Add transaction to be settled
            List<String> transTypes = getTransTypes();
            //Count the total amount of these transactions
            long totalAmount = 0;
            long totalNumber = 0;
            List<TransactionSummary> transactionSummaries = recordRepository.getTransactionSummary(merchant.getMid(),merchant.getTid());
            List<TransItem> items = new ArrayList<>();
            for (String transType : transTypes) {
                TransactionSummary summary = new TransactionSummary();
                for (TransactionSummary transactionSummary : transactionSummaries) {
                    if (transType.equals(transactionSummary.getTransType())){
                        summary = transactionSummary;
                    }
                }
                String name = TransUtils.getName(transType);
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
            items.add(new TransItem(mActivity.getString(R.string.core_settle_total), totalNumber, totalAmount));

            //below lines test developing time,
            /*SettleData amountObj = SettleData.getInstance();
            amountObj.setTotalAmount(totalAmount);
            amountObj.setTotalNumber(totalNumber);*/
            //---------------------------------


            //Transaction items
            itemBinding.rvRecords.setAdapter(new SettleAdapter(items));

            itemBinding.cbType.setChecked(selects.contains(merchant));
            itemBinding.cbType.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selects.contains(merchant)) {
                        selects.add(merchant);
                        if (selects.size() == merchants.size()) {
                            binding.cbAll.setChecked(true);
                        }
                    }
                } else {
                    selects.remove(merchant);
                    binding.cbAll.setChecked(false);
                }
            });
        }

        @Override
        public int getItemCount() {
            return merchants.size();
        }
    }


    /**
     * Settle adapter
     *
     * @author Janson
     * @date 2021/3/15 13:48
     */
    static class SettleAdapter extends BaseBindingRecyclerAdapter<CoreSettleItemBinding> {
        private final List<TransItem> items;

        SettleAdapter(List<TransItem> items) {
            this.items = items;
        }

        @Override
        protected void bindItemData(CoreSettleItemBinding itemBinding, int position) {
            if (position % 2 == 0) {
                itemBinding.getRoot().setBackgroundResource(R.color.core_settle_item_gray);
            }
            TransItem item = items.get(position);
            itemBinding.tvName.setText(item.name);
            itemBinding.tvNumber.setText(String.format(Locale.getDefault(), "%d", item.number));
            itemBinding.tvAmount.setText(FormatUtils.formatAmount(item.amount));
        }


        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    /**
     * Transaction settle info
     *
     * @author Janson
     * @date 2020/6/23 19:16
     */
    static class TransItem {
        private final String name;
        private final long number;
        private final long amount;

        TransItem(String name, long number, long amount) {
            this.name = name;
            this.number = number;
            this.amount = amount;
        }
    }
}
