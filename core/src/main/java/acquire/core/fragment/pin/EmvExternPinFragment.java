package acquire.core.fragment.pin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentExternPinBinding;


/**
 * A external PIN pad {@link Fragment} of EMV process
 *
 * @author Janson
 * @date 2022/3/23 10:22
 */
public class EmvExternPinFragment extends BaseFragment {

    private final static String ARG_PARAMS = "PARAMS";

    @NonNull
    public static EmvExternPinFragment newInstance(PinFragmentArgs pinFragmentArgs) {
        EmvExternPinFragment fragment = new EmvExternPinFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAMS,pinFragmentArgs);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CoreFragmentExternPinBinding binding = CoreFragmentExternPinBinding.inflate(inflater,container,false);
        if (getArguments()!= null){
            PinFragmentArgs pinFragmentArgs = (PinFragmentArgs) getArguments().getSerializable(ARG_PARAMS);
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
        }
        return binding.getRoot();
    }

    @Override
    public boolean onBack() {
        return true;
    }

    @Override
    public FragmentCallback<byte[]> getCallback() {
        return null;
    }

}
