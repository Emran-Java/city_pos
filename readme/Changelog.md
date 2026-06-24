# Changelog

## [R3.14] - 2025-9-16

### ✨ Added

- Adapted devices: **N560K **, **S30**;
- Added sensitive data processing logic.

### 🛠️ Fixed

- Updated NFC Tag Order URL to solve the problem of server switching and inability to access

### 🔧 Changed

- Updated **EMVL3** 4.4.10 to 4.4.15;
- Updated **NSDK** 2.13.0 to 2.15.0, using NSDK automatic paper feeding instead of upper layer processing method;
- Updated **Ext NSDK** 2.5.0 to 2.6.0;
- Updated **Wireless Dock** 1.1.3 to 1.1.10;


---



## [R3.13] - 2025-4-14

### 🛠️ Fixed

- Remove hipay-sdk-v1.0.01.aar to solve the issue of project build failure.

### 🔧 Changed

- Restore End Check Card,.

- When using an external keyboard, disable the manual card number input function on the password keyboard.

---

  

## [R3.12] - 2025-4-3

### ✨ Added
- Support device: **N960K**,**U200**, **NS960 Pro**;
- Added HCE Sale;
- Added  E-Receipt;
- Support girocard.
- Added accessibility PIN pad.
- Added **NSDK Card Emulation library** 1.1.2-alpha7.

### 🛠️ Fixed
- Resolve the issue of application crashes when the system is in Arabic.
- Updated platform.apk to enable it to keep alive in the background on Android 14 devices.

### 🔧 Changed

- Change the receipt printing on the transaction result interface to manually selecting the receipt printing method.

- Updated **EMVL3** 4.4.3 to 4.4.10;

- Updated **Ext EMVL3** 1.2.3-beta01 to 1.2.4;

- Updated **NSDK** 2.9.2 to 2.13.0;

- Updated **Ext NSDK** 2.4.1 to 2.5.0;

- Updated **NSDK RFIC** 1.3.0 to 1.5.0;

- Updated **TOMS API** 1.0.15 to 1.0.20.


---



## [R3.11] - 2024-8-4

### ✨ Added

- Support device: **N950S-C;**
- Support Cashback and CashAdvance transactions;
- Update TOMSClientApi aar V1.0.12 to V1.0.15 to adapt new TOMS Fly Receipt System;
- Added HiPay SDK.

### 🛠️ Fixed

- Updated NSDK to 2.9.2 to fix the Ethernet bug;

### 🔧 Changed

- Updated **NSDK** 2.8.1 to 2.9.2;

- Updated **NSDK RFIC** 1.1.0 to 1.3.0;

- Updated **TOMS API** 1.0.12 to 1.0.15


---



## [R3.10] - 2024-3-29

### ✨ Added

- Support device: **N950K**,**N750P**;
- Support Japan,Spanish language;
- Support P180(PIN pad );
- Pass  UL 5.7.0 and 5.6.1 cases;
- Support MIR bank card;

### 🔧 Changed

- Updated **EMVL3**  4.3.7 to 4.4.3;

- Updated **NSDK** 2.5.0 to 2.8.1;

- Updated **Ext NSDK** 1.2.3 to 2.4.1;

- Updated **NSDK RFIC** 1.1.0 to 1.3.0;

- Updated **TOMS API** 1.0.11 to 1.0.12.


---



## [R3.9] - 2023-10-12

### 🛠️ Fixed

- Fixed the link 'demo apk' error in Readme file.


---



## [R3.8] - 2023-9-22

### 🛠️ Fixed

- Fixed the bug where the ISO8583.getMacSrcData() error caused an error in not calculating field 63 to mac.


---



## [R3.7] - 2023-9-10

### ✨ Added

- Support device : **P300**;
- Support external Bluetooth and USB printer;
- Support external Bluetooth and USB scanner;
- Support FlyReceipt;
- Support connect external PIN pad. printer, scanner by DH10;
- Added **NSDK RFIC** 1.1.0;
- Added **NSDK dock** 1.1.3;

### 🔧 Changed

- Updated **EMVL3**  4.3.3 to 4.3.7;

- Updated **Ext EMVL3** 1.2.2 to 1.2.3-beta01;

- Updated **NSDK** 2.3.111113 to 2.5.0;

- Updated **NSDK RKL** 1.1.0-alpha2 to 1.1.0；

- Updated **TOMS API** 1.0.00 to 1.0.11;


---



## [R3.6] - 2022-10-8

### ✨ Added

- Initial release for BankTemplate；
- Support external PIN pad；
- Support **DH10**;
- Support TOMS FlyParameter function;
- Support device modes:
  - **N950**
  - **N700**
  - **N750**
  - **N850**
  - **N910；**
  - **N910Pro；**
  - **CPOS；**
  - **X800.**

