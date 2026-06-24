package acquire.core.fragment.print;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseDialogFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.R;
import acquire.core.bean.SettleReceiptBean;
import acquire.core.databinding.CoreFragmentPrintBinding;
import acquire.core.model.CardSchemeReportModel;
import acquire.core.model.DeviceItem;
import acquire.core.report_data_factiry.ReportDataFactory;
import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;

/**
 * Print receipt
 *
 * @author Janson
 * @date 2022/5/30 20:41
 */
public class PrintFragment extends BaseDialogFragment {

    //for About infor print
    private List<DeviceItem> mAboutInfpItems = new ArrayList<>();

    //for brac settle
    private List<Record> recordsData = new ArrayList<Record>();
    private List<CardSchemeReportModel> schemeList;
    //-----------------


    private CoreFragmentPrintBinding binding;

    private FragmentCallback<Void> callback;

    private Record record;
    private SettleReceiptBean settleReceiptBean;
    private boolean isReprint;

    private boolean stopScroll;

    private PrintViewModel printViewModel;
    private final static int TYPE_RECEIPT = 0, TYPE_SETTLEMET = 1, TYPE_DETAIL = 2,  TYPE_ABOUT_INFO = 3;
    private int type = TYPE_RECEIPT;
    private int receiptOwner;
    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (stopScroll) {
                return;
            }
            binding.scrollView.scrollBy(0, 5);
            int viewHeight = binding.llImages.getMeasuredHeight();
            if (viewHeight != 0 && viewHeight == binding.scrollView.getScrollY()) {
                // scroll over
                return;
            }
            ThreadPool.postDelayOnMain(this, 15);
        }
    };

    public static PrintFragment newPrintAboutInfoInstance(List<DeviceItem> items, boolean isReprint, int receiptOwner, FragmentCallback<Void> callback) {
        PrintFragment fragment = new PrintFragment();
        fragment.type = TYPE_ABOUT_INFO;
        fragment.receiptOwner = receiptOwner;
//        fragment.record = record;
        fragment.mAboutInfpItems = items;
        fragment.callback = callback;
        return fragment;
    }

    public static PrintFragment newReceiptInstance(Record record, boolean isReprint, int receiptOwner, FragmentCallback<Void> callback) {
        PrintFragment fragment = new PrintFragment();
        fragment.type = TYPE_RECEIPT;
        fragment.receiptOwner = receiptOwner;
        fragment.record = record;
        fragment.isReprint = isReprint;
        fragment.callback = callback;
        return fragment;
    }


    public static PrintFragment newReceiptInstance(Record record, boolean isReprint, FragmentCallback<Void> callback) {
        PrintFragment fragment = new PrintFragment();
        fragment.type = TYPE_RECEIPT;
        fragment.record = record;
        fragment.isReprint = isReprint;
        fragment.callback = callback;
        return fragment;
    }

    public static PrintFragment newDetailInstance(FragmentCallback<Void> callback) {
        PrintFragment fragment = new PrintFragment();
        fragment.type = TYPE_DETAIL;
        fragment.callback = callback;
        return fragment;
    }

    public static PrintFragment newSettlementInstance(SettleReceiptBean settleReceiptBean, FragmentCallback<Void> callback) {
        PrintFragment fragment = new PrintFragment();
        fragment.type = TYPE_SETTLEMET;
        fragment.settleReceiptBean = settleReceiptBean;
        fragment.callback = callback;
        return fragment;
    }

    @Override
    public View onCreateDialogView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        printViewModel = new ViewModelProvider(this).get(PrintViewModel.class);
        binding = CoreFragmentPrintBinding.inflate(inflater, container, false);
        //prompt message
        printViewModel.getPrompt().observe(getViewLifecycleOwner(), prompt -> binding.tvPrinting.setText(prompt));
        //receipt bitmap
        printViewModel.getReceipt().observe(getViewLifecycleOwner(), bitmap -> {
            if (binding.llImages.getChildCount() == 0) {
                scrollRunnable.run();
            }
            ImageView imageView = new ImageView(mActivity);
            imageView.setImageBitmap(bitmap);
            binding.llImages.addView(imageView);
        });

        //gte records for settle
        RecordRepository repoObj = new RecordRepository();
        recordsData = repoObj.findAllReport();
        ReportDataFactory reportDataFactory = ReportDataFactory.getInstance();
        schemeList = reportDataFactory.moveGrandTotalToBottom(reportDataFactory.generateSchemeReport(recordsData));

        //printing status
        printViewModel.getStatus().observe(getViewLifecycleOwner(), prtStatus -> {
            int status = prtStatus.getStatus();
            switch (status) {
                case PrintViewModel.STATUS_READY:
                    stopScroll = false;
                    binding.llImages.removeAllViews();
                    binding.scrollView.scrollTo(0, 0);
                    break;
                case PrintViewModel.STATUS_OUT_OF_PAPER:
                    stopScroll = true;
                    if (Model.X800.equals(BDevice.getDeviceModel())) {
                        mSupportDelegate.switchContent(NoPaperPromptFragment.newInstance(new FragmentCallback<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                mSupportDelegate.popBackFragment(1);
                                print();
                            }

                            @Override
                            public void onFail(int errorType, String errorMsg) {
                                callback.onFail(FragmentCallback.FAIL, errorMsg);
                            }
                        }));
                    } else {
                        mActivity.runOnUiThread(() ->
                                new MessageDialog.Builder(mActivity)
                                        .setMessage(R.string.core_print_load_paper)
                                        //continue to print
                                        .setConfirmButton(R.string.core_print_dialog_button_reprint, dialog -> print())
                                        //cancel
                                        .setCancelButton(dialog -> callback.onFail(FragmentCallback.FAIL, getString(R.string.core_print_load_paper)))
                                        .setCancelTimeout(30 * 1000)
                                        .show()
                        );
                    }
                    break;
                case PrintViewModel.STATUS_NEXT_RECEIPT:
                    if (scrollNotComplete()) {
                        ThreadPool.postDelayOnMain(() -> printViewModel.getStatus().postValue(prtStatus), 1000);
                        return;
                    }
                    String message = prtStatus.getMessage();
                    if (message != null) {
                        new MessageDialog.Builder(mActivity)
                                .setMessage(message)
                                .setConfirmButton(dialog -> print())
                                .setCancelButton(dialog -> callback.onSuccess(null))
                                .setConfirmTimeout(5 * 1000)
                                .show();
                    } else {
                        print();
                    }
                    break;
                case PrintViewModel.STATUS_ERROR:
                    stopScroll = true;
                    String msg = prtStatus.getMessage();
                    new MessageDialog.Builder(mActivity)
                            .setMessage(msg)
                            .setConfirmButton(dialog -> callback.onFail(FragmentCallback.FAIL, msg))
                            .setConfirmTimeout(30 * 1000)
                            .show();
                    break;
                case PrintViewModel.STATUS_SUCCESS:
                    if (scrollNotComplete()) {
                        ThreadPool.postDelayOnMain(() -> printViewModel.getStatus().postValue(prtStatus), 700);
                        return;
                    }
                    callback.onSuccess(null);
                    break;
                default:
                    break;
            }
        });
        //init
        printViewModel.init();
        //start to print
        print();
        return binding.getRoot();
    }

    private boolean scrollNotComplete() {
        int viewHeight = binding.llImages.getMeasuredHeight();
        float scrollY = binding.scrollView.getScrollY();
        return (scrollY / viewHeight) < 0.3;
    }

    private void print() {
        switch (type) {
            case TYPE_RECEIPT:
                printViewModel.printReceipt(record, isReprint, receiptOwner);
                break;
            case TYPE_SETTLEMET:
                //printViewModel.printSettlement(settleReceiptBean);
                printViewModel.printBracSettlement(settleReceiptBean, recordsData);
                break;
            case TYPE_ABOUT_INFO:{
                printViewModel.printBracAboutInfo(mAboutInfpItems);
                break;
            }
            case TYPE_DETAIL:
            default:
                printViewModel.printDetail();
                break;
        }
    }

    @Override
    public int[] getPopAnimation() {
        return null;
    }

    @Override
    public boolean onBack() {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScroll = true;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return callback;
    }


}
