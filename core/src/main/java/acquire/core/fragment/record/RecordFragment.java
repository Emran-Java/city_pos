package acquire.core.fragment.record;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.DateUtils;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.base.widget.dialog.date.DateRangeDialog;
import acquire.base.widget.keyboard.listener.ViewKeyboardListener;
import acquire.core.constant.TransStatus;
import acquire.core.databinding.CoreFragmentEmptyRecordBinding;
import acquire.core.databinding.CoreFragmentRecordBinding;
import acquire.core.databinding.CoreRecordItemBinding;
import acquire.core.tools.TransUtils;
import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;


/**
 * A {@link Fragment} that displays the records
 *
 * @author Janson
 * @date 2019/7/25 11:20
 */
public class RecordFragment extends BaseFragment {
    private CoreFragmentRecordBinding binding;
    private FragmentCallback<Record> mCallback;
    private DateRangeDialog dateRangeDialog;
    private final RecordRepository recordRepository = new RecordRepository();
    private final static int PAGE_SIZE = 16;
    private int pageIndex = 0;
    private RecordRepository.RecordFilter filter = new RecordRepository.RecordFilter();

    private HashMap<Integer, Record> recordsHashMap = new HashMap<>();

    @NonNull
    public static RecordFragment newInstance(FragmentCallback<Record> callback) {
        RecordFragment fragment = new RecordFragment();
        fragment.mCallback = callback;
        return fragment;
    }

    /**
     * create a {@link RecordFragment}
     *
     * @param transTypes Used to filter records.Only records matching transTypes can be displayed
     * @param statuses   Used to filter records.Only records matching status can be displayed
     * @param callback   selection result callback
     * @return {@link RecordFragment}
     */
    @NonNull
    public static RecordFragment newInstance(String[] transTypes, int[] statuses, FragmentCallback<Record> callback) {
        RecordFragment fragment = new RecordFragment();
        fragment.mCallback = callback;
        fragment.filter.setTransTypes(transTypes);
        fragment.filter.setTransStatuses(statuses);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        List<Record> records = recordRepository.findByPageDesc(filter, 0, PAGE_SIZE);
        if (records.isEmpty()) {
            //no record
            CoreFragmentEmptyRecordBinding emptyRecordBinding = CoreFragmentEmptyRecordBinding.inflate(inflater, container, false);
            emptyRecordBinding.toolbar.setTitle(mActivity.getTitle());
            emptyRecordBinding.toolbar.setBackListener(v -> mActivity.getOnBackPressedDispatcher().onBackPressed());
            return emptyRecordBinding.getRoot();
        }
        binding = CoreFragmentRecordBinding.inflate(inflater, container, false);

        binding.toolbar.setTitle(mActivity.getTitle().toString().toUpperCase());
        binding.toolbar.setBackListener(v -> mActivity.getOnBackPressedDispatcher().onBackPressed());

        //DisplayUtils.fitsWindowStatus(binding.llToolbar);
        DisplayUtils.fitsWindowStatus(binding.toolbar);


        //for Brac
        bracInputIndex(records);

        binding.ivBack.setOnClickListener(v -> mActivity.getOnBackPressedDispatcher().onBackPressed());
        //adpater
        RecordAdapter adapter = new RecordAdapter(records);
        binding.rvRecords.setAdapter(adapter);

        //search a record
        binding.tvSearch.setOnClickListener(v ->
                mSupportDelegate.switchContent(SearchFragment.newInstance(new FragmentCallback<Record>() {
                    @Override
                    public void onSuccess(Record record) {
                        if (mCallback != null) {
                            mCallback.onSuccess(record);
                        }
                    }

                    @Override
                    public void onFail(int errorType, String errorMsg) {
                        mSupportDelegate.popBackFragment(1);
                    }
                }, filter.getTransStatuses(), filter.getTransTypes()))
        );

        //pre-load when RecyclerView scrolling.
        double countDouble = ((double) recordRepository.getCountByFilter(filter)) / PAGE_SIZE;
        int pageMax = (int) Math.ceil(countDouble);

        binding.rvRecords.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //scroll to next
                LinearLayoutManager llManager = (LinearLayoutManager) binding.rvRecords.getLayoutManager();
                if (llManager != null) {
                    int lastVisibleItem = llManager.findLastVisibleItemPosition();
                    int itemCount = llManager.getItemCount();
                    if (lastVisibleItem >= itemCount - 2 && dy > 0) {
                        //load next page
                        if (pageIndex < pageMax) {
                            new Handler(Looper.getMainLooper()).post(() -> loadNextPage(adapter));
                        }
                    }
                }
            }
        });

        //select the date rang
        binding.llDate.setOnClickListener(v -> {
            if (dateRangeDialog == null) {
                dateRangeDialog = new DateRangeDialog.Builder(mActivity)
                        .setConfirmListener((startDate, endDate) -> selectDateRecords(adapter, startDate, endDate))
                        .endToday()
                        .create();
            }
            dateRangeDialog.show();
        });
        return binding.getRoot();
    }

    private void bracInputIndex(List<Record> records) {

        binding.iclInIndx.btnOk.setEnabled(false);

        for (int i = 0; i < records.size(); i++) {
            int stan = -1;
            try {
                stan = Integer.parseInt(records.get(i).getTraceNo());
                recordsHashMap.put(stan, records.get(i));
            } catch (Exception ex) {

            }

        }

        //binding.iclInIndx.etInputInvoice
        binding.iclInIndx.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRecordToDetails();
            }
        });

        binding.iclInIndx.etInputInvoice.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

                String inputValue = binding.iclInIndx.etInputInvoice.getText().toString();
                if (inputValue == null || inputValue.isEmpty()) {
                    binding.iclInIndx.btnOk.setEnabled(false);
                    binding.iclInIndx.tilInvoice.setError("Please input valid Invoice no.");
                } else {
                    binding.iclInIndx.tilInvoice.setError(null);
                    binding.iclInIndx.btnOk.setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
        });

        binding.iclInIndx.keyboardNumber.setKeyBoardListener(new ViewKeyboardListener(mActivity, 13) {

            @Override
            public void onClear() {
                binding.iclInIndx.etInputInvoice.setText("");
                binding.iclInIndx.btnOk.setEnabled(false);
            }

            @Override
            public String getText() {
                String inputValue = binding.iclInIndx.etInputInvoice.getText().toString();
                if (inputValue == null || inputValue.isEmpty()) {
                    binding.iclInIndx.btnOk.setEnabled(false);
                } else
                    binding.iclInIndx.btnOk.setEnabled(true);

                return inputValue;
            }

            @Override
            public void setText(String text) {
                binding.iclInIndx.etInputInvoice.setText(text);
                LoggerUtils.d("input Invoice index: " + text);
            }

            @Override
            public void onEnter() {
                getRecordToDetails();
            }
        });


    }

    private void getRecordToDetails() {
        try {
            String inputInvoice = binding.iclInIndx.etInputInvoice.getText().toString();
            LoggerUtils.d("newCall Brac search record by inputed invoice: " + inputInvoice);
            //binding.iclInIndx.etInputInvoice.setError("");
            binding.iclInIndx.tilInvoice.setError(null);

            if (inputInvoice == null || inputInvoice.isEmpty()) {
                //TODO: Sho message as like old BAC app
                binding.iclInIndx.btnOk.setEnabled(false);
                //binding.iclInIndx.etInputInvoice.setError("Input valid Invoice no.");
                binding.iclInIndx.tilInvoice.setError("Please input valid Invoice no.");
                return;
            }

            binding.iclInIndx.btnOk.setEnabled(true);
            int invoiceOrStanNumber = Integer.parseInt(inputInvoice);
            Record getR = recordsHashMap.get(invoiceOrStanNumber);

            if (getR == null) {
                //binding.iclInIndx.etInputInvoice.setError("Invalid Invoice");
                binding.iclInIndx.tilInvoice.setError("Invoice not found");
            } else {
                binding.iclInIndx.tilInvoice.setError(null);
                mCallback.onSuccess(recordsHashMap.get(invoiceOrStanNumber));
            }

        } catch (Exception ex) {
            LoggerUtils.e("newCall Brac search record by inputed invoice: " + ex.toString());
        }

    }

    /**
     * load next page records
     *
     * @param adapter records view adapter
     */
    private void loadNextPage(RecordAdapter adapter) {
        //load next page
        pageIndex++;
        List<Record> nextRecords = recordRepository.findByPageDesc(filter, pageIndex, PAGE_SIZE);
        if (nextRecords != null) {
            List<Record> records = adapter.getRecords();
            if (records == null) {
                records = nextRecords;
            } else {
                records.addAll(nextRecords);
            }
            adapter.update(records);
            LoggerUtils.d("Loading next page -> " + pageIndex);
        }
    }

    /**
     * select the date ranage and update records
     *
     * @param adapter   records view adapter
     * @param startDate start date [yyyy,MM,dd]
     * @param endDate   end date [yyyy,MM,dd]
     */
    private void selectDateRecords(RecordAdapter adapter, int[] startDate, int[] endDate) {
        if (startDate == null && endDate == null) {
            return;
        }
        String startFormat = "";
        if (startDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(startDate[0], startDate[1] - 1, startDate[2]);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date start = calendar.getTime();
            startFormat = DateUtils.formatTime(start, DateUtils.YYYY_MM_DD);
            filter.setFrom(start);
        }
        String endFormat = "";
        if (endDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(endDate[0], endDate[1] - 1, endDate[2]);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date end = calendar.getTime();
            endFormat = DateUtils.formatTime(end, DateUtils.YYYY_MM_DD);
            filter.setTo(end);
        }

        pageIndex = 0;
        adapter.update(recordRepository.findByPageDesc(filter, pageIndex, PAGE_SIZE));
        binding.tvDate.setText(startFormat + " - " + endFormat);
    }

    @Override
    public FragmentCallback<Record> getCallback() {
        return mCallback;
    }

    /**
     * record list adapter
     *
     * @author Janson
     * @date 2021/11/25 11:36
     */
    private class RecordAdapter extends BaseBindingRecyclerAdapter<CoreRecordItemBinding> {
        private List<Record> records;

        public RecordAdapter(List<Record> records) {
            this.records = records;
        }

        public void update(List<Record> records) {
            this.records = records;
            notifyDataSetChanged();
        }

        public List<Record> getRecords() {
            return records;
        }

        @Override
        protected void bindItemData(CoreRecordItemBinding itemBinding, int position) {
            final Record record = records.get(position);
            if (record != null) {
                //Trans name
                String transName = TransUtils.getName(record.getTransType());
                itemBinding.tvTransType.setText(transName);

                //status
                if (record.getStatus() != TransStatus.SUCCESS) {
                    itemBinding.tvStatus.setVisibility(View.VISIBLE);
                    itemBinding.tvStatus.setText("[" + TransStatus.getDescription(record.getStatus()) + "]");
                } else {
                    itemBinding.tvStatus.setVisibility(View.GONE);
                }
                //Amount
                String amt = FormatUtils.formatAmount(record.getAmount(), 2, "");
                String identify = CurrencyUtils.getCurrencySymbol(record.getCurrencyCode());
                amt = identify + amt;
                itemBinding.tvAmount.setText(amt);
                //Time
                itemBinding.tvTime.setText(DateUtils.formatTime(record.getDate() + record.getTime()));
                //Trace
                itemBinding.tvTraceNo.setText(record.getTraceNo());
            }
            //click
            itemBinding.getRoot().setOnClickListener(v -> mCallback.onSuccess(record));
        }


        @Override
        public int getItemCount() {
            if (records == null) {
                return 0;
            }
            return records.size();
        }
    }
}
