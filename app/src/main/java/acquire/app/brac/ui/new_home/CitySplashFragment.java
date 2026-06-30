package acquire.app.brac.ui.new_home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zztl.pos.city.databinding.FragmentCitySplashBinding;

import acquire.app.brac.ui.base.BaseFragment;

public class CitySplashFragment extends BaseFragment {

    private FragmentCitySplashBinding _binding;
    protected FragmentCitySplashBinding binding() {
        return _binding;
    }

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

        // Initialize UI / listeners here
        initUI();
    }

    private void initUI() {
        // Example:
        // binding().textTitle.setText("Welcome");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }
}