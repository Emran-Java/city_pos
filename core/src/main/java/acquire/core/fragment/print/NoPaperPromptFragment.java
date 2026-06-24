package acquire.core.fragment.print;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import acquire.base.activity.BaseDialogFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentPrintNoPaperBinding;

/**
 * No receipt paper prompt
 *
 * @author Janson
 * @date 2022/10/19 9:55
 */
public class NoPaperPromptFragment extends BaseDialogFragment {
    private FragmentCallback<Void> callback;
    public static NoPaperPromptFragment newInstance(FragmentCallback<Void> callback) {
        NoPaperPromptFragment fragment = new NoPaperPromptFragment();
        fragment.callback = callback;
        return fragment;
    }

    @Override
    public View onCreateDialogView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CoreFragmentPrintNoPaperBinding binding = CoreFragmentPrintNoPaperBinding.inflate(inflater, container, false);
        binding.btnCancel.setOnClickListener(v-> callback.onFail(FragmentCallback.CANCEL,getString(R.string.core_print_error_prompt)));
        binding.btnReprint.setOnClickListener(v-> callback.onSuccess(null));
        return binding.getRoot();
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
    public FragmentCallback<Void> getCallback() {
        return callback;
    }


}
