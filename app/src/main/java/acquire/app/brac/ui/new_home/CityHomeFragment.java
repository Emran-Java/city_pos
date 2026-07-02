package acquire.app.brac.ui.new_home;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zztl.pos.city.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.zztl.pos.city.databinding.FragmentCityHomeBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import acquire.app.brac.models.FeatureMainMenuModel;
import acquire.app.brac.ui.base.CityBaseFragment;
import acquire.app.brac.ui.home.BrandPromoFragment;
import acquire.app.brac.ui.home.ViewPagerAdapter;
import acquire.app.brac.utility.FileUtility;
import acquire.base.widget.dialog.message.MessageDialog;

public class CityHomeFragment extends CityBaseFragment {

    private FragmentCityHomeBinding _binding;
    private MenuAdapter adapter;
    private CityHomeViewModel viewModel;

    private FeatureMainMenuModel featureTopOneMenuModel;

    private Handler sliderHandler;
    private Runnable sliderRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        _binding = FragmentCityHomeBinding.inflate(inflater, container, false);
        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this)
                .get(CityHomeViewModel.class);

        observeViewModel();

        viewModel.loadData();
        viewModel.loadFeatureMainMenu();

    }

    private void observeViewModel() {

        viewModel.getFeatureMainMenuState().observe(getViewLifecycleOwner(), state -> {
            List<FeatureMainMenuModel> featureMainMenuModels = state.getMainFeatureMenuJson();
            List<FeatureMainMenuModel> subFeatureMainMenuModels = new ArrayList<>();
            if (featureMainMenuModels != null && featureMainMenuModels.size() > 4) {
                subFeatureMainMenuModels.clear();
                subFeatureMainMenuModels.addAll(featureMainMenuModels.subList(0, 4));
            }
            if (subFeatureMainMenuModels.size() > 3) {
                //featureTopOneMenuModel = featureMainMenuModels.get(0);
                updateTopItemOnUi(subFeatureMainMenuModels.get(0));
                subFeatureMainMenuModels.remove(0);
                assert featureMainMenuModels != null;
                setMenuUi(subFeatureMainMenuModels, featureMainMenuModels.size());
            }

        });// end state

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {

            _binding.tvMerchantName.setText(state.getMerchantName());

            FileUtility.getInstance().loadImageFromFileUrl(
                    requireContext(),
                    _binding.ivTopLeftLogo,
                    state.getLeftLogoPath());

            FileUtility.getInstance().loadImageFromFileUrl(
                    requireContext(),
                    _binding.ivTopRightLogo,
                    state.getRightLogoPath());

            readFilesForHomePromotionSlider(state.getSliderPath());

        });// end observer
    }

    private void updateTopItemOnUi(FeatureMainMenuModel featureMainMenuModel) {
        _binding.tvFeatureTopItem.setText(featureMainMenuModel.getTitle());
        _binding.cvTopFeatureItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                featureCall(featureMainMenuModel);
            }
        });
    }

    private void featureCall(FeatureMainMenuModel featureMainMenuModel) {
        if (callbackMainMenuItem != null) {
            callbackMainMenuItem.onSwitchFeature(featureMainMenuModel);
        }
    }

    private void setMenuUi(List<FeatureMainMenuModel> featureMainMenuModels, int realMenuSize) {

        if(realMenuSize>4){
            FeatureMainMenuModel moreFeatureMenuItem = new FeatureMainMenuModel();
            moreFeatureMenuItem.setId(999);
            moreFeatureMenuItem.setTitle("More");
            moreFeatureMenuItem.setCode("MORE");
            moreFeatureMenuItem.setIcon("ic_more");
            moreFeatureMenuItem.setActive(true);
            moreFeatureMenuItem.setShow(true);
            featureMainMenuModels.add(moreFeatureMenuItem);
        }

        adapter = new MenuAdapter(
                requireContext(),
                featureMainMenuModels,
                this::onMenuClicked
        );

        _binding.rvMenuItem.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        _binding.rvMenuItem.setAdapter(adapter);
    }

    private void onMenuClicked(FeatureMainMenuModel menu) {
        featureCall(menu);
    }

    private void setHomeBrandingSlider(ArrayList<FileUtility.FileCountModel> files, boolean autoSlide) {
        ViewPager2 viewPagerHomePromo = _binding.viewPagerHomePromo;


        ViewPagerAdapter homePoromoPagerAdapter = new ViewPagerAdapter(requireActivity());
        for (int i = 0; i < files.size(); i++) {
            //if(!files.get(i).getFileType().equals(FileCounter.EMPTY_FILE_TYPE)){
            if (FileUtility.acceptableMediaFiles.contains(files.get(i).getFileType())) {
                homePoromoPagerAdapter.addFragment(BrandPromoFragment.newInstance(files.get(i).getFilePath()));
            }
        }
        viewPagerHomePromo.setAdapter(homePoromoPagerAdapter);
        viewPagerHomePromo.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        // Optional
        viewPagerHomePromo.setOffscreenPageLimit(1);


        new TabLayoutMediator(
                _binding.tabIndicator,
                _binding.viewPagerHomePromo,
                (tab, position) -> {

                    View dot = new View(requireContext());
                    LinearLayout.LayoutParams lp =
                            new LinearLayout.LayoutParams(14, 14);

                    lp.leftMargin = 2;
                    lp.rightMargin = 2;

                    dot.setLayoutParams(lp);
                    dot.setBackgroundResource(R.drawable.tab_dot);
                    tab.setCustomView(dot);
                }).attach();

        if (autoSlide) {
            startAutoSlide(_binding.viewPagerHomePromo);
        } else {
            stopAutoSlide();
        }
    }
/*

    private void startAutoSlide(ViewPager2 viewPager) {

        stopAutoSlide();

        sliderHandler = new Handler(Looper.getMainLooper());

        sliderRunnable = new Runnable() {
            @Override
            public void run() {

                RecyclerView.Adapter adapter = viewPager.getAdapter();

                if (adapter == null) return;

                int count = adapter.getItemCount();

                if (count <= 1) return;

                int next = viewPager.getCurrentItem() + 1;

                if (next >= count) {
                    next = 0;
                }

                viewPager.setCurrentItem(next, true);

                sliderHandler.postDelayed(this, 3000);
            }
        };

        sliderHandler.postDelayed(sliderRunnable,3000);
    }
*/

    int next2 = 0, adapterGetCount = 0;

    private void startAutoSlide(ViewPager2 viewPager) {

        stopAutoSlide();

        sliderHandler = new Handler(Looper.getMainLooper());
        sliderRunnable = new Runnable() {
            @Override
            public void run() {

                RecyclerView.Adapter adapter = viewPager.getAdapter();
                if (adapter == null || adapter.getItemCount() <= 1) {
                    return;
                }

                int next = viewPager.getCurrentItem() + 1;
                adapterGetCount = adapter.getItemCount();

                if (next >= adapter.getItemCount()) {
                    viewPager.setCurrentItem(0, true); // instant jump
                    next = 0;
                }

                RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
                LinearLayoutManager layoutManager =
                        (LinearLayoutManager) recyclerView.getLayoutManager();
                next2 = next;
                LinearSmoothScroller smoothScroller =
                        new LinearSmoothScroller(requireContext()) {

                            @Override
                            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                                if (next2 >= adapterGetCount) {
                                    return 0.0f;
                                }
                                return 0.3f;

                                // slow> value > fast
                                /*if (next2 >= adapterGetCount) {
                                    //viewPager.setCurrentItem(0, false);
                                    return 0.0f;
                                } else {
                                    return 0.3f;
                                }*/
                            }
                        };
                smoothScroller.setTargetPosition(next);
                layoutManager.startSmoothScroll(smoothScroller);
                sliderHandler.postDelayed(this, 3000);
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    private void stopAutoSlide() {

        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    private void readFilesForHomePromotionSlider(String sliderPath) {

        //String pathWithDirectoryName = FileConst.EXTRA_FILE_URL_CUSTOM_FOLDER;
        ExecutorService executor = Executors.newSingleThreadExecutor();

        //red file from background thread, because this is IO operation
        FileUtility fileUtility = FileUtility.getInstance();
        executor.execute(() -> {
            fileUtility.countFiles(sliderPath);
        });

        fileUtility.setOnFileCountFinishedListener(new FileUtility.OnFilesCountListener() {
            @Override
            public void filesCountCompleted(ArrayList<FileUtility.FileCountModel> files) {
                requireActivity().runOnUiThread(() -> {
                    setHomeBrandingSlider(files, true);
                });
            }
        });
    }

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
    public void onPause() {
        super.onPause();
        stopAutoSlide();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (_binding.viewPagerHomePromo.getAdapter() != null) {
            startAutoSlide(_binding.viewPagerHomePromo);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

}