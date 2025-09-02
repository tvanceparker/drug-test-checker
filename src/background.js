import * as TaskManager from 'expo-task-manager';
import * as BackgroundFetch from 'expo-background-fetch';
import { runCheckNow } from './checker';
import * as Notifications from 'expo-notifications';

const TASK_NAME = 'DRUG_TEST_CHECK_TASK';

TaskManager.defineTask(TASK_NAME, async () => {
  try {
    const res = await runCheckNow();
    // if res indicates required, send notification
    if (res && res.includes('required')) {
      await Notifications.scheduleNotificationAsync({
        content: { title: 'Drug Test', body: res },
        trigger: null,
      });
    }
    return BackgroundFetch.BackgroundFetchResult.NewData;
  } catch (e) {
    return BackgroundFetch.BackgroundFetchResult.Failed;
  }
});

export async function registerTask() {
  try {
    await BackgroundFetch.registerTaskAsync(TASK_NAME, {
      minimumInterval: 60 * 60 * 24, // once a day
      stopOnTerminate: false,
      startOnBoot: true,
    });
  } catch (e) {
    console.log('registerTask error', e);
  }
}
