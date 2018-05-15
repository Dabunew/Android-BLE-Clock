
# Android-BLE-Clock
Set BLE device date-time by GATT

to use with "power watch2" ble wrist strap.

Service: 0000FFF0-0000-1000-8000-00805F9B34FB</br>
Write Characteristic: 	UUID 0000FFF2-
  
Write Value: </br>
CMD 1 byte </br>
LEN 1 byte (x=7, year-2000, month, date, hour, minute, second, weekday)</br>
DAT x byte </br>
CRC 1 byte (xor DAT bytes)

Example: C207A6040F0D181002AA

Programmed in Android Studio.
