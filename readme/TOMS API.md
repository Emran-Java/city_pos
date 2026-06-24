# TOMS API

This document describes APIs provided by the TOMS platform , includes the following:

- Fly Parameters
- Fly Receipt

## Library

TOMSClientApi_xxx.aar

## External App

TOMS apk

## Fly Parameters

Fly Parameters is the service that the app downloads parameters from **TOMS** platform. 

[FlyParameterHelper](../sdk_helper/src/main/java/acquire/sdk/FlyParameterHelper.java) encapsulates the process of Fly Parameters. 

When the app starts, `FlyParameterHelper` will be initialized  and add the watcher that if TOMS push a update message , `RemoteParamsUpdater` will invoke `FlyParameterHelper`  to download parameters.

```JAVA
public class SelfCheckHelper {
    public static void initDevice(Context context) {
       	...
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_TOMS_FLY_PARAMETERS)) {
            LoggerUtils.d("register TOMS Fly Parameter service");
            boolean result = FlyParameterHelper.getInstance().bind(context);
            if (result) {
                FlyParameterHelper.getInstance().setParameterWatcher(context, () ->
                    new RemoteParamsUpdater().updateParams(BaseApplication.getAppContext())
                );
            }else{
                ToastUtils.showToast(R.string.core_device_fly_parameter_init_failed);
            }
        }else{
            FlyParameterHelper.getInstance().unbind();
        }
    }
}

```

Also, you can execute fly parameters on [SettingUpdateFragment](../settings/src/main/java/acquire/settings/fragment/update/SettingUpdateFragment.java).

```java
public class SettingUpdateFragment extends BaseSettingFragment {
   ...
    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //TOMS FLY params
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_paramfile_fly_parameter)
                .setMessage(R.string.settings_paramfile_fly_parameter_message)
                .setParamKey(ParamsConst.PARAMS_KEY_TOMS_FLY_PARAMETERS)
                .setCheckChangeListener((switchButton, value) -> {
                    refreshItems();
                    if (value) {
                        //bind fly parameter services
                        FLY_SINGLE_EXECUTOR.execute(() -> {
                            boolean result = FlyParameterHelper.getInstance().bind(mActivity);
                            if (result) {
                                FlyParameterHelper.getInstance().setParameterWatcher(mActivity, () ->
                                    new RemoteParamsUpdater().updateParams(BaseApplication.getAppContext())
                                );
                            } else {
                                mActivity.runOnUiThread(() -> {
                                    ToastUtils.showToast(acquire.core.R.string.core_device_fly_parameter_init_failed);
                                    switchButton.setChecked(false);
                                });
                            }
                        });
                    } else {
                        FLY_SINGLE_EXECUTOR.execute(() -> FlyParameterHelper.getInstance().unbind());
                    }
                })
                .create());
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_TOMS_FLY_PARAMETERS)) {
            //update parameters from TOMS FLY params
            items.add(new TextItem.Builder(mActivity)
                    .setTitle(R.string.settings_paramfile_update_by_fly_parameter)
                    .setOnClickListener(v -> new RemoteParamsUpdater().updateParams(mActivity))
                    .create());
        }
		...
        return items;
    }
}
```



## Fly Receipt

Like **Fly Parameters**，**Fly Receipt** is a service of **TOMS**, too.

It will upload the following data to **TOMS**:

1. The receipt data, such as Sale receipt data.
2. The settlement data.

[FlyReceiptHelper](../sdk_helper/src/main/java/acquire/sdk/FlyReceiptHelper.java) encapsulates the process of **Fly Receipt** . It will be executed  at the end of a transaction.

`FlyReceiptHelper`  upload transaction receipt data:

```java
public void sendReceipt(Context context,ReceiptBean receiptBean, FlyReceiptCallback callback) 
```

`FlyReceiptHelper`  upload settlement data:

```java
public void sendSettle(Context context,SettleTicketBean settleTicketBean, FlyReceiptCallback callback)
```

