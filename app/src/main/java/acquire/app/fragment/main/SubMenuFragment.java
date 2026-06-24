package acquire.app.fragment.main;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;
import java.util.Objects;

import com.zztl.pos.city.databinding.AppFragmentSubMenuBinding;
import acquire.app.fragment.main.menu.MainMenuAdapter;
import acquire.app.fragment.main.menu.MenuItem;
import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ViewUtils;
import acquire.core.constant.ScreenHeightDps;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;

/**
 * A sub menu fragment.
 *
 * @author Janson
 * @date 2021/8/6 14:53
 */
public class SubMenuFragment extends BaseFragment {
    private AppFragmentSubMenuBinding binding;
    private List<MenuItem> menuItems;
    private String title;
    private float heightDps;

    @NonNull
    public static SubMenuFragment newInstance(String title, List<MenuItem> menuItems) {
        SubMenuFragment fragment = new SubMenuFragment();
        fragment.menuItems = menuItems;
        fragment.title = title;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AppFragmentSubMenuBinding.inflate(inflater, container, false);
        if (menuItems != null){
            binding.rvMenu.setAdapter(new MainMenuAdapter(mActivity, menuItems));
        }
        DisplayMetrics metrics = DisplayUtils.getDisplayMetrics(mActivity);
        heightDps = metrics.heightPixels / metrics.density;
        binding.toolbar.setTitle(title);
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        return binding.getRoot();
    }

    @Override
    public void onFragmentShow() {
        super.onFragmentShow();
      // if(BDevice.supportPhysicalKeyboard() && heightDps<=ScreenHeightDps.HEIGHT_285_DPS){
            ViewUtils.setFocus(binding.getRoot());
            binding.getRoot().setOnKeyListener(new View.OnKeyListener() {
                private int handleKey;
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        handleKey = keyCode;
                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        if (handleKey == keyCode) {
                            if (keyCode >= KeyEvent.KEYCODE_1 && keyCode <= KeyEvent.KEYCODE_9) {
                                RecyclerView.ViewHolder holder = binding.rvMenu.findViewHolderForAdapterPosition(keyCode -KeyEvent.KEYCODE_0-1);
                                if (holder != null) {
                                    holder.itemView.performClick();
                                }
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });

       /* }else{
            LoggerUtils.d("NewCall->SubMenuFragment");
        }*/
    }

    @Override
    public void onFragmentHide() {
        super.onFragmentHide();
        if(BDevice.supportPhysicalKeyboard() && heightDps<=ScreenHeightDps.HEIGHT_285_DPS){
            binding.getRoot().setOnKeyListener(null);
        }
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }
}
