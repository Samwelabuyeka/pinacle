const outputEl = document.getElementById('output');
const apiBaseEl = document.getElementById('apiBase');
const apiKeyEl = document.getElementById('apiKey');
const promptEl = document.getElementById('prompt');

const permKeys = [
  'ai_chat', 'send_sms', 'make_calls', 'manage_calendar', 'location_access', 'run_when_phone_off'
];

function headers() {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${apiKeyEl.value.trim()}`,
  };
}

function api(path) {
  return `${apiBaseEl.value.trim()}${path}`;
}

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
    method: 'POST',
    headers: headers(),
    body: JSON.stringify(readPermsFromUi()),
  });
  const data = await r.json();
  writePermsToUi(data.permissions || {});
  outputEl.textContent = 'Permissions saved.';
}

async function askAssistant() {
  const prompt = promptEl.value.trim();
  if (!prompt) return;
  outputEl.textContent = 'Thinking...';
  try {
    const resp = await fetch(api('/generate'), {
      method: 'POST',
      headers: headers(),
      body: JSON.stringify({ prompt, n_predict: 64 }),
    });
    const data = await resp.json();
    const text = data.output || data.error || JSON.stringify(data);
    outputEl.textContent = text;
    if ('speechSynthesis' in window) {
      speechSynthesis.cancel();
      speechSynthesis.speak(new SpeechSynthesisUtterance(text));
    }
  } catch (err) {
    outputEl.textContent = `Request failed: ${err.message}`;
  }
}

async function queueTask() {
  const title = document.getElementById('taskTitle').value.trim() || 'Untitled task';
  const details = document.getElementById('taskDetails').value.trim();
  const resp = await fetch(api('/tasks'), {
    method: 'POST',
    headers: headers(),
    body: JSON.stringify({ title, details }),
  });
  const data = await resp.json();
  outputEl.textContent = JSON.stringify(data, null, 2);
}

document.getElementById('loadPermsBtn').addEventListener('click', loadPerms);
document.getElementById('savePermsBtn').addEventListener('click', savePerms);
document.getElementById('sendBtn').addEventListener('click', askAssistant);
document.getElementById('queueTaskBtn').addEventListener('click', queueTask);

const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
const speakBtn = document.getElementById('speakBtn');
if (!SpeechRecognition) {
  speakBtn.disabled = true;
  speakBtn.textContent = 'Speech unavailable';
} else {
  const rec = new SpeechRecognition();
  rec.lang = 'en-US';
  rec.interimResults = false;
  rec.maxAlternatives = 1;
  speakBtn.addEventListener('click', () => rec.start());
  rec.onresult = (event) => {
    promptEl.value = event.results[0][0].transcript;
    askAssistant();
  };
}
