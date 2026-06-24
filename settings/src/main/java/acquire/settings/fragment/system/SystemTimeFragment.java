package acquire.settings.fragment.system;


import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.DateUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.dialog.date.DateDialog;
import acquire.base.widget.dialog.time.TimeDialog;
import acquire.sdk.device.BDevice;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.TextItem;

/**
 * A {@link Fragment} that sets system time.
 *
 * @author Janson
 * @date 2019/2/14 14:44
 */
public class SystemTimeFragment extends BaseSettingFragment {
    private TextItem dateItem,timeItem;
    private ScheduledFuture<?> future;

    @Override
    protected String getTitle() {
        return getString(R.string.settings_system_item_time);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //Current date
        String date = DateUtils.formatTime(new Date(),DateUtils.YYYY_MM_DD);
        dateItem = new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_system_time_item_date)
                .setMessage(date)
                .setOnClickListener(v->{
                    //date dialog
                    new DateDialog.Builder(mActivity)
                            .setConfirmListener((year, month, day) -> {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year,month-1,day);
                                BDevice.setSystemTime( calendar.getTime());
                                dateItem.setMessage(DateUtils.formatTime(new Date(),DateUtils.YYYY_MM_DD));
                            })
                            .show();
                })
                .create();
        items.add(dateItem);
        //Current time
        String time = DateUtils.formatTime(new Date(),DateUtils.HH_MM_SS);
        timeItem = new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_system_time_item_time)
                .setMessage(time)
                .setOnClickListener(v->{
                    //time dialog
                    new TimeDialog.Builder(mActivity)
                            .setConfirmListener((hour, minute, second) -> {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.HOUR_OF_DAY,hour);
                                calendar.set(Calendar.MINUTE,minute);
                                calendar.set(Calendar.SECOND,second);
                                BDevice.setSystemTime( calendar.getTime());
                                timeItem.setMessage(DateUtils.formatTime(new Date(),DateUtils.HH_MM_SS));
                            })
                            .show();
                })
                .create();
        items.add(timeItem);
        return items;
    }

    @Override
    public FragmentCallback<?> getCallback() {
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        //update time
        if (future == null){
            future = ThreadPool.scheduleAtFixedRate(()->{
                String time = DateUtils.formatTime(new Date(),DateUtils.HH_MM_SS);
                String date = DateUtils.formatTime(new Date(),DateUtils.YYYY_MM_DD);
                mActivity.runOnUiThread(()->{
                    timeItem.setMessage(time);
                    String dateContent = dateItem.getMessage();
                    if (!date.equals(dateContent)){
                        dateItem.setMessage(date);
                    }
                });
            },0,1000);
        }
    }

    @Override
    public void onDestroy() {
        //close update time
        if (future != null){
            future.cancel(true);
        }
        super.onDestroy();
    }
}
