package acquire.core.fragment.record;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.DateUtils;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.StringUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.core.constant.TransStatus;
import acquire.core.databinding.CoreFragmentSearchBinding;
import acquire.core.tools.TransUtils;
import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;

/**
 * A {@link Fragment} to search record
 *
 * @author Janson
 * @date 2019/7/25 11:20
 */
public class SearchFragment extends BaseFragment {
    private final RecordRepository recordRepository = new RecordRepository();

    private CoreFragmentSearchBinding binding;
    private FragmentCallback<Record> mCallback;
    private String[] mTransTypes;
    private Record record;
    private int[] mStatus ;
    @NonNull
    public static SearchFragment newInstance(FragmentCallback<Record> callback,int[] status, String[]transTypes) {
        SearchFragment fragment = new SearchFragment();
        fragment.mCallback = callback;
        fragment.mTransTypes = transTypes;
        fragment.mStatus = status;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentSearchBinding.inflate(inflater, container, false);
        DisplayUtils.fitsWindowStatus(binding.llToolbar);
        binding.ivBack.setOnClickListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        binding.item.getRoot().setOnClickListener(v->{
            if (mCallback != null){
                mCallback.onSuccess(record);
            }
        });
        //search record
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)){
                    return true;
                }
                record = recordRepository.findByTrace(StringUtils.fill(newText,"0",6,true));
                if (record == null){
                    showNothing();
                    return true;
                }
                if (mTransTypes != null && mTransTypes.length != 0){
                    boolean match = false;
                    for (String transType : mTransTypes) {
                        if (record.getTransType().equals(transType)){
                            match = true;
                            break;
                        }
                    }
                    if (!match){
                        showNothing();
                        return true;
                    }
                }
                if (mStatus != null && mStatus.length != 0){
                    boolean match = false;
                    for (int status : mStatus) {
                        if (record.getStatus() == status){
                            match = true;
                            break;
                        }
                    }
                    if (!match){
                        showNothing();
                        return true;
                    }
                }
                showRecord(record);
                return true;
            }
        });
        return binding.getRoot();
    }

    private void showRecord(Record record){
        //Trans name
        String transName = TransUtils.getName(record.getTransType());
        binding.item.tvTransType.setText(transName);

        //status
        if (record.getStatus() != TransStatus.SUCCESS){
            binding.item.tvStatus.setVisibility(View.VISIBLE);
            binding.item.tvStatus.setText("["+TransStatus.getDescription(record.getStatus())+"]");
        }else{
            binding.item.tvStatus.setVisibility(View.GONE);
        }
        //Amount
        String amt = FormatUtils.formatAmount(record.getAmount(), 2, "");
        String identify = CurrencyUtils.getCurrencySymbol(record.getCurrencyCode());
        amt = identify + amt;
        binding.item.tvAmount.setText(amt);
        //Time
        binding.item.tvTime.setText(DateUtils.formatTime(record.getDate() + record.getTime()));
        //Trace
        binding.item.tvTraceNo.setText(record.getTraceNo());
        binding.llNoItem.setVisibility(View.GONE);
        binding.item.getRoot().setVisibility(View.VISIBLE);
    }

    private void showNothing(){
        binding.llNoItem.setVisibility(View.VISIBLE);
        binding.item.getRoot().setVisibility(View.GONE);
    }


    @Override
    public FragmentCallback<Record> getCallback() {
        return mCallback;
    }

}
