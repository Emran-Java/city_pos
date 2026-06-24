package acquire.core.fragment.result;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.BoolRes;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.base.widget.keyboard.listener.ViewKeyboardListener;
import acquire.core.R;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ScreenHeightDps;
import acquire.core.databinding.CoreFragmentResultBinding;
import acquire.core.databinding.CoreReceiptTypeItemBinding;
import acquire.core.fragment.receipt.ReceiptProvider;
import acquire.core.fragment.receipt.ReceiptTypeItem;
import acquire.core.tools.PhysicalKeyboardUtils;
import acquire.core.tools.SoundPlayer;
import acquire.core.tools.TransUtils;
import acquire.database.model.Record;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;

/**
 * A {@link Fragment} that displays the simple result information & choose receipt
 */
public class ResultFragment extends BaseFragment {

    private boolean mIsReprint;
    private boolean mIsSuccess;
    private String mMessage;
    /**
     * The record to be printed
     */
    private Record mRecord;
    /**
     * Result callback
     */
    private FragmentCallback<Void> mCallback;


    private CoreFragmentResultBinding binding;

    private CountDownTimer timer ;
    private long timeLeftInMillis = 5 * 1000;


    @NonNull
    public static ResultFragment newInstanceAboutInfo(boolean isReprint, boolean isSuccess, String message, FragmentCallback<Void> callback) {
        ResultFragment fragment = new ResultFragment();
        fragment.mCallback = callback;
        fragment.mIsReprint = isReprint;
        fragment.mIsSuccess = isSuccess;
        fragment.mMessage = message;
//        fragment.mRecord = record;
        return fragment;
    }

    @NonNull
    public static ResultFragment newInstance(boolean isReprint, boolean isSuccess, String message, Record record, FragmentCallback<Void> callback) {
        ResultFragment fragment = new ResultFragment();
        fragment.mCallback = callback;
        fragment.mIsReprint = isReprint;
        fragment.mIsSuccess = isSuccess;
        fragment.mMessage = message;
        fragment.mRecord = record;
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentResultBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle());
        //done button
        binding.btnDone.setOnClickListener(v -> {
                if(timer!=null){
                    timer.cancel();
                }
                if (mCallback != null) {
                    mCallback.onSuccess(null);
                    mCallback = null;

                }
        });

        //physical enter
        if (BDevice.supportPhysicalKeyboard()) {
            DisplayMetrics metrics = DisplayUtils.getDisplayMetrics(mActivity);
            float heightDps = metrics.heightPixels / metrics.density;
            PhysicalKeyboardUtils.setKeyboardListener(binding.tvMessage, new ViewKeyboardListener(mActivity) {
                @Override
                public String getText() {
                    return "";
                }

                @Override
                public void setText(String text) {
                    switch (text){
                        case "1":
                        case "2":
                        case "3":
                        case "4":
                        case "5":
                            if(heightDps<=ScreenHeightDps.HEIGHT_285_DPS){
                                if(binding.llReceipt.getVisibility() == View.VISIBLE){
                                    RecyclerView.ViewHolder holder = binding.rvReceiptType.findViewHolderForAdapterPosition(Integer.parseInt(text)-1);
                                    if(holder!=null){
                                        holder.itemView.performClick();
                                    }
                                }
                            }
                            break;
                        default:
                            break;

                    }

                }

                @Override
                public void onEnter() {
                    binding.btnDone.callOnClick();
                }
            });
        }

        String title = mActivity.getTitle().toString();

        boolean isSimpleResult = !mIsSuccess || mRecord == null ;
        //show result
        if (isSimpleResult){
            binding.tvMessage.setText(mMessage);
            if (!mIsSuccess) {
                binding.ivResult.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.core_result_fail));
                binding.tvMessage.setTextColor(ContextCompat.getColor(mActivity, R.color.base_warning));
            }
            else{
                //PLAY audio
                if(title.toLowerCase().contains("sale")) {
                    SoundPlayer.getInstance().playSuccess(getContext());
                }
            }
        }else {
            if(title.toLowerCase().contains("sale")) {
                SoundPlayer.getInstance().playSuccess(getContext());
            }

            if (mRecord.getAmount() != 0) {
                String amountPrompt = CurrencyUtils.getCurrencySymbol(mRecord.getCurrencyCode()) + FormatUtils.formatAmount(mRecord.getAmount());
                String message = TransUtils.getName(mRecord.getTransType()) + " " + amountPrompt;
                binding.tvMessage.setText(message);
            }
        }

        if (isSimpleResult){
            //No receipt
            startTimer();
        }else if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_RECEIPT_AUTO_PRINT)){
            //auto print
            onMultiReceiptsDisabled();
        }else {
            //multi-receipt supported
            onMultiReceiptsEnabled();
        }

        return binding.getRoot();
    }

    public void onMultiReceiptsEnabled(){
        List<ReceiptTypeItem> list = ReceiptTypeItem.getReceiptTypeList();
        if (!list.isEmpty()){
            binding.llReceipt.setVisibility(View.VISIBLE);
            binding.llReceipt.setVisibility(View.VISIBLE);
            binding.rvReceiptType.setAdapter(new ReceiptTypeAdapter(list, item -> {
               if(timer!=null){
                   timer.cancel();
               }
                binding.btnDone.setText(R.string.base_done);
                ReceiptProvider.provideReceipt(mActivity, mRecord,mIsReprint, item.getReceiptType(), () -> {
                });
            }));
        }
        startTimer();
    }

    public void onMultiReceiptsDisabled(){
        binding.llReceipt.setVisibility(View.GONE);
        binding.btnDone.setEnabled(false);
        ThreadPool.postDelayOnMain(() -> {
            if (BDevice.supportPrint() || ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
                ReceiptProvider.provideReceipt(mActivity, mRecord, mIsReprint,ReceiptTypeItem.MERCHANT_PAPER, this::autoReceiptFinish);
            }else {
                autoReceiptFinish();
            }
        },500);
    }

    private void autoReceiptFinish() {
        binding.btnDone.setEnabled(true);
        if (mIsReprint){
            if (mCallback != null) {
                mCallback.onSuccess(null);
                mCallback = null;
            }
        }else {
            timeLeftInMillis = 5 * 1000;
            startTimer();
        }
    }

    private void startTimer() {
        if (mIsReprint) {
            return;
        }
        ThreadPool.postOnMain(() -> timer = new CountDownTimer(timeLeftInMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int countDown = (int) Math.ceil(millisUntilFinished / 1000f);
                String content = String.format(Locale.getDefault(), "%s(%d)",
                        mActivity.getString(R.string.base_done), countDown);
                binding.btnDone.setText(content);
                timeLeftInMillis = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                if (mCallback != null) {
                    mCallback.onSuccess(null);
                    mCallback = null;
                }
            }
        }.start());
    }


    @Override
    public FragmentCallback<Void> getCallback() {
        return mCallback;
    }

    @Override
    public void onDestroy() {
        if (BDevice.supportPhysicalKeyboard()) {
            PhysicalKeyboardUtils.removeKeyboardListener(binding.tvMessage);
        }
        super.onDestroy();
    }


    @Keep
    static class ReceiptTypeAdapter extends BaseBindingRecyclerAdapter<CoreReceiptTypeItemBinding> {
        private final List<ReceiptTypeItem> items;
        private final OnClickReceiptListener listener;

        public ReceiptTypeAdapter(List<ReceiptTypeItem> items, OnClickReceiptListener listener) {
            this.items = items;
            this.listener = listener;
        }


        @Override
        protected void bindItemData(CoreReceiptTypeItemBinding itemBinding, int position) {
            ReceiptTypeItem item = items.get(position);
            DisplayMetrics metrics = itemBinding.getRoot().getContext().getResources().getDisplayMetrics();
            float heightDps = metrics.heightPixels / metrics.density;
            if(BDevice.supportPhysicalKeyboard() && heightDps<= ScreenHeightDps.HEIGHT_285_DPS){
                String sub = String.format("%s.", position + 1) ;
                itemBinding.tvName.setText(String.format("%s%s", sub, item.getName()));
            }else {
                //name
                itemBinding.tvName.setText(item.getName());
            }
            //icon
            itemBinding.ivIcon.setImageResource(item.getIcon());

            itemBinding.getRoot().setOnClickListener(view -> listener.onSelect(item));
        }


        @Override
        public int getItemCount() {
            return items.size();
        }
    }


    public interface OnClickReceiptListener {
        void onSelect(ReceiptTypeItem item);
    }
}
