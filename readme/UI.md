# UI

This document describes the following:

- Theme
- Screen adaptation
- Custom Widgets
- Main Menu
- Settings Item

## Theme

**BankTemplate** use the material theme as the basic theme.

In [base/res/values/base_themes.xml](../base/src/main/res/values/base_themes.xml), there are the `AppTheme` for application theme,  `AppTheme.Transparent` for TransActivity and ThirdActivity  `AppTheme.Launch` for MainActivity. `MyMaterialButtonStyle` can set  the all material button default text size. 

In [base/res/values/base_styles.xml](../base/src/main/res/values/base_styles.xml),  there are the non-activity component theme, such as  dialog, buttons.

In [base/res/values/base_colors.xml](../base/src/main/res/values/base_colors.xml),  there are basic colors, such as primary color, secondary color. They will be used for themes, views background, text color , and so on. The main purpose of doing this is to unify the views of colors.

In [base/res/values/base_dimens.xml](../base/src/main/res/values/base_dimens.xml),  there are basic size, such as text size, view size. The main purpose of doing this is to unify the views of size.

To adapt to different screens, different resource files are required. See next section.



## Screen Adaptation

The default resource file adapts to most Newland POS  but some models need to add a specified suffix to adapt.

The following takes **values directory** as an example (also applies to **drawable** and **layout**)

**smallest width solution (Current use):**

- `values-sw530dp` :	X800 (Main screen)
- `values-sw540dp-land` :	X800 (Secondary screen)
- `values-sw320dp-mdpi` :	U200
- `values-sw320dp-hdpi`:  N950K
- `values-sw360dp` :	N950，N950S，N910，N910Pro,  N960K, N750，NS960 Pro
- `values-sw360dp-land` :	P300
- `values-sw720dp-land` :	CPOS X5, 	CPOS X3
- `values` : other device

**dimension solution:**

- `values-800x1208` :	X800 (Main screen)
- `values-land-800x480` :	X800 (Secondary screen)
- `values-480x1782` :	N950K
- `values-320x432` :	U200
- `values-720x1504` :	S6 Pro
- `values-land-1196x720` :	P300
- `values-land-1920x1008` :	CPOS X5, 	CPOS X3
- `values`: other device

SW(**smallest width**) solution is recommended. The Android system will search for resources based on the closest SW value, so you don't need to create SW folders for each screen, just create resources for general and special screens.



## Custom Widgets

In folder [base/widget](../base/src/main/java/acquire/base/widget), many widgets are provided for use by other modules.

### Dialog

In daily business, dialogs need to be frequently used, so **BankTemplate** designs a set of easy-to-use dialog classes.

[BaseDialog](../base/src/main/java/acquire/base/widget/dialog/BaseDialog.java) is the basic dialog class.  It uses the `R.style.BaseDialog` as the default dialog style. And it has the timeout function and can safe dialog when its activity finishes to avoid window disconnection exception.  Its subclasses include:  

[MessageDialog](../base/src/main/java/acquire/base/widget/dialog/message/MessageDialog.java) : shows the message.

[CheckDialog](../base/src/main/java/acquire/base/widget/dialog/check/CheckDialog.java) : shows a  menu 投.

[DateDialog](../base/src/main/java/acquire/base/widget/dialog/date/DateDialog.java)：selects the date.

[TimeDialog](../base/src/main/java/acquire/base/widget/dialog/time/TimeDialog.java): selects the time.

[EditDialog](../base/src/main/java/acquire/base/widget/dialog/edit/EditDialog.java): shows the edit.

[MenuDialog](../base/src/main/java/acquire/base/widget/dialog/menu/MenuDialog.java)  : shows a menu to select a item..

They use the Builder Pattern to configure the parameters.  **For example**:

```java
 new MessageDialog.Builder(mActivity)
                .setMessage("title")
                .setConfirmButton("ok",dialog -> {
                    //confirm
                })
                .setCancelButton("cancel",dialog -> {
                    //cancel
                })
                .show();
```

###Keyboard

**BankTemplate**  designs some easy-to-use keyboard for occasions such as entering amounts or passwords.

[BaseKeyboard](../base/src/main/java/acquire/base/widget/keyboard/BaseKeyboard.java) is the basic keyboard class. It designs the key listener logical and key code. When the key is pressed, you can receive the key event by key listener.  Its subclasses include: 

[NumberKeyboard](../base/src/main/java/acquire/base/widget/keyboard/NumberKeyboard.java): includes 0-9 , cancel, back and ok buttons. e.g.  amount entry.

[SimpleKeyboard](../base/src/main/java/acquire/base/widget/keyboard/SimpleKeyboard.java):  includes 0-9 and back buttons. e.g. admin password entry.

[HexNumberKeyboard](../base/src/main/java/acquire/base/widget/keyboard/HexNumberKeyboard.java): includes 0-9, A-F, cancel, back and ok buttons.  e.g. cipher key entry。

[NumberPointKeyboard](../base/src/main/java/acquire/base/widget/keyboard/NumberPointKeyboard.java): includes 0-9, point, cancel, back and ok buttons. 

**For example**:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
	...
    <acquire.base.widget.keyboard.NumberKeyboard
        android:id="@+id/keyboard_number"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</LinearLayout>
```

There are 2 key listener for these keyboards.

[ViewKeyboardListener](../base/src/main/java/acquire/base/widget/keyboard/listener/ViewKeyboardListener.java):  you need to implement its abstract method to receive the key code.

[EditKeyboardListener](../base/src/main/java/acquire/base/widget/keyboard/listener/EditKeyboardListener.java) : used for EditText. Compared to `ViewKeyboardListener`，It only needs to be bound with EditText to pass the key value into the EditText.

**ViewKeyboardListener **

```java
binding.keyboardNumber.setKeyBoardListener(new ViewKeyboardListener(13) {
            @Override
            public void onClear() {
                binding.tvAmount.setText(DEFAULT_AMOUNT);
            }

            @Override
            public String getText() {
                return binding.tvAmount.getText().toString();
            }
            @Override
            public void setText(String text) {
                long amount = getAmount(text);
                String strAmount = FormatUtils.formatAmount(amount,DECIMAL);
                binding.tvAmount.setText(strAmount);
                LoggerUtils.d("amount: "+strAmount );
            }
            @Override
            public void onEnter(){
                //enter amount
                LoggerUtils.d("enter amount: "+binding.tvAmount.getText().toString() );
                long amount = getAmount(binding.tvAmount.getText().toString());
                if (amount == 0){
                    return;
                }
                mCallback.onSuccess(amount);
            }
 });
```

**EditKeyboardListener**

```java
EditKeyboardListener expdateKeyboardListener = new EditKeyboardListener(binding.etExpdate) {
            @Override
            public void onEnter() {
                
            }
        };
```

#### Physical Keyboard

There are physical buttons on some device(e.g. P300), so **BankTemplate**  also handles this issue.

For the edit text, based on Android native functionality, the physical button value will be automatically passed in to the edit text, and the code does not need to be modified.

If there is no edit text and you want to listen to physical button value, you can use [PhysicalKeyboardUtils](../core/src/main/java/acquire/core/tools/PhysicalKeyboardUtils.java)

**For example**

**Register it**

```java
if (BDevice.supportPhysicalKeyboard()){
     PhysicalKeyboardUtils.setKeyboardListener(binding.tvAmount,binding.keyboardNumber.getKeyBoardListener());
}
```

**note: you need to release it when it is unused.**

```java
 if (BDevice.supportPhysicalKeyboard()){
       PhysicalKeyboardUtils.removeKeyboardListener(binding.tvAmount);
  }
```



### Recycler Adapter

[BaseBindingRecyclerAdapter](../base/src/main/java/acquire/base/widget/BaseBindingRecyclerAdapter.java) simplifies  the use of RecyclerView Adapter.

`BaseBindingRecyclerAdapter`encapsulates the functions of RecyclerView Adapter and ViewBinding, allowing developers to avoid writing tedious layout loading code and only implement the process of setting data to View.

**For example**

```java
//BaseMenuDialogItemBinding is the layout view binding
public class MenuDialogAdapter extends BaseBindingRecyclerAdapter<BaseMenuDialogItemBinding> {
    ...
    @Override
    public int getItemCount() {
        return mItems.size();
    }
    @Override
    protected void bindItemData(BaseMenuDialogItemBinding itemBinding, int position) {
        //set data to views
        String item = mItems.get(position);
        if (item != null) {
            itemBinding.radio.setText(item);
            itemBinding.radio.setChecked(selected == position);
            ...
        }
    }
}
```



### Toolbar

[PrimaryToolbar](../base/src/main/java/acquire/base/widget/PrimaryToolbar.java) is the common toolbar.

It includes the title, back button, right image and text button. And it fits the immersed status bar automatically. Its background color follows the theme color.

Its back button  is displayed by default, and its click event will simulate the system's back key event. Also , it can be set by `app:backVisibile`.

Its title is the activity title by default. It can be set by`app:title`. or invoke method `setTitle`.

Its right button is hidden by default. It can be set by  `app:rightIcon` and `app:rightContent` .

**For example**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center_horizontal"
    android:orientation="vertical">

    <acquire.base.widget.PrimaryToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"                              
        app:rightIcon="@drawable/settings_merchant_ic_modify"/>
</LinearLayout>
```



## Main Menu

[MainMenu](../app/src/main/java/acquire/app/fragment/main/menu/MainMenu.java) is the menu of the main screen. It includes the all transaction entry.  

**Its items core codes**:

```java
public class MainMenu {
  	...
    private List<MenuItem> getItems() {
        List<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem(TransType.TRANS_SALE, R.drawable.app_menu_sale, R.color.app_menu_light_pink_background));
        items.add(new MenuItem(TransType.TRANS_PRE_AUTH, R.drawable.app_menu_pre_auth, R.color.app_menu_light_blue_background));
         if (BDevice.supportPrint() || ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
            //child menu
            List<MenuItem> folder = new ArrayList<>();
            folder.add(new MenuItem(TransType.TRANS_REPRINT_LAST_RECEIPT, R.drawable.app_menu_reprint_last_receipt, R.color.app_menu_light_pink_background));
            ...
            items.add(new MenuItem(folder, R.string.app_menu_print_folder_title, R.drawable.app_menu_print_folder, R.color.app_menu_light_purple_background));
        }
        ...
        return items;
    }
   ...
   public boolean isChanged() {
        for (Map.Entry<String, Boolean> entry : mLastParamsMap.entrySet()) {
            String key = entry.getKey();
            Boolean lastParamValue = mLastParamsMap.get(key);
            if (lastParamValue == null || lastParamValue != ParamsUtils.getBoolean(key, true)) {
                //Trans support status was changed
                return true;
            }
        }
        return false;
    }
    private void checkSupport(List<MenuItem> items) {
        if (items == null || items.size() == 0) {
            return;
        }
        Iterator<MenuItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            MenuItem item = iterator.next();
            List<MenuItem> subItems = item.getSubItems();
            if (subItems != null) {
                checkSupport(subItems);
                if (subItems.isEmpty()) {
                    iterator.remove();
                }
                continue;
            }
            //Check whether to support this item.
            //Save item ParamsConst key.
            String paramsKey = TransUtils.getParamsKey(item.getTransType());
            boolean support = ParamsUtils.getBoolean(paramsKey, true);
            mLastParamsMap.put(paramsKey, support);
            if (!support) {
                iterator.remove();
            }
        }
    }
}

```

When the transaction paramskey (`TransUtils.getParamsKey` ) value is changed, the  `MainMenu` will update. 

On [SystemSupportFragment](../settings/src/main/java/acquire/settings/fragment/system/SystemSupportFragment.java), the transaction switch can be changed.

On method `getItems`, the child folder can be configured.



## Settings Item

All sub class of [BaseSettingFragment](../settings/src/main/java/acquire/settings/BaseSettingFragment.java) can use [IItemView](../settings/src/main/java/acquire/settings/widgets/IItemView.java) to add item quickly.

`IItemView`'s implementation class include:

 [TextItem](../settings/src/main/java/acquire/settings/widgets/item/TextItem.java):  It will shows the title and content. And it can be set click listener.

 [AmountItem](../settings/src/main/java/acquire/settings/widgets/item/AmountItem.java):  If it be clicked, it will shows a amount edit dialog to input amount.

 [EditTextItem](../settings/src/main/java/acquire/settings/widgets/item/EditTextItem.java):  If it be clicked, it will shows a edit dialog to input text.

 [MenuDialogItem](../settings/src/main/java/acquire/settings/widgets/item/MenuDialogItem.java):  If it be clicked, it will shows a menu dialog to select

 [SwitchItem](../settings/src/main/java/acquire/settings/widgets/item/SwitchItem.java): it will shows a switch button with title and content.

**For example**

```java
//TextItem
items.add(new TextItem.Builder(mActivity)
                .setTitle(getString(R.string.settings_system_item_support_transaction))
                .setOnClickListener(v -> mSupportDelegate.switchContent(new SystemSupportFragment()))
                .create());
//EditTextItem
 items.add(new EditTextItem.Builder(mActivity)
                            .setTitle(R.string.settings_scanner_item_usb_delay)
                            .setParamKey(ParamsConst.PARAMS_KEY_SCAN_EXTERN_USB_WAIT_TIME)
                            .setInputMaxLen(8)
                            .setDigits(Characters.NUMBER)
                            .create());
//switch item
   items.add(new SwitchItem.Builder(mActivity)
                .setParamKey(ParamsConst.PARAMS_KEY_TRANS_SALE)
                .setTitle(R.string.settings_system_support_item_sale)
                .create());
//menu item
  List<MenuBean> menu = new ArrayList<>();
        menu.add(new MenuBean(getString(R.string.settings_scanner_back_camera), Scanner.BACK_CAMERA));
        menu.add(new MenuBean(getString(R.string.settings_scanner_front_camera), Scanner.FRONT_CAMERA));
        menu.add(new MenuBean(getString(R.string.settings_scanner_external), Scanner.EXTERNAL));
        items.add(new MenuDialogItem.Builder(mActivity)
                .setTitle(R.string.settings_scanner_item_priority)
                .setParamKey(ParamsConst.PARAMS_KEY_SCAN_PRIORITY_SCANNER)
                .setParamBean(menu)
                .create());
//AmountItem
items.add(new AmountItem.Builder(mActivity)
                .setParamKey(ParamsConst.PARAMS_KEY_BASE_MAX_REFUND_AMOUNT)
                .setTitle(R.string.settings_system_item_refund_max_money)
                .setMaxValue(9999999999.99d)
                .setMinValue(1.00d)
                .create());
```

