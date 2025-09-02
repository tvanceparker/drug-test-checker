import React, { useEffect, useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, FlatList } from 'react-native';
import * as SecureStore from 'expo-secure-store';
import { registerTask } from './src/background';
import { runCheckNow, getLogs } from './src/checker';

export default function App() {
  const [pin, setPin] = useState('');
  const [last4, setLast4] = useState('');
  const [email, setEmail] = useState('to_be_coded@example.com');
  const [logs, setLogs] = useState([]);

  useEffect(() => {
    registerTask();
    loadLogs();
  }, []);

  async function saveCredentials() {
    await SecureStore.setItemAsync('pin', pin);
    await SecureStore.setItemAsync('last4', last4);
  await SecureStore.setItemAsync('email', email);
    alert('Saved');
  }

  async function loadLogs() {
    const l = await getLogs();
    setLogs(l);
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Drug Test Checker</Text>

  <Text style={styles.label}>6-digit PIN</Text>
      <TextInput style={styles.input} value={pin} onChangeText={setPin} keyboardType="number-pad" maxLength={6} />

  <Text style={styles.label}>First 4 of Last Name</Text>
  <TextInput style={styles.input} value={last4} onChangeText={setLast4} maxLength={4} />

  <Text style={styles.label}>Email (placeholder)</Text>
  <TextInput style={styles.input} value={email} onChangeText={setEmail} keyboardType="email-address" />

      <View style={styles.actions}>
        <Button title="Save Credentials" onPress={saveCredentials} />
        <View style={{width:12}} />
        <Button title="Run Now" onPress={async () => { const r = await runCheckNow(); alert(r); loadLogs(); }} />
      </View>

      <Text style={{marginTop:20, fontWeight:'bold'}}>Logs</Text>
      <FlatList data={logs} keyExtractor={(i,idx)=>String(idx)} renderItem={({item})=> (
        <View style={styles.logItem}><Text>{item}</Text></View>
      )} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16, paddingTop: 48, backgroundColor: '#fafafa' },
  title: { fontSize: 26, marginBottom: 16, fontWeight: '700' },
  label: { marginTop: 8, marginBottom: 6, color: '#444' },
  input: { borderWidth: 1, borderColor: '#ddd', padding: 10, borderRadius: 8, backgroundColor: '#fff' , marginBottom: 12},
  actions: { flexDirection: 'row', marginTop: 6, marginBottom: 12 },
  logItem: { padding: 8, borderBottomWidth: 1, borderColor: '#eee' }
});
