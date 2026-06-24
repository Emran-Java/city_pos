package acquire.app.fragment.main;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.zztl.pos.ucb.R;
import com.zztl.pos.ucb.databinding.AppFragmentMainBinding;
import com.zztl.pos.ucb.databinding.AppPageDotBinding;
import acquire.app.fragment.main.menu.MainMenu;
import acquire.app.fragment.main.menu.MainMenuAdapter;
import acquire.app.fragment.main.menu.MenuItem;
import acquire.base.ActivityStackManager;
import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.TransActivity;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ScreenHeightDps;
import acquire.core.constant.TransTag;
import acquire.sdk.device.BDevice;

/**
 * A main {@link Fragment}
 *
 * @author Janson
 * @date 2019/1/28 10:22
 */
public class MainFragment extends BaseFragment {
    /**
     * menu page row
     */
    private static int PAGE_ROW = 4;

    private final MainUpdateReceiver mainUpdateReceiver = new MainUpdateReceiver();
    private AppFragmentMainBinding binding;

    @NonNull
    public static MainFragment newInstance() {
        return new MainFragment();
    }
    private boolean isScreensaverShowing = false;
    private Handler saverHandler ;
    private final Runnable saverRunnable = () -> {
        if (!isScreensaverShowing) {
            showScreensaver();
        }
    };

    private float heightDps;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = AppFragmentMainBinding.inflate(inflater, container, false);
        DisplayMetrics metrics = DisplayUtils.getDisplayMetrics(mActivity);
        heightDps = metrics.heightPixels / metrics.density;
        if (heightDps >= ScreenHeightDps.HEIGHT_750_DPS) {
            PAGE_ROW = 5;
        }else {
            if(heightDps<= ScreenHeightDps.HEIGHT_456_DPS){
                //N560K,S30
                PAGE_ROW =2;
            }else if (heightDps<=ScreenHeightDps.HEIGHT_530_DPS){
                PAGE_ROW = 3;
            }
        }
        initView();
        IntentFilter filter = new IntentFilter();
        filter.addAction(mActivity.getPackageName());
        mActivity.registerReceiver(mainUpdateReceiver, filter);
        saverHandler = new Handler();
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(BDevice.supportPhysicalKeyboard() && heightDps<=ScreenHeightDps.HEIGHT_285_DPS){
            binding.getRoot().setOnTouchListener((v, event) -> {
                resetSaver();
                return false;
            });
        }
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }

    @Override
    public int[] getPopAnimation() {
        return new int[]{R.anim.slide_left_in, 0};
    }

    private void initView() {
        List<MenuItem> items = MainMenu.getInstance().getMenu();
        List<MenuItem> functionItems;
        //top item
        if (heightDps<=ScreenHeightDps.HEIGHT_285_DPS){
            functionItems = items;
        }else {
            MenuItem topItem = items.get(0);
            functionItems = items.subList(1, items.size());
            if (binding.ivTopIcon != null) {
                binding.ivTopIcon.setImageResource(topItem.getIcon());
            }
            if (binding.tvTopName != null) {
                binding.tvTopName.setText(topItem.getName());
            }
            if (binding.cvTopContainer != null) {
                if (topItem.getColorId() != 0) {
                    binding.cvTopContainer.setCardBackgroundColor(ContextCompat.getColor(mActivity, topItem.getColorId()));
                }
                binding.cvTopContainer.setOnClickListener(v -> {
                    if (ActivityStackManager.getTopActivity() instanceof TransActivity) {
                        return;
                    }
                    //start transaction
                    Intent intent = new Intent(mActivity, TransActivity.class);
                    intent.putExtra(TransTag.TRANS_TYPE, topItem.getTransType());
                    ActivityCompat.startActivity(mActivity, intent, null);
                });
            }

        }
        //menu page
        List<MenuFragment> childFragments = createMenuFragments(functionItems);
        binding.viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return childFragments.get(position);
            }

            @Override
            public int getItemCount() {
                return childFragments.size();
            }
        });
        //bottom dot
        if (childFragments.size() < 2) {
            binding.rvDot.setVisibility(View.GONE);
        } else {
            binding.rvDot.setVisibility(View.VISIBLE);
            DotAdapter dotAdapter = new DotAdapter(childFragments.size());
            binding.rvDot.setAdapter(dotAdapter);
            binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    dotAdapter.setSelected(position);
                }
            });
        }
    }

    @NonNull
    private List<MenuFragment> createMenuFragments(@NonNull List<MenuItem> items) {
        List<MenuFragment> fragments = new ArrayList<>();
        int pageSize = PAGE_ROW * getResources().getInteger(R.integer.app_main_menu_columns);
        for (int i = 0; i < items.size(); i += pageSize) {
            List<MenuItem> fragItems = new ArrayList<>();
            int remainSize = items.size() - i;
            if (remainSize >= pageSize) {
                fragItems.addAll(items.subList(i, i + pageSize));
            } else {
                //fill placeholder item
                fragItems.addAll(items.subList(i, items.size()));
                int fillSize = pageSize - remainSize;
                for (int j = 0; j < fillSize; j++) {
                    fragItems.add(MainMenuAdapter.FILL_PLACE_ITEM);
                }
            }
            if(BDevice.supportPhysicalKeyboard() && heightDps<=ScreenHeightDps.HEIGHT_285_DPS){
                fragments.add(MenuFragment.newInstance(fragItems, this::resetSaver));
            }else {
                fragments.add(MenuFragment.newInstance(fragItems));
            }

        }
        return fragments;
    }

    @Override
    public boolean onBack() {
        //Confirm to exit this application
        new MessageDialog.Builder(mActivity)
                .setMessage(R.string.app_exit_prompt)
                .setConfirmButton(dialog -> mActivity.finish())
                .setCancelButton(dialog -> {})
                .show();
        return true;
    }

    @Override
    public void onFragmentShow() {
        super.onFragmentShow();
        registerN560Key();
    }

    @Override
    public void onResume() {
        super.onResume();
        List<Fragment> listFragment  = mActivity.getSupportFragmentManager().getFragments();
        if(!listFragment.isEmpty()){
            Fragment topFragment = listFragment.get(listFragment.size()-1);
            if(topFragment instanceof MainFragment){
                registerN560Key();
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if(BDevice.supportPhysicalKeyboard() && heightDps<=ScreenHeightDps.HEIGHT_285_DPS){
            binding.getRoot().setOnKeyListener(null);
            isScreensaverShowing =false;
            saverHandler.removeCallbacks(saverRunnable);
        }
    }


    @Override
    public void onFragmentHide() {
        super.onFragmentHide();
         if(BDevice.supportPhysicalKeyboard() && heightDps<=ScreenHeightDps.HEIGHT_285_DPS){
            binding.getRoot().setOnKeyListener(null);
            isScreensaverShowing =false;
            saverHandler.removeCallbacks(saverRunnable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity.unregisterReceiver(mainUpdateReceiver);
    }

    class MainUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (context.getPackageName().equals(action)) {
                if (MainMenu.getInstance().isChanged()) {
                    //Put at the end of the main thread queue
                    initView();
                }
            }

        }
    }
    /**
     * dot adapter
     *
     * @author Janson
     * @date 2021/11/26 16:10
     */
    private class DotAdapter extends BaseBindingRecyclerAdapter<AppPageDotBinding> {
        private final int count;
        private int selected;

        public DotAdapter(int count) {
            this.count = count;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setSelected(int selected) {
            this.selected = selected;
            notifyDataSetChanged();
        }

        @Override
        protected void bindItemData(AppPageDotBinding itemBinding, int position) {
            itemBinding.ivDot.setSelected(position == selected);
            itemBinding.getRoot().setOnClickListener(v -> binding.viewPager.setCurrentItem(position, true));
        }

        @Override
        public int getItemCount() {
            return count;
        }
    }

    //=======saver for N560K

    private void registerN560Key(){
        if(BDevice.supportPhysicalKeyboard() && heightDps<=ScreenHeightDps.HEIGHT_285_DPS){
            resetSaver();
            ViewUtils.setFocus(binding.getRoot());
            binding.getRoot().setOnKeyListener(new View.OnKeyListener() {
                private int handleKey;

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        resetSaver();
                        handleKey = keyCode;
                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        if (handleKey == keyCode) {
                            switch (keyCode) {
                                case KeyEvent.KEYCODE_STAR:
                                    int index = binding.viewPager.getCurrentItem();
                                    if(index!=0){
                                        binding.viewPager.setCurrentItem(index-1);
                                        return true;
                                    }
                                    break;
                                case KeyEvent.KEYCODE_POUND:
                                    int currentIndex = binding.viewPager.getCurrentItem();
                                    if(currentIndex!=(Objects.requireNonNull(binding.viewPager.getAdapter()).getItemCount()-1)){
                                        binding.viewPager.setCurrentItem(currentIndex+1);
                                        return true;
                                    }
                                    return true;
                                default:
                                    FragmentStateAdapter adapter = (FragmentStateAdapter) binding.viewPager.getAdapter();
                                    if (keyCode >= KeyEvent.KEYCODE_1 && keyCode <= KeyEvent.KEYCODE_9) {
                                        //  MenuFragment currentFragment = childFragments.get(binding.viewPager.getCurrentItem());
                                        MenuFragment currentFragment = (MenuFragment) adapter.createFragment(binding.viewPager.getCurrentItem());
                                        currentFragment.clickByPhysicalKeyBoard(keyCode - KeyEvent.KEYCODE_0-1);
                                        return true;
                                    }
                                    break;
                            }
                        }
                    }
                    return false;
                }
            });


        }
    }
    public void resetSaver() {
        if (isScreensaverShowing) {
            hideScreensaver();
        }
        saverHandler.removeCallbacks(saverRunnable);
        saverHandler.postDelayed(saverRunnable, ParamsUtils.getLong(ParamsConst.PARAMS_KEY_SAVER_SCREEN_TIME,15000));
    }

    /**
     * Enabled screen saver
     */
    private void showScreensaver() {
        isScreensaverShowing = true;
        mActivity.mSupportDelegate.switchContent(SaverFragment.newInstance());
    }

    /**
     * Hide screen saver
     */
    private void hideScreensaver() {
        isScreensaverShowing = false;
        List<Fragment> listFragment  = mActivity.getSupportFragmentManager().getFragments();
        if(listFragment.isEmpty()){
            return;
        }
        Fragment topFragment = listFragment.get(listFragment.size()-1);
        if(topFragment instanceof SaverFragment){
            mActivity.mSupportDelegate.popBackFragment(1);
        }
    }
}
