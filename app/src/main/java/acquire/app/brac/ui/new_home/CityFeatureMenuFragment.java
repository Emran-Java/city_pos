package acquire.app.brac.ui.new_home;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.zztl.pos.city.databinding.FragmentCityFeatureMenuBinding;

import java.util.ArrayList;

import acquire.app.brac.models.FeatureMainMenuModel;
import acquire.app.brac.ui.base.CityBaseFragment;

public class CityFeatureMenuFragment extends CityBaseFragment {

    private FragmentCityFeatureMenuBinding _binding;

    private MenuAdapter adapter;
    private ArrayList<FeatureMainMenuModel> _featureMainMenuModels;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // return super.onCreateView(inflater, container, savedInstanceState);
        _binding = FragmentCityFeatureMenuBinding.inflate(inflater, container, false);
        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _binding.toolbar.setTitle(mActivity.getTitle());
        _binding.toolbar.setBackListener(v->mActivity.getOnBackPressedDispatcher().onBackPressed());

        initAdapter();
        initArgumentData();

    }

    private void initArgumentData() {

        Bundle args = getArguments();

        if (args == null || !args.containsKey("featureMenuList")) {
            //TODO: when we cant get data
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            _featureMainMenuModels = (ArrayList<FeatureMainMenuModel>)
                    args.getSerializable("featureMenuList", ArrayList.class);
        } else {
            _featureMainMenuModels = (ArrayList<FeatureMainMenuModel>)
                    args.getSerializable("featureMenuList");
        }

        if (_featureMainMenuModels != null && _featureMainMenuModels.size() > 0) {
            setAndUpdateMenuList(_featureMainMenuModels);
        }

    }

    private void setAndUpdateMenuList(ArrayList<FeatureMainMenuModel> featureMainMenuModels) {
        if(adapter==null ){
            initAdapter();
        }
        if (featureMainMenuModels != null)
            adapter.updateListData(featureMainMenuModels);
    }

    private void initAdapter() {
        //Initialize Menu
        adapter = new MenuAdapter(
                requireContext(),
                new ArrayList<FeatureMainMenuModel>(),
                this::onMenuClicked
        );

        _binding.rvMenuItem.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        _binding.rvMenuItem.setAdapter(adapter);
    }

    private void onMenuClicked(FeatureMainMenuModel featureMainMenuModel) {
        callbackMainMenuItem.onSwitchFeature(featureMainMenuModel);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }
}

