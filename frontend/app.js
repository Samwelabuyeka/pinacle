const outputEl = document.getElementById('output');
const suggestionsEl = document.getElementById('suggestions');
const capabilitiesEl = document.getElementById('capabilities');
const apiBaseEl = document.getElementById('apiBase');
const apiKeyEl = document.getElementById('apiKey');
const promptEl = document.getElementById('prompt');

const permKeys = [
  'ai_chat', 'send_sms', 'make_calls', 'manage_calendar', 'location_access',
  'run_when_phone_off', 'device_search', 'always_mic', 'always_speaker', 'os_level_control'
];

const headers = () => ({
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${apiKeyEl.value.trim()}`,
});
const api = (path) => `${apiBaseEl.value.trim()}${path}`;

function readPermsFromUi() {
  const perms = {};
  for (const key of permKeys) perms[key] = document.getElementById(`perm_${key}`).checked;
  return perms;
}
function writePermsToUi(perms) {
  for (const key of permKeys) {
    const el = document.getElementById(`perm_${key}`);
    if (el) el.checked = !!perms[key];
  }
}

async function loadPerms() {
  const r = await fetch(api('/permissions'), { headers: headers() });
  const data = await r.json();
  writePermsToUi(data.permissions || {});
  outputEl.textContent = 'Permissions loaded.';
}

async function savePerms() {
  const r = await fetch(api('/permissions'), {
    method: 'POST', headers: headers(), body: JSON.stringify(readPermsFromUi()),
  });
  const data = await r.json();
  writePermsToUi(data.permissions || {});
  outputEl.textContent = 'Permissions saved.';
}


async function getCapabilities() {
  const r = await fetch(api('/capabilities'), { headers: headers() });
  const data = await r.json();
  capabilitiesEl.textContent = JSON.stringify(data.capabilities || data, null, 2);
}

async function setReminder() {
  const title = document.getElementById('reminderTitle').value.trim() || 'Reminder';
  const at = document.getElementById('reminderAt').value.trim() || 'unspecified';
  const r = await fetch(api('/reminders'), {
    method: 'POST', headers: headers(), body: JSON.stringify({ title, at }),
  });
  const data = await r.json();
  outputEl.textContent = JSON.stringify(data, null, 2);
}

async function getSuggestions() {
  const r = await fetch(api('/suggestions'), { headers: headers() });
  const data = await r.json();
  suggestionsEl.textContent = JSON.stringify(data.suggestions || data, null, 2);
}


async function runOsAction() {
  const action = document.getElementById('osAction').value.trim();
  const r = await fetch(api('/os_action'), {
    method: 'POST', headers: headers(), body: JSON.stringify({ action }),
  });
  const data = await r.json();
  outputEl.textContent = JSON.stringify(data, null, 2);
}

async function searchDevice() {
  const base = document.getElementById('searchBase').value.replace('$HOME', '~');
  const query = document.getElementById('searchQuery').value;
  const r = await fetch(api('/device_search'), {
    method: 'POST', headers: headers(), body: JSON.stringify({ base_path: base, query }),
  });
  const data = await r.json();
  outputEl.textContent = JSON.stringify(data, null, 2);
}

async function askAssistant() {
  const prompt = promptEl.value.trim();
  if (!prompt) return;
  outputEl.textContent = 'Thinking...';
  await fetch(api('/events'), { method: 'POST', headers: headers(), body: JSON.stringify({ event: 'chat_prompt' }) });
  const resp = await fetch(api('/generate'), {
    method: 'POST', headers: headers(), body: JSON.stringify({ prompt, n_predict: 64 }),
  });
  const data = await resp.json();
  const text = data.output || data.error || JSON.stringify(data);
  outputEl.textContent = text;
  if (data.suggestions) suggestionsEl.textContent = JSON.stringify(data.suggestions, null, 2);
  if ('speechSynthesis' in window) {
    speechSynthesis.cancel();
    speechSynthesis.speak(new SpeechSynthesisUtterance(text));
  }
}

document.getElementById('loadPermsBtn').addEventListener('click', loadPerms);
document.getElementById('savePermsBtn').addEventListener('click', savePerms);
document.getElementById('sendBtn').addEventListener('click', askAssistant);
document.getElementById('searchBtn').addEventListener('click', searchDevice);
document.getElementById('suggestionsBtn').addEventListener('click', getSuggestions);
document.getElementById('capabilitiesBtn').addEventListener('click', getCapabilities);
document.getElementById('setReminderBtn').addEventListener('click', setReminder);
document.getElementById('runOsActionBtn').addEventListener('click', runOsAction);

const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
const speakBtn = document.getElementById('speakBtn');
if (!SpeechRecognition) {
  speakBtn.disabled = true;
  speakBtn.textContent = 'Speech unavailable';
} else {
  const rec = new SpeechRecognition();
  rec.lang = 'en-US'; rec.interimResults = false; rec.maxAlternatives = 1;
  speakBtn.addEventListener('click', () => rec.start());
  rec.onresult = (event) => { promptEl.value = event.results[0][0].transcript; askAssistant(); };
}
