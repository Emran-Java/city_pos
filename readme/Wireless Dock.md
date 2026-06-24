# Wireless Dock

Wireless Dock (DH10) is a device provided by Newland that can be used to expand device ports.

It has 2 USB, 1 RS232 Serial Port, 1 cash drawer and 1 type-c.

## Library

Newland-NSDK-Dock-xxx.aar

## External App

Dock Service



## USB

[DockUsbPort](../sdk_helper/src/main/java/acquire/sdk/dock/DockUsbPort.java) 

```java
public class DockUsbPort extends BaseDock{
    public static int getUsbCount()
    public boolean open() 
    public void close()
    public byte[] read(int lengthMax, int timeoutMillis)
    public boolean write(@NonNull byte[] data)
    public void flush()
    public int getCacheLength()
    public boolean isConnected()
    public String getUsbFactoryId()
}
```

You can use `DockUsbPort` open the dock USB and write or read data.

e.g. write data to USB.

```java
//create USB1 port.
DockUsbPort dockUsbPort1 = new DockUsbPort(1);
//open usb
if (!dockUsbPort1.open()) {
     return false;
}
//write data to usb
boolean result = dockUsbPort1.write(esc); 
//close usb
dockUsbPort1.close();
if (!result) {
     return false;
}
return true;
```



## RS232 Serial Port/Type-C

[DockSerialPort](../sdk_helper/src/main/java/acquire/sdk/dock/DockSerialPort.java) 

```java
public class DockSerialPort extends BaseDock{
    public boolean open(final int baudRate)
    public boolean openTypeC()
    public byte[] read(int lengthMax, int timeoutMillis) 
    public boolean write(byte[] data)
    public boolean close()
    public void flush() 
    public int getCacheLength(
    public boolean isConnected()
}
```

`DockSerialPort` controls the RS232 serial port and type-c.

e.g. write data to RS232 serial port or type-c.

```java
DockSerialPort dockSerialPort = new DockSerialPort();
boolean result = dockSerialPort.open(baudRate); // type-c boolean result = dockSerialPort.openTypeC()
if (!result) {
    return false;
}
result = dockSerialPort.write(esc);
dockSerialPort.close();
if (!result) {
    return false;
}
returnt true;
```

## 

##Cash Drawer

[DockCashDrawer](../sdk_helper/src/main/java/acquire/sdk/dock/DockCashDrawer.java) 

```java
public class DockCashDrawer extends BaseDock{
    public void open()
    public void open(int voltage, int delayMillis)
}
```

`DockCashDrawer`can open the cash drawer connected Dock.

e.g. open a cash darwer.

```java
DockCashDrawer drawer = new DockCashDrawer();
drawer.open();
```

