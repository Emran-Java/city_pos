package acquire.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;

import acquire.base.activity.BaseActivity;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.activity.callback.SimpleCallback;
import acquire.base.utils.BytesUtils;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.crypto.ShaUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.constant.PasswordType;
import acquire.core.fragment.password.PasswordFragment;
import acquire.settings.databinding.SettingsActivityBinding;
import acquire.settings.fragment.SettingFragment;
import acquire.settings.fragment.VendorFragment;
import acquire.settings.fragment.brac_setting.BracMainSettingFragment;


/**
 * An app settings {@link Activity}
 *
 * @author Janson
 * @date 2019/4/24 18:42
 */
public class SettingsActivity extends BaseActivity {

    @Override
    public int attachFragmentResId() {
        return R.id.fl_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsActivityBinding binding = SettingsActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        //set immersed status bar.
        DisplayUtils.immersedStatusBar(getWindow());
        //need to enter settings password

        mSupportDelegate.switchContent(PasswordFragment.newInstance(getString(R.string.settings_menu_title), PasswordType.SYSTEM_ADMIN,new FragmentCallback<String>() {
            @Override
            public void onSuccess(String password) {
                ThreadPool.postDelayOnMain(()->{
                    if (PasswordFragment.VENDOR_PASSWORD_HASH.equals(password)) {
                       //enter vendor fragment
                        mSupportDelegate.switchContent(BracMainSettingFragment.newInstance(new SimpleCallback() {
                            @Override
                            public void result() {
                                setResult(Activity.RESULT_OK);
                                finish();
                            }
                        }));
                    }else{
                        //enter settings fragment
                        mSupportDelegate.switchContent(SettingFragment.newInstance(new SimpleCallback() {
                            @Override
                            public void result() {
                                setResult(Activity.RESULT_OK);
                                finish();
                            }
                        }));
                    }
                },10); //Delay 10 ms, display entering animation
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                setResult(Activity.RESULT_OK);
                finish();

            }
        }));
    }



}
