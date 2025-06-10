#!/usr/bin/env bash
# Создаём виртуальное окружение в папке .venv
python3 -m venv .venv

# Активируем
source .venv/bin/activate

# Обновляем pip и устанавливаем зависимости
pip install --upgrade pip
pip install -r requirements.txt

echo "Virtual environment ready! Для активации: source .venv/bin/activate"
