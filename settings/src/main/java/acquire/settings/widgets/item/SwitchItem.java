package acquire.settings.widgets.item;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import acquire.base.chain.Chain;
import acquire.base.chain.Interceptor;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.bean.CommonBean;
import acquire.settings.databinding.SettingsItemSwitchBinding;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.listener.ItemGetSet;


/**
 * Switch Item
 *
 * @author Janson
 * @date 2018/10/22 11:37
 */
public class SwitchItem implements IItemView {
    private final SettingsItemSwitchBinding binding;

    private SwitchItem(@NonNull Context context) {
        binding = SettingsItemSwitchBinding.inflate(LayoutInflater.from(context));
    }


    @Override
    public View getView() {
        return binding.getRoot();
    }

    public void setChecked(boolean checked) {
        binding.switchButton.setChecked(checked);
    }

    /**
     * Interceptors interrupt
     *
     * @param succResult result callback
     */
    protected void interrupt(List<Interceptor<CommonBean<String>>> interceptors, Runnable succResult) {
        if (interceptors == null) {
            //no interceptors,result callback run
            succResult.run();
            return;
        }
        CommonBean<String> bean = new CommonBean<>();
        Chain<CommonBean<String>> chain = new Chain<>(bean);
        chain.setInterceptors(interceptors);
        chain.proceed(success ->
                ThreadPool.postOnMain(() -> {
                    if (success) {
                        //clear interceptors,result callback run
                        succResult.run();
                    } else {
                        if (bean.getValue() != null) {
                            ToastUtils.showToast(bean.getValue());
                        }
                    }
                })
        );
    }

    /**
     * {@link SwitchItem} Builder
     */
    public static class Builder {
        private final Context context;
        private String title;
        private String message;
        private ItemGetSet<Boolean> getSet;
        private boolean enable = true;
        private List<Interceptor<CommonBean<String>>> interceptors;
        private CompoundButton.OnCheckedChangeListener checkChangeListener;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Set title
         *
         * @param title title text
         * @return {@code Builder.this}
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set title
         *
         * @param titleId title resource id
         * @return {@code Builder.this}
         */
        public Builder setTitle(@StringRes int titleId) {
            this.title = context.getString(titleId);
            return this;
        }

        /**
         * Set content
         *
         * @param message content text
         * @return {@code Builder.this}
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Set content
         *
         * @param messageId content resource id
         * @return {@code Builder.this}
         */
        public Builder setMessage(@StringRes int messageId) {
            this.message = context.getString(messageId);
            return this;
        }

        /**
         * Set params key
         *
         * @param paramKey params key
         * @return {@code Builder.this}
         */
        public Builder setParamKey(String paramKey) {
            this.getSet = new ItemGetSet<Boolean>() {
                @Override
                public Boolean getValue() {
                    return ParamsUtils.getBoolean(paramKey);
                }

                @Override
                public void setValue(Boolean value) {
                    ParamsUtils.setBoolean(paramKey,value);

                }
            };
            return this;
        }
        public Builder setIGetSet(ItemGetSet<Boolean> getSet) {
            this.getSet = getSet;
            return this;
        }
        /**
         * Set params key
         *
         * @param paramKey     params key
         * @param defaultValue default status value
         * @return {@code Builder.this}
         */
        public Builder setParamKey(String paramKey, boolean defaultValue) {
            this.getSet = new ItemGetSet<Boolean>() {
                @Override
                public Boolean getValue() {
                    return ParamsUtils.getBoolean(paramKey,defaultValue);
                }

                @Override
                public void setValue(Boolean value) {
                    ParamsUtils.setBoolean(paramKey,value);

                }
            };
            return this;
        }



        /**
         * Set switch status change listener
         *
         * @param checkChangeListener change listener
         * @return {@code Builder.this}
         */
        public Builder setCheckChangeListener(CompoundButton.OnCheckedChangeListener checkChangeListener) {
            this.checkChangeListener = checkChangeListener;
            return this;
        }

        /**
         * Set Switch Button enable
         *
         * @param enable enable if true,else disable
         * @return {@code Builder.this}
         */
        public Builder setEnable(boolean enable) {
            this.enable = enable;
            return this;
        }

        /**
         * Set interceptors
         *
         * @param interceptors click interceptors
         * @return {@code Builder.this}
         */
        @SafeVarargs
        public final Builder setInterceptors(Interceptor<CommonBean<String>>... interceptors) {
            this.interceptors = new ArrayList<>();
            if (interceptors != null) {
                Collections.addAll(this.interceptors, interceptors);
            }
            return this;
        }

        public SwitchItem create() {
            SwitchItem item = new SwitchItem(context);
            //title
            if (TextUtils.isEmpty(title)) {
                item.binding.tvTitle.setVisibility(View.GONE);
            } else {
                item.binding.tvTitle.setText(title);
            }
            //message
            if (TextUtils.isEmpty(message)) {
                item.binding.tvMessage.setVisibility(View.GONE);
            } else {
                item.binding.tvMessage.setText(message);
            }
            //available
            item.binding.switchButton.setEnabled(enable);
            //checked
            if (getSet != null){
                item.binding.switchButton.setChecked(getSet.getValue());
            }
            item.binding.rlSwitch.setOnClickListener(v -> item.binding.switchButton.toggle());
            //change listener
            item.binding.switchButton.setOnCheckedChangeListener((buttonView, isCheck) -> {
                //save params
                if (getSet != null){
                    getSet.setValue(isCheck);
                }
                //execute listener
                if (checkChangeListener != null) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> checkChangeListener.onCheckedChanged(buttonView, isCheck), 200);
                }
            });
            //interceptors
            if (interceptors != null) {
                item.binding.mask.setOnClickListener(v -> {
                    //execute interceptors
                    item.interrupt(interceptors, () -> {
                        item.binding.mask.setVisibility(View.GONE);
                        item.binding.switchButton.toggle();
                    });
                });
            } else {
                item.binding.mask.setVisibility(View.GONE);
            }
            return item;
        }
    }
}
