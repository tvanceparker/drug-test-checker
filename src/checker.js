import * as SecureStore from 'expo-secure-store';
import * as FileSystem from 'expo-file-system';

const LOG_FILE = FileSystem.documentDirectory + 'dtc_logs.txt';

async function appendLog(line) {
  const ts = new Date().toISOString();
  const entry = `${ts} - ${line}\n`;
  try {
    await FileSystem.appendAsStringAsync(LOG_FILE, entry, { encoding: FileSystem.EncodingType.UTF8 });
  } catch (e) {
    // create file
    await FileSystem.writeAsStringAsync(LOG_FILE, entry, { encoding: FileSystem.EncodingType.UTF8 });
  }
}

export async function getLogs() {
  try {
    const exists = await FileSystem.getInfoAsync(LOG_FILE);
    if (!exists.exists) return [];
    const txt = await FileSystem.readAsStringAsync(LOG_FILE, { encoding: FileSystem.EncodingType.UTF8 });
    return txt.trim().split('\n').reverse();
  } catch (e) {
    return [];
  }
}

export async function runCheckNow() {
  try {
    const pin = await SecureStore.getItemAsync('pin');
    const last4 = await SecureStore.getItemAsync('last4');
    if (!pin || !last4) {
      const m = 'Missing credentials';
      await appendLog(m);
      return m;
    }

    // Perform POST as the site expects a form submit
    const form = new URLSearchParams();
    form.append('callInCode', pin);
    form.append('lastName', last4);

    const resp = await fetch('https://drugtestcheck.com', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: form.toString(),
    });

    const html = await resp.text();

    // simple regex to find the label reply
    const m = html.match(/<label[^>]*>\s*(You are(?: not)? required to test today[\s\S]*?)<\/label>/i);
    let result = 'Unable to parse response';
    if (m) result = m[1].replace(/<.*?>/g, '').trim();

    await appendLog(`Request pin=${pin} last4=${last4} => ${result}`);
    return result;
  } catch (e) {
    await appendLog(`Error: ${String(e)}`);
    return `Error: ${String(e)}`;
  }
}
