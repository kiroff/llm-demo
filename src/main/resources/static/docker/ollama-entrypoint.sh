#!/bin/bash
# Стартира сървъра на заден план
ollama serve &
OLLAMA_PID=$!

# Изчаква API-то — ollama list връща 0 само когато сървърът е готов
echo "Waiting for Ollama to start..."
until ollama list &>/dev/null; do
  sleep 1
done
echo "Ollama is ready."

# Изтегля моделите от OLLAMA_MODELS (разделени със запетая)
IFS=',' read -ra MODELS <<< "${OLLAMA_MODELS:-llama3.2}"
for model in "${MODELS[@]}"; do
  echo "Pulling: $model"
  ollama pull "$model"
done

echo "All models ready."
wait $OLLAMA_PID