# RKL

**RKL** implements the function of downloading keys from **Newland** platform.

## Library

NSDK-Plugin-RKL-xxx.aar

## Wrapper Class

[FlyKeyHelper](../sdk_helper/src/main/java/acquire/sdk/FlyKeyHelper.java) encapsulates the process of key download. Simply call its method `downloadMsterKey` to complete the key download.

```java
public static void downloadMsterKey(Context context, boolean isExternalPinpad, FlyKeyListener listener)
```

If the key needs to be injected into a external PIN pad, please set`isExternalPinpad` true.

## Download Key

You can start to download key on  [SettingKeyManageFragment](../settings/src/main/java/acquire/settings/fragment/key/SettingKeyManageFragment.java).

```java
public class SettingKeyManageFragment extends BaseSettingFragment {
    @Override
    protected List<IItemView> getItems() {
		...
        //FLY key
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_pinpad_item_fly_key)
                .setMessage(R.string.settings_pinpad_fly_key_summary)
                .setOnClickListener(v -> {
                    new MenuDialog.Builder(mActivity)
                            .setTitle(R.string.settings_pinpad_fly_key_dialog_title)
                            .setItems(getString(R.string.settings_pinpad_fly_key_dialog_built_in), getString(R.string.settings_pinpad_fly_key_dialog_external))
                            .setConfirmButton(index -> {
                                boolean externalPinpad = index == 1;
                                if (externalPinpad && !ExtServiceHelper.getInstance().isInit()) {
                                    ToastUtils.showToast(R.string.settings_pinpad_fly_key_connect_external_pin_pad);
                                    return;
                                }
                                flykey(externalPinpad);
                            })
                            .setCancelButton(dialog -> {
                            })
                            .show();
                })
                .create());
        return items;
    }

    private void flykey(boolean externalPinpad) {
        new ProgressDialog.Builder(mActivity)
                .setContent(R.string.settings_pinpad_fly_key_sending)
                .setShowListener(dialog ->
                        FlyKeyHelper.downloadMsterKey(mActivity, externalPinpad, new FlyKeyHelper.FlyKeyListener() {
                            @Override
                            public void onSuccess(@NonNull int[] indexes) {
                                mActivity.runOnUiThread(() -> {
                                    dialog.dismiss();
                                    if (indexes.length == 0) {
                                        ToastUtils.showToast(R.string.settings_pinpad_fly_key_no_key);
                                    } else {
                                        ToastUtils.showToast(R.string.settings_pinpad_fly_key_success);
                                    }
                                });

                            }

                            @Override
                            public void onFailed(FlyKeyHelper.FlyKeyErrorBean errorBean) {
                                mActivity.runOnUiThread(() -> {
                                    dialog.dismiss();
                                    new MessageDialog.Builder(mActivity)
                                            .setMessage(errorBean.getMessage())
                                            .setConfirmButton(dialog1 -> {
                                            })
                                            .show();
                                });
                            }


                            @Override
                            public void onException(Exception e) {
                                e.printStackTrace();
                                mActivity.runOnUiThread(() -> {
                                    dialog.dismiss();
                                    ToastUtils.showToast(R.string.settings_pinpad_fly_key_exception);
                                });

                            }
                        })
                )
                .show();
    }
}
```

