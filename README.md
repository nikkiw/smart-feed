# smart-feed

**smart-feed** is a modular Android demo application showcasing a dynamic article feed with on-device personalized recommendations. Built with modern Android technologies and clean architecture principles, it is designed as a high-quality, CI-integrated portfolio project.

---

## 🎥 Demo

> _Screencast of the application in action will be added here._

![Demo](https://github.com/user-attachments/assets/0d65b014-050b-4733-8d55-9ae5655efd8d)

---

## 🚀 Features

- 📰 **Dynamic article feed** with server sync and offline storage.
- 🧠 **On-device recommendation engine** using article embeddings.
- 📊 **User behavior tracking** and analytics-ready event logging.
- 🔄 **Pull-to-refresh** and **infinite scrolling** for seamless UX.
- 🧪 **Unit & instrumentation tests**, with GitHub Actions CI pipeline.
- 🧩 **Modular architecture** using Decompose for scalable feature development.

---

## 📱 Screens

- **Feed Screen** – with filter & sort.
- **Article Detail** – with full content and "Recommended Reading".
- **Recommendation Screen** – personalized content suggestions.

---

## 🛠 Tech Stack

| Layer        | Technologies                                        |
|--------------|-----------------------------------------------------|
| Language     | Kotlin                                              |
| Architecture | MVVM, Clean Architecture                            |
| Navigation   | [Decompose](https://github.com/arkivanov/Decompose) |
| DB           | Room                                                |
| Background   | WorkManager                                         |
| DI           | Hilt                                                |
| Networking   | Retrofit                                            |
| Async        | Coroutines                                          |
| CI/CD        | GitHub Actions                                      |

---

## 📁 Project Structure

```plaintext
smart-feed/
├── app/             # App module (UI, ViewModel, DI)
├── build-logic/     # Custom Gradle plugins
├── buildSrc/        # Build configuration and constants
├── core/            # Shared models and utilities
├── docs/            # Markdown documentation
├── feature/         # UI features with Decompose components
├── mock-server/     # Mock server for local dev
└── scripts/         # Data generation scripts
````

---

## 📦 Installation

1. Clone the repo:
```bash
git clone https://github.com/nikkiw/smart-feed.git
cd smart-feed
```

2. Open in **Android Studio Hedgehog+**

3. Run on emulator or physical device (Android 6.0+, SDK 23+)

---

## 🔄 Syncing & Recommendations

* Periodic syncing with `ContentFetchWorker` via `WorkManager`
* Recommendations built locally using weighted article embeddings

---

## ✅ Testing & CI

* **Unit tests:** ViewModels, UseCases, DAOs with JUnit + MockK
* **Instrumentation tests:** Room DB, WorkManager
* **CI:** GitHub Actions (lint, build, test on PR & push)

---

## 📚 Documentation

* API: [`docs/content_delta_sync_spec.md`](docs/content_delta_sync_spec.md)
* Spec: [`docs/tech-spec.md`](docs/tech-spec.md)

---

## 📅 Roadmap

* [ ] Add screencast and screenshots
* [ ] Responsive UI for tablets
* [ ] Support for videos, podcasts, and polls
* [ ] Analytics dashboard for engagement metrics
* [ ] Localization & internationalization

---

## 📄 License

Apache License. See [`LICENSE`](LICENSE) for details.

---

## 🙋‍♂️ Author

Made by [Nikolay Vlasov](https://www.linkedin.com/in/nikolay-vlasov-dev) – open to feedback and collaboration.

