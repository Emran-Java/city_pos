package acquire.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.chain.Interceptor;
import acquire.core.bean.CommonBean;
import acquire.core.constant.PasswordType;
import acquire.core.fragment.password.PasswordFragment;
import acquire.settings.databinding.SettingsFragmentBaseSettingBinding;
import acquire.settings.widgets.IItemView;

/**
 * The base list settings {@link Fragment}
 *
 * @author Janson
 * @date 2018/10/15 10:39
 */
public abstract class BaseSettingFragment extends BaseFragment {
    private SettingsFragmentBaseSettingBinding binding;
    /**
     * get fragment title
     */
    protected abstract String getTitle();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SettingsFragmentBaseSettingBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(getTitle());
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        //start to add settings item
        refreshItems();
        return binding.getRoot();
    }

    protected void refreshItems(){
        binding.llContainer.removeAllViews();
        List<IItemView> items = getItems();
        for (int i = 0; i < items.size(); i++) {
            IItemView item = items.get(i);
            //add a item
            View child = item.getView();
            binding.llContainer.addView(child);
            if (i != 0) {
                //set item spacing
                ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
                if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) layoutParams;
                    p.setMargins(0, 2, 0, 0);
                    child.setLayoutParams(p);
                }
            }
        }
    }

    /**
     * Settings Items
     */
    protected abstract List<IItemView> getItems();


    /**
     * Require to enter safe password
     */
    protected Interceptor<CommonBean<String>> getSafePasswordInterceptor() {
        return new Interceptor<CommonBean<String>>() {
            @Override
            public void init(CommonBean<String> commonBean) {
            }

            @Override
            public void intercept(Callback callback) {
                mSupportDelegate.switchContent(PasswordFragment.newInstance(PasswordType.SECURITY
                        , new FragmentCallback<String>() {
                            @Override
                            public void onSuccess(String password) {
                                mSupportDelegate.popBackFragment(1);
                                callback.onResult(true);
                            }

                            @Override
                            public void onFail(int errorType, String errorMsg) {
                                mSupportDelegate.popBackFragment(1);
                                callback.onResult(false);
                            }
                        }));
            }
        };
    }


}
