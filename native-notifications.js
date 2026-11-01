import { LocalNotifications } from '@capacitor/local-notifications';
import { registerPlugin } from '@capacitor/core';
window.VaultLocalNotifications = LocalNotifications;
window.VaultRingtonePicker = registerPlugin('RingtonePicker');
