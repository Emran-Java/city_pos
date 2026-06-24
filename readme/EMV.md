# EMV

EMV library can execute EMV card reading process. it is based on [NSDK](./NSDK.md), so you need to confirm whether `NSDK` is imported into the project. It includes built-in EMV and external EMV.

## Library

built-in EMV:  Newland-EMVL3-xxx.aar

external EMV:  Newland-EMVL3-Plugin-Ext-xxx.jar

## Wrapper Class

### Built-In EMV

[BEmvProcessor](../sdk_helper/src/main/java/acquire/sdk/emv/BEmvProcessor.java) executes  the EMV process.

[BEmvParamLoader](../sdk_helper/src/main/java/acquire/sdk/emv/BEmvParamLoader.java) downloads EMV aid and capk.

### External EMV

[BExtEmvProcessor](../sdk_helper/src/main/java/acquire/sdk/emv/BExtEmvProcessor.java) executes  the EMV process.

[BExtEmvProcessor](../sdk_helper/src/main/java/acquire/sdk/emv/BExtEmvProcessor.java) downloads EMV aid and capk.

## Initialization

[SelfCheckHelper](../core/src/main/java/acquire/core/tools/SelfCheckHelper.java) completes the EMV initialization process for both Built-In and External EMV.

```java
public class SelfCheckHelper {
    public static void initDevice(Context context) {
        ...
        boolean isConnectNsdk;
        //init NSDK
        isConnectNsdk = ServiceHelper.getInstance().init(context);
        if (!isConnectNsdk) {
            ToastUtils.showToast(R.string.core_sdk_init_failed);
            return;
        }
        //check external flag
        boolean external = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD);
        if (!external && (!BDevice.isExistSecurityModule() || BDevice.isCpos())) {
            LoggerUtils.d("No Built-in Security Module!");
            ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD, true);
            external = true;
        }
        if (external) {
            //external PIN pad
            int connectMode = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD_CONNECT_MODE);
            isConnectNsdk = ExtServiceHelper.getInstance().init(context,connectMode);
            if (!isConnectNsdk) {
                ToastUtils.showToast(R.string.core_device_ext_pinpad_init_failed);
            }
        }
        if (isConnectNsdk){
            //EMV config
            if (!loadEmvConfig(context, external,false)){
                ToastUtils.showToast(R.string.core_device_load_emv_configurations);
            }
        }
        ...
    }

    public static boolean loadEmvConfig(Context context, boolean external,boolean forceLoad) {
        IEmvParamLoader loader;
        if (external) {
            loader = new BExtEmvParamLoader();
            if (!ExtServiceHelper.getInstance().isInit()) {
                return false;
            }

        } else {
            loader = new BEmvParamLoader();
            if (!ServiceHelper.getInstance().isInit()) {
                return false;
            }
        }
        boolean loadAidCapk = !ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_EMV_AID_CAPK)
                || forceLoad
                || loader.isCapkLoss()
                || loader.isCtAidLoss()
                || loader.isClessAidLoss();
        if (loadAidCapk) {
            LoggerUtils.d(String.format(Locale.getDefault(),"init %s emv ",external?"external":"built-in"));
            boolean loadSucc = EmvConfigXmlParser.parseXml(context, FileConst.EMV_CONFIG, loader);
            if (loadSucc) {
                ParamsUtils.setBoolean(ParamsConst.PARAMS_KEY_EMV_AID_CAPK, true);
            }
            return loadSucc;
        }
        return true;
    }
}

```

Firstly, it is necessary to ensure that the `NSDK` has been initialized. Built-In EMV needs the built-in `NSDK`, and external EMV needs the external `NSDK`.

Then,  you need to load EMV aid and capk into EMV. if the external PIN pad is used, the aid and capk need to be injected into the external PIN pad, else,  be injected into built-in POS.  For easier configuration of AID and CAPK, `BankTemplate` specifies an XML file [Newland_L3_configuration.xml](../core/src/main/assets/Newland_L3_configuration.xml) to manage these parameters. [EmvConfigXmlParser](../core/src/main/java/acquire/core/tools/EmvConfigXmlParser.java) will parse the xml and load them.

So that the EMV configuration is complete, we can use it in the transaction.



## EMV Helper

Class [EmvHelper](../core/src/main/java/acquire/core/tools/EmvHelper.java) encapsulates EMV common functions.

1. Manage the built-in and external EMV api.

2. start to read card and EMV process.

3. Fetch the EMV tag data and pack them.

4. Terminates EMV process

   etc.

## Read Card

On card reading fragment, you can invoke `EmvHelper` to read card.

```java
public void readCard(EmvLaunchParam launchParam, EmvListener emvListener)
```

The `EmvLaunchParam` has the transaction type,card entry type, timeout.

```java
EmvLaunchParam param = new EmvLaunchParam.Builder(emvTransType)
                    .entryMode(supportEntry)
                    .amount(pubBean.getAmount())
                    .timeout(60)
                    .create();
```

And you can listen EMV process by  `EmvListener`.

```java
public interface EmvListener {
    void onReady(EmvReadyBean emvReadyBean);
    void onReading();
    int onSelectAid(List<String> preferNames);
    boolean onInsertError();
    void onFinalSelect();
    boolean onSeePhone();
    boolean onCardNum(String pan);
    PinResult onInputPin(boolean online, int pinTryCount);
    void onResult(boolean success, int emvResult);
}
```

When the card is read, 

first,  `onReady` will be invoked, it will tell you the support card entry mode, you can update your UI by it.  

Then,`onReading` will tell you the card is being reading. 

If the card has multiple applications(e.g. VISA and Master pan are in a card),  `onSelectAid` will let you select a application. you should show the selection item for the card holder.

When selection is finished, `onFinalSelect` will be invoked. And now, you can get the real card entry mode.

If the card is inserted and it is damaged or operate  wrong(e.g. card reverse insertion), `onInsertError` will be invoked. If you return false , the EMV will end. If return true, the card will be read again.

If the card requires see phone, `onSeePhone` will be invoked. It will make the card be read again.

When the card number is fetched, `onCardNum` will be invoked,. Here, you can show the card number for card holder confirm.

And then `onInputPin` is invoked. If the argument `online` is true, it means the PIN is used for request to bank host; else, the PIN is verified in EMV process, you don't need to deal it. The argument `pinTryCount` is the times PIN can be input incorrectly. `PinResult` includes  the PIN block and result.

Finally,  `onResult` means the EMV is finish. If the transaction is Sale and the entry is  contact, you need to execute secondary authorization by `EmvHelper.secondGac`.  



## Example

[CardFragment](../core/src/main/java/acquire/core/fragment/card/CardFragment.java) is a fragment with card reading and EMV.

