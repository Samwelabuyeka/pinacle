# Maya Phone Strategy

## Model plan

Maya should not rely on a single giant local model for every task on a phone.

- `phone model`: a strong compact model that can actually run on-device with acceptable latency
- `heavy model`: a larger local model for escalation when the hardware can handle it
- `routing`: keep fast day-to-day assistant work on the phone model and escalate only for harder reasoning

Current intended profiles:

- `phone model`: `Qwen3-4B-Instruct` through `MLC LLM Android`
- `heavy model`: `Mixtral-8x7B-Instruct-GGUF` where hardware allows it
- `fallback heavy local`: `BitNet-b1.58-2B-4T` as a local CPU path already present in this workspace

## Siri-on-Android target

The target capability set includes:

- wake word
- speech in and speech out
- reminders and alarms
- calling and messaging flows
- calendar awareness
- notification reading and summarization
- app launch and device search
- automation and smart-home routing
- context memory and proactive suggestions

## Android reality

Maya can approach Siri-style behavior on Android, but the deepest controls depend on:

- assistant role
- accessibility service
- notification listener service
- dialer or SMS role where needed
- device-owner mode for some enterprise-level controls
- OEM hooks such as HyperOS bridges

Without those privileges, Maya can still be a strong offline assistant, but not every system action can be guaranteed on every phone.
