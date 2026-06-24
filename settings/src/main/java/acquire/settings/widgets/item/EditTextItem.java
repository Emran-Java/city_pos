package acquire.settings.widgets.item;


import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;

import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import acquire.base.activity.BaseActivity;
import acquire.base.chain.Interceptor;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.widget.dialog.edit.EditDialog;
import acquire.core.bean.CommonBean;
import acquire.core.constant.Characters;
import acquire.settings.R;
import acquire.settings.widgets.item.listener.ItemGetSet;

/**
 * Edit item
 *
 * @author Janson
 * @date 2021/1/24 11:37
 */
public class EditTextItem extends TextItem {

    protected EditTextItem(Context context) {
        super(context);
        binding.tvMessage.setHint(R.string.settings_not_set);
    }

    /**
     * {@link EditTextItem} Builder
     */
    public static class Builder {
        private final BaseActivity activity;
        private String title;
        private InputFilter[] inputFilters;
        private int maxLen;
        private int minLen;
        private List<Interceptor<CommonBean<String>>> interceptors;
        private String digits;

        private ItemGetSet<String> getSet;

        public Builder(BaseActivity activity) {
            this.activity = activity;
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
            this.title = activity.getString(titleId);
            return this;
        }


        /**
         * Set params key
         *
         * @param paramKey params key
         * @return {@code Builder.this}
         */
        public Builder setParamKey(String paramKey) {
            this.getSet = new ItemGetSet<String>() {
                @Override
                public String getValue() {
                    return ParamsUtils.getString(paramKey);
                }

                @Override
                public void setValue(String value) {
                    ParamsUtils.setString(paramKey, value);
                }
            };
            return this;
        }

        public Builder setIGetSet(ItemGetSet<String> getSet) {
            this.getSet = getSet;
            return this;
        }

        /**
         * Set digits limit
         *
         * @param digits allow digits 。
         * @see Characters
         */
        public Builder setDigits(String digits) {
            this.digits = digits;
            return this;
        }

        /**
         * Set input filters
         *
         * @param inputFilters input filters
         */
        public Builder setInputFilter(InputFilter... inputFilters) {
            this.inputFilters = inputFilters;
            return this;
        }


        /**
         * Set input max length
         *
         * @param maxLen max length
         * @return {@code Builder.this}
         */
        public Builder setInputMaxLen(int maxLen) {
            this.maxLen = maxLen;
            return this;
        }

        /**
         * Set input min length, default 1.
         *
         * @param minLen min length
         * @return {@code Builder.this}
         */
        public Builder setInputMinLen(int minLen) {
            this.minLen = minLen;
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
         * Create Item
         *
         * @return {@link EditTextItem}
         */
        public EditTextItem create() {
            EditTextItem item = new EditTextItem(activity);
            //set title
            item.binding.tvTitle.setText(title);
            //set value
            if (getSet != null) {
                item.binding.tvMessage.setText(getSet.getValue());

            }
            //interrupt
            item.binding.getRoot().setOnClickListener(v -> {
                if (ViewUtils.isFastClick(300)) {
                    return;
                }
                //show edit dialog after interceptors are executed.
                item.interrupt(interceptors, () -> {
                    interceptors = null;
                    Integer inputType = null;
                    if (digits != null && TextUtils.isDigitsOnly(digits)) {
                        //set input type number
                        inputType = InputType.TYPE_CLASS_NUMBER;
                    }
                    //show edit dialog
                    new EditDialog.Builder(activity)
                            .setTitle(title)
                            .setContent(item.binding.tvMessage.getText().toString())
                            //Set input type
                            .setInputType(inputType)
                            //Set filters
                            .setDigits(digits)
                            .setMaxLength(maxLen)
                            .setMinLength(minLen)
                            .setFilters(inputFilters)
                            //Confirm button
                            .setConfirmButton((editDialog, text) -> {
                                //show text
                                item.binding.tvMessage.setText(text);
                                if (getSet != null) {
                                    getSet.setValue(text);
                                }
                                return true;
                            })
                            .show();
                });
            });
            return item;
        }
    }


}
