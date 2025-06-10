# Scripts for Generating Test Data (JSON)

This directory contains Python scripts for generating JSON files with test data, including text embeddings using the `sentence-transformers` model.

## 📁 Structure

```
scripts/
├── generate_test_data.py     # Main script for JSON generation
├── requirements.txt          # Python dependencies
└── setup_venv.sh             # Bash script to create a virtual environment
```

## 🚀 Quick Start

### 1. Install Python (if not already installed)

Make sure you have Python 3.8+ and `pip` installed.

### 2. Create and activate a virtual environment

```bash
cd scripts
./setup_venv.sh
source .venv/bin/activate
```

> After activating the environment, your command line will show the prefix `(.venv)`.

### 3. Run JSON generation

```bash
python generate_test_data.py
```

### 4. Move the JSON file to the Android project

Move the generated `articles.json` file to the following path in your project:

```
core/core-networks/src/dev/assets/articles.json
```

If you're building the Android app with the `dev` flavor, `DevStaticJsonTestNetworkDataSource` will be used automatically as the data source — this is useful for offline testing and development without a server connection.

## ⚙️ Dependencies

* `sentence-transformers` — for generating embeddings
* `transformers` — for tokenization
* `numpy<2` — for vector operations
* `pybind11>=2.12` — required by some libraries during build

## 📌 Notes

* The script automatically splits long texts into chunks, extracts embeddings, and saves the result as JSON.
* Default model: `all-MiniLM-L6-v2`. It will be downloaded from the internet on the first run.
* If you're using Gradle, you can integrate the script as an `Exec` task.

---

## 📞 Support

If you encounter errors when installing dependencies:

* Make sure you're using Python 3.8–3.11.
* Make sure NumPy hasn't been upgraded to version 2.0 — use `numpy<2`.
