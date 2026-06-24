package acquire.core.fragment.record;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.DateUtils;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.core.R;
import acquire.core.constant.TransStatus;
import acquire.core.databinding.CoreFragmentRecordDetailBinding;
import acquire.core.databinding.CoreRecordDetailItemBinding;
import acquire.core.tools.TransUtils;
import acquire.database.model.Record;
import acquire.database.repository.PreStepVoidAbleRecordData;

/**
 * A {@link Fragment} that displays the record detail.
 *
 * @author Janson
 * @date 2019/7/25 11:23
 */
public class RecordTipAdjustDetailFragment extends BaseFragment {
    private FragmentCallback<Void> mCallback;
    private Record mRecord;
    private String buttonText;

    @NonNull
    public static RecordTipAdjustDetailFragment newInstance(String buttonText, Record record, FragmentCallback<Void> callback) {
        RecordTipAdjustDetailFragment fragment = new RecordTipAdjustDetailFragment();
        fragment.mCallback = callback;
        fragment.mRecord = record;
        fragment.buttonText = buttonText;
        return fragment;
    }

    @NonNull
    public static RecordTipAdjustDetailFragment newInstance(Record record, FragmentCallback<Void> callback) {
        RecordTipAdjustDetailFragment fragment = new RecordTipAdjustDetailFragment();
        fragment.mCallback = callback;
        fragment.mRecord = record;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CoreFragmentRecordDetailBinding binding = CoreFragmentRecordDetailBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle().toString().toUpperCase());
        binding.toolbar.setBackListener(v -> mActivity.getOnBackPressedDispatcher().onBackPressed());

        /*String identify = CurrencyUtils.getCurrencySymbol(mRecord.getCurrencyCode());
        String formatAmt = identify + FormatUtils.formatAmount(mRecord.getAmount());
        binding.tvAmount.setText(formatAmt);
        binding.tvTransName.setText(TransUtils.getName(mRecord.getTransType()));
        binding.tvTransStatus.setText(TransStatus.getDescription(mRecord.getStatus()));

        List<DetailItem> items = new ArrayList<>();
        items.add(new DetailItem(R.string.core_record_detail_name_pan, FormatUtils.maskCardNo(mRecord.getCardNo())));
        items.add(new DetailItem(R.string.core_record_detail_name_scheme, mRecord.getCardScheme()));
        items.add(new DetailItem(R.string.core_record_detail_name_trace, mRecord.getTraceNo()));
        items.add(new DetailItem(R.string.core_record_detail_name_batch, mRecord.getBatchNo()));
        items.add(new DetailItem(R.string.core_record_detail_name_refnum, mRecord.getReferNo()));
        items.add(new DetailItem(R.string.core_record_detail_name_authcode, mRecord.getAuthCode()));
        items.add(new DetailItem(R.string.core_record_detail_name_time, DateUtils.formatTime(mRecord.getDate() + mRecord.getTime())));
*/
        String identify = CurrencyUtils.getCurrencySymbol(mRecord.getCurrencyCode());
        identify = "TK ";
        String formatAmt = identify + FormatUtils.formatAmount(mRecord.getAmount());
        binding.tvAmount.setVisibility(View.GONE);
        binding.tvTransName.setVisibility(View.GONE);
        binding.tvTransStatus.setVisibility(View.GONE);

        //binding.tvAmount.setText(formatAmt);
        //binding.tvTransName.setText(TransUtils.getName(mRecord.getTransType()));
        //binding.tvTransStatus.setText(TransStatus.getDescription(mRecord.getStatus()));
        List<DetailItem> items = new ArrayList<>();

        items.add(new DetailItem(R.string.core_record_detail_tran_type, TransUtils.getName(mRecord.getTransType()), true));
        items.add(new DetailItem(R.string.core_record_detail_amount, formatAmt, true));
        items.add(new DetailItem(R.string.core_record_detail_name_pan, FormatUtils.maskCardNo(mRecord.getCardNo())));

        items.add(new DetailItem(R.string.core_record_detail_name_scheme, mRecord.getCardScheme()));
        items.add(new DetailItem(R.string.core_record_detail_name_authcode, mRecord.getAuthCode()));
        items.add(new DetailItem(R.string.core_record_detail_name_refnum, mRecord.getReferNo()));
//        items.add(new DetailItem(R.string.core_record_detail_name_batch, mRecord.getBatchNo()));
        items.add(new DetailItem(R.string.core_record_detail_name_trace, mRecord.getTraceNo(), true));
        items.add(new DetailItem(R.string.core_record_detail_tran_date_name_time, DateUtils.formatTime(mRecord.getDate() + mRecord.getTime())));

        binding.rvDetail.setAdapter(new DetailAdapter(items));
        if (buttonText != null) {
            binding.btnConfirm.setText(buttonText);
        }
        binding.btnConfirm.setOnClickListener(v -> {
                    PreStepVoidAbleRecordData.preStepVoidRecord = mRecord;
                    mCallback.onSuccess(null);
                }
        );
        return binding.getRoot();
    }


    @Override
    public FragmentCallback<Void> getCallback() {
        return mCallback;
    }

    private static class DetailAdapter extends BaseBindingRecyclerAdapter<CoreRecordDetailItemBinding> {
        private final List<DetailItem> items;

        DetailAdapter(List<DetailItem> items) {
            this.items = items;
        }

        @Override
        protected void bindItemData(@NonNull CoreRecordDetailItemBinding itemBinding, int position) {
            DetailItem item = items.get(position);
            itemBinding.tvName.setText(item.name);
            itemBinding.tvValue.setText(item.value);
            if(item.isBold){
                itemBinding.tvName.setTypeface(null, Typeface.BOLD);
                itemBinding.tvValue.setTypeface(null, Typeface.BOLD);
            }else{
                itemBinding.tvName.setTypeface(null, Typeface.NORMAL);
                itemBinding.tvValue.setTypeface(null, Typeface.NORMAL);
            }
        }


        @Override
        public int getItemCount() {
            return items.size();
        }
    }


    private static class DetailItem {
        private @StringRes
        final int name;
        private final String value;
        private final boolean isBold;

        DetailItem(@StringRes int name, String value) {
            this.name = name;
            this.value = value;
            this.isBold = false;

        }
        DetailItem(@StringRes int name, String value, boolean isBold) {
            this.name = name;
            this.value = value;
            this.isBold = isBold;

        }
    }

}
