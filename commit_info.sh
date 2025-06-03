#!/usr/bin/env bash
# openai gpt4.1-mini prompt
# you are a senior developer write a git commit informaiton on this output git commit description in the style of conventional commits for github in English
OUT=changes_for_llm.txt

# Проверим, существует ли ревизия HEAD
if git rev-parse --verify HEAD >/dev/null 2>&1; then
  # HEAD есть, можно делать diff относительно него
  echo "=== Changed files (relative to HEAD) ===" > $OUT
  git diff --name-status HEAD              >> $OUT
  echo -e "\n=== Staged diff ==="           >> $OUT
  git diff --cached                        >> $OUT
  echo -e "\n=== Unstaged diff ==="         >> $OUT
  git diff                                 >> $OUT
else
  # HEAD нет (первый коммит), считаем все файлы как новые (status A)
  echo "=== New repository: listing all tracked files as 'A' ===" > $OUT
  git ls-files | sed 's/^/A\t/'            >> $OUT
  echo -e "\n=== Staged diff ==="           >> $OUT
  git diff --cached                        >> $OUT
  echo -e "\n=== Unstaged diff ==="         >> $OUT
  git diff                                 >> $OUT
fi

echo "Saved all changes to $OUT"
