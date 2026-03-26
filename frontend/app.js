const promptEl = document.getElementById('prompt');
const outputEl = document.getElementById('output');
const apiUrlEl = document.getElementById('apiUrl');
const apiKeyEl = document.getElementById('apiKey');

async function askAssistant() {
  const prompt = promptEl.value.trim();
  if (!prompt) return;
  outputEl.textContent = 'Thinking...';

  try {
    const resp = await fetch(apiUrlEl.value.trim(), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKeyEl.value.trim()}`,
      },
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

document.getElementById('sendBtn').addEventListener('click', askAssistant);

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
