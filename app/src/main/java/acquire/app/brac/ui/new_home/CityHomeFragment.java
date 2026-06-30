package acquire.app.brac.ui.new_home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zztl.pos.city.databinding.FragmentCityHomeBinding;

import acquire.app.brac.ui.base.BaseFragment;

public class CityHomeFragment extends BaseFragment {

    private FragmentCityHomeBinding binding ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentCityHomeBinding.bind(view);
    }


}
