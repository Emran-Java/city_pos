package acquire.settings.widgets.item;


import android.content.Context;
import android.text.InputType;

import androidx.annotation.StringRes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import acquire.base.activity.BaseActivity;
import acquire.base.chain.Interceptor;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.StringUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.widget.AmountFilter;
import acquire.base.widget.dialog.edit.EditDialog;
import acquire.core.bean.CommonBean;
import acquire.settings.R;
import acquire.settings.widgets.item.listener.ItemGetSet;

/**
 * Amount item
 *
 * @author Janson
 * @date 2021/1/24 11:37
 */
public class AmountItem extends TextItem {
    protected AmountItem(Context context) {
        super(context);
        binding.tvMessage.setHint(R.string.settings_not_set);
    }

    /**
     * {@link AmountItem} Builder
     */
    public static class Builder {
        private final BaseActivity activity;
        private String title;
        private Double minValue, maxValue;
        private List<Interceptor<CommonBean<String>>> interceptors;
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
         * Set input max value
         *
         * @param maxValue max value
         * @return {@code Builder.this}
         */
        public Builder setMaxValue(double maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        /**
         * Set input min value
         *
         * @param minValue min value
         * @return {@code Builder.this}
         */
        public Builder setMinValue(double minValue) {
            this.minValue = minValue;
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
         * @return {@link AmountItem}
         */
        public AmountItem create() {
            AmountItem item = new AmountItem(activity);
            //set title
            item.binding.tvTitle.setText(title);
            //set value
            String value = null;
            if (getSet != null) {
                value = getSet.getValue();
            }
            try {
                long longAmount = Long.parseLong(value);
                String formatAmount = FormatUtils.formatAmount(longAmount, 2, "");
                item.binding.tvMessage.setText(formatAmount);
            } catch (Exception e) {
                LoggerUtils.e("parseLong "+value+" failed!",e);
                item.binding.tvMessage.setText(value);
            }
            //set input max length
            int maxLen;
            if (maxValue != null) {
                maxLen = StringUtils.doubelToStr(maxValue).length();
            } else {
                maxLen = 64;
            }
            int minLen;
            if (minValue != null) {
                minLen = StringUtils.doubelToStr(minValue).length();
            } else {
                minLen = 0;
            }
            //set interceptors
            item.binding.getRoot().setOnClickListener(v -> {
                if (ViewUtils.isFastClick(300)) {
                    return;
                }
                //show edit dialog after interceptors are executed.
                item.interrupt(interceptors, () -> {
                    interceptors = null;
                    //show edit dialog
                    new EditDialog.Builder(activity)
                            .setTitle(title)
                            .setContent(item.binding.tvMessage.getText().toString())
                            //Float edit
                            .setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
                            //Set filters
                            .setFilters(new AmountFilter(2))
                            .setMaxLength(maxLen)
                            .setMinLength(minLen)
                            //Confirm button
                            .setConfirmButton((editDialog, text) -> {
                                //text to double
                                double textDouble;
                                try {
                                    textDouble = Double.parseDouble(text);
                                } catch (Exception e) {
                                    LoggerUtils.e("parseDouble "+text+" failed!",e);
                                    editDialog.getContentEdit().setError(activity.getString(R.string.settings_widget_edit_amount_format_wrong));
                                    return false;
                                }
                                //Check max and min value
                                if (maxValue != null || minValue != null) {
                                    //max value
                                    if (maxValue != null && textDouble > maxValue) {
                                        editDialog.getContentEdit().setError(activity.getString(R.string.settings_widget_edit_amount_too_big_frontfm) + StringUtils.doubelToStr(maxValue));
                                        return false;
                                    }
                                    //min value
                                    if (minValue != null && textDouble < minValue) {
                                        editDialog.getContentEdit().setError(activity.getString(R.string.settings_widget_edit_amount_too_small_frontfm) + StringUtils.doubelToStr(minValue));
                                        return false;
                                    }
                                }
                                //convert to long amount
                                long amountValue = BigDecimal.valueOf(textDouble)
                                        .multiply(new BigDecimal(100))
                                        .longValue();
                                if (getSet != null) {
                                    getSet.setValue(String.valueOf(amountValue));
                                }
                                //show amount
                                item.binding.tvMessage.setText(text);
                                return true;
                            })
                            .show();

                });
            });
            return item;
        }
    }


}
