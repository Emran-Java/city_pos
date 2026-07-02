package acquire.app.brac.ui.base;

import android.content.Context;

import androidx.annotation.NonNull;

import acquire.app.brac.ui.new_home.ActivityCallback;
import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;

public class CityBaseFragment extends BaseFragment {

    protected ActivityCallback callbackMainMenuItem;

    @Override
    public FragmentCallback getCallback() {
        return null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof ActivityCallback) {
            callbackMainMenuItem = (ActivityCallback) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbackMainMenuItem = null;
    }
}
