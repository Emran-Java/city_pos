package acquire.core.fragment.pin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentAccessPinBinding;
import acquire.sdk.device.BDevice;


/**
 * A built-in RNIB accessibility PIN pad {@link Fragment}
 *
 * @author Janson
 * @date 2025/2/20 13:57
 */
public class AccessPinFragment extends BaseFragment {
    /**
     * Result callback.
     */
    private FragmentCallback<byte[]> mCallback;
    private CoreFragmentAccessPinBinding binding;
    private PinViewModel pinViewModel;
    private final static String ARG_PARAMS = "PARAMS";

    private PinFragmentArgs pinFragmentArgs;

    @NonNull
    public static AccessPinFragment newInstance(PinFragmentArgs pinFragmentArgs, FragmentCallback<byte[]> callback) {
        AccessPinFragment fragment = new AccessPinFragment();
        fragment.mCallback = callback;
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAMS,pinFragmentArgs);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentAccessPinBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle());
        pinViewModel = new ViewModelProvider(this).get(PinViewModel.class);
        if (getArguments()!= null){
            pinFragmentArgs = (PinFragmentArgs) getArguments().getSerializable(ARG_PARAMS);
            //prompts
            if (pinFragmentArgs != null){
                if (pinFragmentArgs.isOnlinePin()) {
                    binding.tvMsg.setText(R.string.core_pin_input_password);
                } else {
                    binding.tvMsg.setText(R.string.core_pin_input_offline_password);
                }
                //amount
                if (pinFragmentArgs.getAmount() != 0L) {
                    String amt = CurrencyUtils.getCurrencySymbol(pinFragmentArgs.getCurrencyCode())
                            + FormatUtils.formatAmount(pinFragmentArgs.getAmount());
                    binding.tvAmount.setText(amt);
                }
                //card number
                binding.tvPan.setText(FormatUtils.maskCardNo(pinFragmentArgs.getPan()));
                pinViewModel.init(pinFragmentArgs);
            }
        }
        pinViewModel.getError().observe(getViewLifecycleOwner(), pinError -> {
            if (pinError.getErrorCode() == PinViewModel.PinError.CANCEL){
                mCallback.onFail(FragmentCallback.CANCEL, pinError.getDescription());
            }else {
                mCallback.onFail(FragmentCallback.FAIL, pinError.getDescription());
            }
        });
        pinViewModel.getInputLength().observe(getViewLifecycleOwner(), inputLength -> {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < inputLength; i++) {
                builder.append(" * ");
            }
            binding.tvPin.setText(builder.toString());
        });
        pinViewModel.getPinBlock().observe(getViewLifecycleOwner(),pinBlock-> mCallback.onSuccess(pinBlock));
        return binding.getRoot();
    }


    private boolean firstAddPreDraw = true;

    @Override
    public void onResume() {
        super.onResume();
        if (firstAddPreDraw) {
            firstAddPreDraw = false;
            if (pinFragmentArgs == null){
                mCallback.onFail(FragmentCallback.FAIL,getString(R.string.core_pin_args_error));
                return;
            }
            if (BDevice.supportPhysicalKeyboard()){
                binding.glPinLayout.setVisibility(View.GONE);
                pinViewModel.startPin();
                return;
            }
            binding.key0.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    binding.key0.getViewTreeObserver().removeOnPreDrawListener(this);
                    List<View> numberKeyViews = new ArrayList<>();
                    List<View> funcKeyViews = new ArrayList<>();
                    numberKeyViews.add(binding.key0);
                    numberKeyViews.add(binding.key1);
                    numberKeyViews.add(binding.key2);
                    numberKeyViews.add(binding.key3);
                    numberKeyViews.add(binding.key4);
                    numberKeyViews.add(binding.key5);
                    numberKeyViews.add(binding.key6);
                    numberKeyViews.add(binding.key7);
                    numberKeyViews.add(binding.key8);
                    numberKeyViews.add(binding.key9);
                    funcKeyViews.add(binding.keyBack);
                    funcKeyViews.add(binding.keyCancel);
                    funcKeyViews.add(binding.keyEnter);
                    pinViewModel.setRandomKeyboard(numberKeyViews,funcKeyViews);
                    pinViewModel.startPin();
                    return true;
                }
            });
        }
    }

    @Override
    public void onStop() {
        pinViewModel.cancelPin();
        super.onStop();
    }



    @Override
    public boolean onBack() {
        return true;
    }

    @Override
    public FragmentCallback<byte[]> getCallback() {
        return mCallback;
    }

}
