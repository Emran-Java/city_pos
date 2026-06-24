package acquire.app.brac.ui.home;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zztl.pos.city.R;

import acquire.app.brac.utility.FileCounter;
import com.zztl.pos.city.databinding.AppFragmentMainBracBinding;
import acquire.app.fragment.main.SaverFragment;
import acquire.app.fragment.main.menu.MainMenu;
import acquire.app.fragment.main.menu.MenuItem;
import acquire.base.ActivityStackManager;
import acquire.base.activity.BaseFragment;
import acquire.base.activity.bottom_sheet.MessageBottomSheet;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.TransActivity;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ScreenHeightDps;
import acquire.core.constant.TransTag;
import acquire.core.constant.TransType;
import acquire.core.fragment.key_board.CoreNumberPadBottomSheet;
import acquire.sdk.device.BDevice;
import acquire.settings.contents.MenuChangeListen;

/**
 * A main {@link Fragment}
 *
 * @author Janson
 * @date 2019/1/28 10:22
 */
public class MainBracFragment extends BaseFragment {
    /**
     * menu page row
     */
    private static int PAGE_ROW = 4;

    private final static String DEFAULT_AMOUNT = "0.00";

    private final MainUpdateReceiver mainUpdateReceiver = new MainUpdateReceiver();
    private AppFragmentMainBracBinding binding;

    @NonNull
    public static MainBracFragment newInstance() {
        return new MainBracFragment();
    }

    private boolean isScreensaverShowing = false;
    private Handler saverHandler;
    private final Runnable saverRunnable = () -> {
        if (!isScreensaverShowing) {
            showScreensaver();
        }
    };

    private float heightDps;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = AppFragmentMainBracBinding.inflate(inflater, container, false);


        DisplayMetrics metrics = DisplayUtils.getDisplayMetrics(mActivity);
        heightDps = metrics.heightPixels / metrics.density;

        if (heightDps >= ScreenHeightDps.HEIGHT_750_DPS) {
            PAGE_ROW = 5;
        } else {
            if (heightDps <= ScreenHeightDps.HEIGHT_456_DPS) {
                //N560K,S30
                PAGE_ROW = 2;
            } else if (heightDps <= ScreenHeightDps.HEIGHT_530_DPS) {
                PAGE_ROW = 3;
            }
        }

        initView();

        IntentFilter filter = new IntentFilter();
        filter.addAction(mActivity.getPackageName());
        mActivity.registerReceiver(mainUpdateReceiver, filter);
        saverHandler = new Handler();
        initClickListener();
        amountInput();
        readFilesForHomePromotionSlider();

        return binding.getRoot();
    }

    private void readFilesForHomePromotionSlider() {

        //Before release, this path will be read from config.ini file
//        String pathWithDirectoryName = "/storage/emulated/0/custom_brac";
        String pathWithDirectoryName = "/storage/emulated/0/custom_ucb";

        ExecutorService executor = Executors.newSingleThreadExecutor();

        //red file from background thread, because this is IO operation
        FileCounter fileCounter = FileCounter.getInstance();
        executor.execute(() -> {
            fileCounter.countFiles(pathWithDirectoryName);
        });

        fileCounter.setOnFileCountFinishedListener(new FileCounter.OnFilesCountListener() {
            @Override
            public void filesCountCompleted(ArrayList<FileCounter.FileCountModel> files) {
                requireActivity().runOnUiThread(() -> {
                    setHomeBrandingSlider(files);
                });
            }
        });

    }

    private void setHomeBrandingSlider(ArrayList<FileCounter.FileCountModel> files) {
        ViewPager2 viewPagerHomePromo = binding.viewPagerHomePromo;

        boolean isAutoSlide = true;

        ViewPagerAdapter homePoromoPagerAdapter = new ViewPagerAdapter(requireActivity());
        for (int i = 0; i < files.size(); i++) {
            //if(!files.get(i).getFileType().equals(FileCounter.EMPTY_FILE_TYPE)){
            if (FileCounter.acceptableMediaFiles.contains(files.get(i).getFileType())) {
                homePoromoPagerAdapter.addFragment(BrandPromoFragment.newInstance(files.get(i).getFilePath()));
            }
        }

        viewPagerHomePromo.setAdapter(homePoromoPagerAdapter);

//        viewPagerHomePromo.setUserInputEnabled(false);

        viewPagerHomePromo.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        // Optional
        viewPagerHomePromo.setOffscreenPageLimit(1);
    }

    private void amountInput() {

        binding.tvAmount.setText(DEFAULT_AMOUNT);

       /* binding.tvAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //enable enter key
                boolean enableEnter = !DEFAULT_AMOUNT.equals(s.toString());
                //binding.keyboardNumber.findKey(BaseKeyboard.K_ENTER).setEnabled(enableEnter);
            }
        });*/
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (BDevice.supportPhysicalKeyboard() && heightDps <= ScreenHeightDps.HEIGHT_285_DPS) {
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


    private void showPinSubmitSheet(String transType) {
        boolean isNumberShuffle = false;
        CoreNumberPadBottomSheet bottomSheetFragment =
                CoreNumberPadBottomSheet.Companion.newInstance(new CoreNumberPadBottomSheet.ItemClickListener() {
                    @Override
                    public void onCancelButtonClick() {

                    }

                    @Override
                    public void onBottomSheetItemClick(@Nullable String amountValue, boolean isTakeAction) {

                        if (amountValue != null && amountValue.isEmpty()) amountValue = "0.00";
                        binding.tvAmount.setText(amountValue);
                        if (isTakeAction) {
                            Intent intent = new Intent(mActivity, TransActivity.class);
                            intent.putExtra(TransTag.TRANS_TYPE, transType);
                            intent.putExtra(TransTag.AMOUNT, amountValue);
                            ActivityCompat.startActivity(mActivity, intent, null);
                            binding.tvAmount.setText("0.00");
                        }
                    }
                }, isNumberShuffle, true, true);

        bottomSheetFragment.setCancelable(false);
        bottomSheetFragment.show(getChildFragmentManager(), bottomSheetFragment.getTag());
    }


    private void initView() {
        List<MenuItem> items;

        items = MainMenu.getInstance().getMenu();

        List<MenuItem> functionItems;
        //top item
        if (heightDps <= ScreenHeightDps.HEIGHT_285_DPS) {
            functionItems = items;
        } else {
            MenuItem topItem = items.get(0);
            functionItems = items.subList(1, items.size());
            if (binding.ivTopIcon != null) {
                binding.ivTopIcon.setImageResource(topItem.getIcon());
            }
            if (binding.tvTopName != null) {
                binding.tvTopName.setText(topItem.getName());
            }
            if (binding.llSl != null) {
                if (topItem.getColorId() != 0) {
                    binding.llSl.setBackgroundColor(ContextCompat.getColor(mActivity, topItem.getColorId()));
                }
                binding.llSl.setOnClickListener(v -> {
                    if (ActivityStackManager.getTopActivity() instanceof TransActivity) {
                        return;
                    }

                    if (!ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_IS_OPERATOR_AVAILABLE, false)) {
                        showBsMessage();
                    } else {
                        showPinSubmitSheet(topItem.getTransType());
                    }

                    //start transaction
                    /* Intent intent = new Intent(mActivity, TransActivity.class);
                    intent.putExtra(TransTag.TRANS_TYPE, topItem.getTransType());
                    ActivityCompat.startActivity(mActivity, intent, null);*/

                });
            }
        }
        //menu page
       /* List<MenuBankItemsFragment> childFragments = createMenuFragments(functionItems);
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
        }*/
    }

    private void initClickListener() {

        binding.ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoClickCount();
            }
        });

        binding.ivImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                logoLongClick();
                return false;
            }
        });
    }


/*
    @NonNull
    private List<MenuBankItemsFragment> createMenuFragments(@NonNull List<MenuItem> items) {
        List<MenuBankItemsFragment> fragments = new ArrayList<>();
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
                    fragItems.add(MenuAdapter.FILL_PLACE_ITEM);
                }
            }
            if(BDevice.supportPhysicalKeyboard() && heightDps<=ScreenHeightDps.HEIGHT_285_DPS){
                fragments.add(MenuBankItemsFragment.newInstance(fragItems, this::resetSaver));
            }else {
                fragments.add(MenuBankItemsFragment.newInstance(fragItems));
            }

        }
        return fragments;
    }
*/

    @Override
    public boolean onBack() {
        //Confirm to exit this application
        new MessageDialog.Builder(mActivity)
                .setMessage(R.string.app_exit_prompt)
                .setConfirmButton(dialog -> mActivity.finish())
//                .setConfirmButton(dialog -> {})
                .setCancelButton(dialog -> {
                })
                .show();
        return true;
    }

    @Override
    public void onFragmentShow() {
        super.onFragmentShow();
        //registerN560Key();
    }

    @Override
    public void onResume() {
        super.onResume();

        /*try {
            initView();
        }catch (Exception ex){

        }*/

        List<Fragment> listFragment = mActivity.getSupportFragmentManager().getFragments();
        if (!listFragment.isEmpty()) {
            Fragment topFragment = listFragment.get(listFragment.size() - 1);
            if (topFragment instanceof MainBracFragment) {
                // registerN560Key();
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (BDevice.supportPhysicalKeyboard() && heightDps <= ScreenHeightDps.HEIGHT_285_DPS) {
            binding.getRoot().setOnKeyListener(null);
            isScreensaverShowing = false;
            saverHandler.removeCallbacks(saverRunnable);
        }
    }


    @Override
    public void onFragmentHide() {
        super.onFragmentHide();
        if (BDevice.supportPhysicalKeyboard() && heightDps <= ScreenHeightDps.HEIGHT_285_DPS) {
            binding.getRoot().setOnKeyListener(null);
            isScreensaverShowing = false;
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
/*    private class DotAdapter extends BaseBindingRecyclerAdapter<AppPageDotBinding> {
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
//            itemBinding.ivDot.setSelected(position == selected);
//            itemBinding.getRoot().setOnClickListener(v -> binding.viewPager.setCurrentItem(position, true));
        }

        @Override
        public int getItemCount() {
            return count;
        }
    }*/

    //=======saver for N560K
/*
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
//                                        MenuFragment currentFragment = (MenuFragment) adapter.createFragment(binding.viewPager.getCurrentItem());
                                        assert adapter != null;
                                        MenuBankItemsFragment currentFragment = (MenuBankItemsFragment) adapter.createFragment(binding.viewPager.getCurrentItem());
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
    }*/
    private void showBsMessage() {
        MessageBottomSheet sheet =
                MessageBottomSheet.newInstance(
                        "Connectivity exception",
                        R.drawable.ic_connectivity_issue,
                        true,
                        false,
                        "OK",
                        "Yes"
                );

        sheet.setActionListener(new MessageBottomSheet.BottomSheetActionListener() {
            @Override
            public void onLeftButtonClick() {


                /*// প্রথমে লক টাস্ক মোড বন্ধ করতে হবে
                mActivity.stopLockTask();
                // তারপর অ্যাপটি পুরোপুরি বন্ধ করে দেওয়া হবে
                mActivity.finishAffinity();*/

                /*if (item.getTransType().equalsIgnoreCase(TransType.TRANS_REPRINT_RECEIPT)
                        || item.getTransType().equalsIgnoreCase(TransType.TRANS_REPRINT_RECEIPT_MENU)
                        || item.getTransType().equalsIgnoreCase(TransType.TRANS_REPRINT_LAST_RECEIPT)
                        || item.getTransType().equalsIgnoreCase(TransType.TRANS_SETTINGS)
                        || item.getTransType().equalsIgnoreCase(TransType.TRANS_ABOUT)
                        || item.getTransType().equalsIgnoreCase(TransType.TRANS_HELP_CENTER)
                        || item.getTransType().equalsIgnoreCase(TransType.TRANS_REPORTS_PRINT)
                ) {
                    actionItemListener(item);
                }*/
            }

            @Override
            public void onRightButtonClick() {

            }
        });
        sheet.setCancelable(false);
        sheet.show(
                mActivity.getSupportFragmentManager(),
                "message_sheet"
        );
    }

    public void resetSaver() {
        if (isScreensaverShowing) {
            hideScreensaver();
        }
        saverHandler.removeCallbacks(saverRunnable);
        saverHandler.postDelayed(saverRunnable, ParamsUtils.getLong(ParamsConst.PARAMS_KEY_SAVER_SCREEN_TIME, 15000));
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
        List<Fragment> listFragment = mActivity.getSupportFragmentManager().getFragments();
        if (listFragment.isEmpty()) {
            return;
        }
        Fragment topFragment = listFragment.get(listFragment.size() - 1);
        if (topFragment instanceof SaverFragment) {
            mActivity.mSupportDelegate.popBackFragment(1);
        }
    }
}
