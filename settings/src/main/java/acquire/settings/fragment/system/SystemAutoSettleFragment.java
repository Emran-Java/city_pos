package acquire.settings.fragment.system;


import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.DateUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.widget.dialog.time.TimeDialog;
import acquire.core.SchedulerService;
import acquire.core.constant.ParamsConst;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.SwitchItem;
import acquire.settings.widgets.item.TextItem;

/**
 * A {@link Fragment} that sets auto settle time.
 *
 * @author Janson
 * @date 2024/3/8 10:06
 */
public class SystemAutoSettleFragment extends BaseSettingFragment {
    private TextItem timeItem;
    private TimeDialog timeDialog;

    @Override
    protected String getTitle() {
        return getString(R.string.settings_system_item_auto_settle);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_auto_settle)
                .setParamKey(ParamsConst.PARAMS_KEY_AUTO_SETTLE_OPEN)
                .setCheckChangeListener((buttonView, isChecked) -> {
                    SchedulerService.scheduleAutoSettle();
                    refreshItems();
                })
                .create());
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_AUTO_SETTLE_OPEN)) {
            String time = ParamsUtils.getString(ParamsConst.PARAMS_KEY_AUTO_SETTLE_TIME, "2300");
            int curHour = Integer.parseInt(time.substring(0,2));
            int curMinute = Integer.parseInt(time.substring(2,4));
            String formatTime = DateUtils.formatTime(time,DateUtils.HHMM,DateUtils.HH_MM);
            timeItem = new TextItem.Builder(mActivity)
                    .setTitle(R.string.settings_system_auto_settle_item_time)
                    .setMessage(formatTime)
                    .setOnClickListener(v -> {
                        //time dialog
                        if (timeDialog == null){
                            timeDialog =  new TimeDialog.Builder(mActivity)
                                    .hour(curHour)
                                    .minute(curMinute)
                                    .hideSecond()
                                    .setConfirmListener((hour, minute, second) -> {
                                        String hhmm = String.format(Locale.getDefault(),"%02d%02d",hour,minute);
                                        timeItem.setMessage(DateUtils.formatTime(hhmm,DateUtils.HHMM,DateUtils.HH_MM));
                                        ParamsUtils.setString(ParamsConst.PARAMS_KEY_AUTO_SETTLE_TIME,hhmm);
                                        SchedulerService.scheduleAutoSettle();
                                    })
                                    .create();
                        }
                        timeDialog.show();
                    })
                    .create();
            items.add(timeItem);
        }

        return items;
    }

    @Override
    public FragmentCallback<?> getCallback() {
        return null;
    }
}
