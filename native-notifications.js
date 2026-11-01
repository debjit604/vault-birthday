import { Capacitor, registerPlugin } from '@capacitor/core';
import { LocalNotifications } from '@capacitor/local-notifications';
window.VaultLocalNotifications = LocalNotifications;
window.VaultRingtonePicker = registerPlugin('RingtonePicker');
window.VaultIsNative = Capacitor.isNativePlatform();
