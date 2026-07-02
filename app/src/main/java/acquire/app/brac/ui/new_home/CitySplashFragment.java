package acquire.app.brac.ui.new_home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zztl.pos.city.databinding.FragmentCitySplashBinding;

import acquire.app.brac.ui.base.CityBaseFragment;
import acquire.base.utils.DisplayUtils;

public class CitySplashFragment extends CityBaseFragment {

    private FragmentCitySplashBinding _binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        _binding = FragmentCitySplashBinding.inflate(inflater, container, false);
        return _binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUI();
        DisplayUtils.getAllDisplayInfo(requireContext());
    }

    private void initUI() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }
}