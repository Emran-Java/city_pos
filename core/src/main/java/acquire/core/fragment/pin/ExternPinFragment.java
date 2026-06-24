package acquire.core.fragment.pin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentExternPinBinding;


/**
 * A external PIN pad {@link Fragment}
 *
 * @author Janson
 * @date 2021/1/5 17:18
 */
public class ExternPinFragment extends BaseFragment {
    /**
     * Result callback.
     */
    private FragmentCallback<byte[]> mCallback;

    private PinViewModel pinViewModel;
    private final static String ARG_PARAMS = "PARAMS";
    @NonNull
    public static ExternPinFragment newInstance(PinFragmentArgs pinFragmentArgs, FragmentCallback<byte[]> callback) {
        ExternPinFragment fragment = new ExternPinFragment();
        fragment.mCallback = callback;
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAMS,pinFragmentArgs);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CoreFragmentExternPinBinding binding = CoreFragmentExternPinBinding.inflate(inflater,container,false);
        binding.toolbar.setTitle(mActivity.getTitle());
        pinViewModel = new ViewModelProvider(this).get(PinViewModel.class);

        if (getArguments()!= null){
            PinFragmentArgs pinFragmentArgs = (PinFragmentArgs) getArguments().getSerializable(ARG_PARAMS);
            if (pinFragmentArgs != null){
                pinViewModel.init(pinFragmentArgs);
                //prompts
                if (pinFragmentArgs.isOnlinePin()){
                    binding.tvMsg.setText(R.string.core_pin_input_extern_password);
                }else{
                    binding.tvMsg.setText(R.string.core_pin_input_extern_offline_password);
                }
                //amount
                if (pinFragmentArgs.getAmount() != 0L) {
                    String amt = CurrencyUtils.getCurrencySymbol(pinFragmentArgs.getCurrencyCode())
                            + FormatUtils.formatAmount(pinFragmentArgs.getAmount());
                    binding.tvAmount.setText(amt);
                }
                //card number
                binding.tvPan.setText(FormatUtils.maskCardNo(pinFragmentArgs.getPan()));
                //card scheme
                binding.tvCardScheme.setText(pinFragmentArgs.getCardScheme());
                //start pinpad
                pinViewModel.startPin();
            }
        }else{
            mCallback.onFail(FragmentCallback.FAIL,getString(R.string.core_pin_args_error));
        }
        pinViewModel.getError().observe(getViewLifecycleOwner(), pinError -> {
            if (pinError.getErrorCode() == PinViewModel.PinError.CANCEL){
                mCallback.onFail(FragmentCallback.CANCEL, pinError.getDescription());
            }else {
                mCallback.onFail(FragmentCallback.FAIL, pinError.getDescription());
            }
        });
        pinViewModel.getPinBlock().observe(getViewLifecycleOwner(),pinBlock->{
            mCallback.onSuccess(pinBlock);
        });
         return binding.getRoot();
    }


    @Override
    public boolean onBack() {
        pinViewModel.cancelPin();
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        pinViewModel.cancelPin();
    }

    @Override
    public FragmentCallback<byte[]> getCallback() {
        return mCallback;
    }

}
