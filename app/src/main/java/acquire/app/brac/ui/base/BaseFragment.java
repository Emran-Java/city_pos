package acquire.app.brac.ui.base;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import acquire.app.brac.ui.new_home.ActivityCallback;

public class BaseFragment extends Fragment {

    protected ActivityCallback callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof ActivityCallback) {
            callback = (ActivityCallback) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}
