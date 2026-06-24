package acquire.app.fragment.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.Objects;

import com.zztl.pos.ucb.databinding.AppFragmentSaverBinding;
import acquire.base.ActivityStackManager;
import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ViewUtils;
import acquire.core.TransActivity;
import acquire.core.constant.TransTag;
import acquire.core.constant.TransType;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;

public class SaverFragment extends BaseFragment {

    private AppFragmentSaverBinding binding;
    @Override
    public FragmentCallback getCallback() {
        return null;
    }

    @Override
    public int[] getPopAnimation() {
        return null;
    }

    @NonNull
    public static SaverFragment newInstance() {
        return new SaverFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AppFragmentSaverBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.getRoot().setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                mActivity.getOnBackPressedDispatcher().onBackPressed();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onFragmentShow() {
        super.onFragmentShow();

        if(BDevice.supportPhysicalKeyboard()){
            ViewUtils.setFocus(binding.getRoot());
            binding.getRoot().setOnKeyListener(new View.OnKeyListener() {
                private int handleKey;

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        handleKey = keyCode;
                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        if (handleKey == keyCode) {
                            if (keyCode == KeyEvent.KEYCODE_1) {
                                if (ActivityStackManager.getTopActivity() instanceof TransActivity) {
                                    return true;
                                }
                                //start transaction
                                Intent intent = new Intent(mActivity, TransActivity.class);
                                intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_SALE);
                                ActivityCompat.startActivity(mActivity, intent, null);
                                return true;
                            }else {
                                mActivity.getOnBackPressedDispatcher().onBackPressed();
                                return true;
                            }

                        }
                    }
                    return false;
                }
            });


        }
    }

    @Override
    public void onFragmentHide() {
        super.onFragmentHide();
        binding.getRoot().setOnKeyListener(null);
    }
}
