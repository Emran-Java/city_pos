# NSDK

NSDK is the library for using hardware features. It includes built-in SDK and external SDK.

## Library

built-in nsdk: Newland-NSDK-xxx.aar

external nsdk NSDK-Plugin-Ext-xxx.aar

## Wrapper Class

### Built-In NSDK

[ServiceHelper](../sdk_helper/src/main/java/acquire/sdk/ServiceHelper.java) initialize the NSDK Manager. Then other sub modules obtain interfaces through it.

[BDevice](../sdk_helper/src/main/java/acquire/sdk/device/BDevice.java) 

you can get the device information, such as model name, SDK version and supported module. You can know the device status by `BDevice`.

[BLed](../sdk_helper/src/main/java/acquire/sdk/led/BLed.java)

opens or closes the led light. POS has four led light, they will open during EMV processing.

[BCardReader](../sdk_helper/src/main/java/acquire/sdk/nonbankcard/BCardReader.java)

Reads mag card or identifies the current card reading method used.

[ContactReader](../sdk_helper/src/main/java/acquire/sdk/nonbankcard/ContactReader.java)

Reads contact Cards.

[BasicContactlessReader](../sdk_helper/src/main/java/acquire/sdk/nonbankcard/BasicContactlessReader.java)

A  contactless card basic class.  It has 2 subclass, [NtagReader](../sdk_helper/src/main/java/acquire/sdk/nonbankcard/contactless/NtagReader.java) for Ntag cards and  [MifareClassicReader](../sdk_helper/src/main/java/acquire/sdk/nonbankcard/contactless/MifareClassicReader.java) for mifare classic  cards.

[BPinpad](../sdk_helper/src/main/java/acquire/sdk/pin/BPinpad.java)

input PIN or encrypt data.  It uses the device's security chip encryption to ensure that data is not leaked. When PIN is input, the screen cannot be clicked except for the PIN keyboard area.

[BPrinter](../sdk_helper/src/main/java/acquire/sdk/printer/BPrinter.java)   

Prints data.

[BScanner](../sdk_helper/src/main/java/acquire/sdk/scan/BScanner.java)  

Quickly identify QR or barcode pictures through Newland system scanner library.  You can start the camera to get a picture, then use `BScanner` to get  the QR code.

[BHardScanner](../sdk_helper/src/main/java/acquire/sdk/scan/BHardScanner.java)

Starts the hard scanner to scan the QR/barcode. When it starts, you don't need to start the camera, it will start the hard scanner. It's faster than `BScanner`.

[BSerialPort](../sdk_helper/src/main/java/acquire/sdk/serial/BSerialPort.java)

Communicates the other device by RS232 serial port.

[BMicroUsbPort](../sdk_helper/src/main/java/acquire/sdk/serial/BMicroUsbPort.java)

Communicates the other device by micro USB or type-c.

[BBeeper](../sdk_helper/src/main/java/acquire/sdk/sound/BBeeper.java)

Uses the built-in buzzer to make a sound.

[BRoute](../sdk_helper/src/main/java/acquire/sdk/route/BRoute.java)

Sets the data sent to some IP to only use Mobile or WIFI.

[BSystem](../sdk_helper/src/main/java/acquire/sdk/system/BSystem.java)

Sets the system properties, such as disable Home button and change the volume.



### External NSDK

[ExtServiceHelper](../sdk_helper/src/main/java/acquire/sdk/ExtServiceHelper.java) initialize the external NSDK Manager.  It can be used to connect to an external PIN Pad.

[BExtDevice](../sdk_helper/src/main/java/acquire/sdk/device/BExtDevice.java) 

You can get the external PIN Pad information, such as SN and PIN Pad Version. 

[BExternScanner](../sdk_helper/src/main/java/acquire/sdk/scan/BExternScanner.java)  

Opens a external scanner to scan QR/barcode. It establishes communication with external devices through serial port or USB.

[BExternalPinpad](../sdk_helper/src/main/java/acquire/sdk/pin/BExternalPinpad.java)

input PIN or encrypt data by the external PIN pad.