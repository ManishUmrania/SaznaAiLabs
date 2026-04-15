@echo off
cd /d C:\Work\sazna-platform
set ANTHROPIC_BASE_URL="http://localhost:11434"
set ANTHROPIC_AUTH_TOKEN="token"
ollama launch claude --model  qwen2.5-coder:7b
:: ollama run qwen2.5-coder:7b