package acquire.settings.widgets.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import acquire.base.chain.Chain;
import acquire.base.chain.Interceptor;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.bean.CommonBean;
import acquire.settings.databinding.SettingsItemTextBinding;
import acquire.settings.widgets.IItemView;

/**
 * Text Item
 *
 * @author Janson
 * @date 2018/10/22 11:37
 */
public class TextItem implements IItemView {
    protected SettingsItemTextBinding binding;

    protected TextItem(Context context) {
        binding = SettingsItemTextBinding.inflate(LayoutInflater.from(context));
    }

    @Override
    public View getView() {
        return binding.getRoot();
    }

    /**
     * Set Content
     */
    public void setMessage(String content){
        binding.tvMessage.setText(content);
    }

    /**
     * Get Content
     */
    public String getMessage(){
        return binding.tvMessage.getText().toString();
    }

    /**
     * Interceptors interrupt
     *
     * @param succResult result callback
     */
    protected void interrupt(List<Interceptor<CommonBean<String>>> interceptors,Runnable succResult){
        if (interceptors == null) {
            //no interceptors,result callback run
            ThreadPool.postOnMain(succResult);
            return;
        }
        CommonBean<String> bean = new CommonBean<>();
        Chain<CommonBean<String>> chain = new Chain<>(bean);
        chain.setInterceptors(interceptors);
        chain.proceed(success ->
                ThreadPool.postOnMain(() -> {
                    if (success) {
                        //clear interceptors,result callback run
                        ThreadPool.postOnMain(succResult);
                    } else {
                        if (bean.getValue() != null) {
                            ToastUtils.showToast(bean.getValue());
                        }
                    }
                })
        );
    }
    /**
     * {@link TextItem} Builder
     */
    public static class Builder {
        private final Context context;
        private String title;
        private String message;
        private List<Interceptor<CommonBean<String>>> interceptors;
        private View.OnClickListener listener;

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

        /**
         * Set click listener
         *
         * @param listener click listener
         * @return {@code Builder.this}
         */
        public Builder setOnClickListener(View.OnClickListener listener) {
            this.listener = listener;
            return this;
        }

        public TextItem create() {
            TextItem item = new TextItem(context);
            //title
            item.binding.tvTitle.setText(title);
            //content
            if (message != null) {
                item.binding.tvMessage.setText(message);
            }
            if (listener != null) {
                //interceptor
                item.binding.getRoot().setOnClickListener(v -> {
                    if (ViewUtils.isFastClick(300)) {
                        return;
                    }
                    //listener is executed after interceptors are executed.
                    item.interrupt(interceptors,()-> {
                        interceptors = null;
                        listener.onClick(v);
                    });
                });
            }else{
                //hide the arrow image viedw
                item.binding.ivArrow.setVisibility(View.GONE);
            }
            return item;
        }
    }

}
