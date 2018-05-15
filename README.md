
# Android-BLE-Clock
Set BLE device date-time by GATT

to use with "power watch2" ble wrist strap.

Service: 0000FFF0-0000-1000-8000-00805F9B34FB

Write: 	UUID 0000FFF2- 

Notify:	UUID 0000FFF1- 

Write Value:

CMD 1 byte

LEN 1 byte (x=7, year-2000, month, date, hour, minute, second, weekday)

DAT x byte

CRC 1 byte (xor DAT bytes)

Example: C207A6040F0D181002AA

Programmed in Android Studio.
