package acquire.app.brac.ui.new_home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zztl.pos.city.databinding.FragmentCityFeatureMenuBinding;

import acquire.app.brac.ui.base.CityBaseFragment;

public class CityFeatureMenuFragment extends CityBaseFragment {

    private FragmentCityFeatureMenuBinding _binding;

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

        initUI();

    }

    private void initUI() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }
}

