package acquire.base.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import acquire.base.BaseApplication;
import acquire.base.R;

/**
 * @author Janson
 * @date 2023/4/20 10:24
 */
public abstract class BaseDialogFragment extends BaseFragment {

    public abstract View onCreateDialogView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Context context = getContext();
        View view = onCreateDialogView(inflater, container, savedInstanceState);
        RelativeLayout relativeLayout = new RelativeLayout(context);
        int paddingHorizontal  = getResources().getDimensionPixelOffset(R.dimen.base_dialog_fragment_content_padding_horizontal);
        int paddingVertical  = getResources().getDimensionPixelOffset(R.dimen.base_dialog_fragment_content_padding_vertical);
        relativeLayout.setPadding(paddingHorizontal,paddingVertical,paddingHorizontal,paddingVertical);
        RelativeLayout.LayoutParams viewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        relativeLayout.addView(view, viewParams);
        relativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.base_translucent));
        return relativeLayout;
    }

    @Override
    public void onFragmentShow() {
        super.onFragmentShow();
        if (getView() != null) {
            getView().setBackgroundColor(ContextCompat.getColor(BaseApplication.getAppContext(), R.color.base_translucent));
        }
    }

    @Override
    public void onFragmentHide() {
        super.onFragmentHide();
        if (getView() != null) {
            getView().setBackgroundColor(ContextCompat.getColor(BaseApplication.getAppContext(), android.R.color.transparent));
        }
    }
}
