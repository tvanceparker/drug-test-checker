import { AppRegistry } from 'react-native';
import { runCheckNow } from './src/checker';
import * as Notifications from 'expo-notifications';

AppRegistry.registerHeadlessTask('DrugTestHeadless', async () => {
  return async () => {
    try {
      const res = await runCheckNow();
      if (res && res.toLowerCase().includes('required')) {
        await Notifications.scheduleNotificationAsync({ content: { title: 'Drug Test', body: res }, trigger: null });
      }
    } catch (e) {
      // log handled inside runCheckNow
    }
  };
});
