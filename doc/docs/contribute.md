
# Contributing to zMantra

Thank you for considering contributing to **zMantra** 🎉
We welcome all contributions—whether it’s bug fixes, new features, documentation improvements, or accessibility enhancements.

---

## 🛠️ How to Contribute

### 1. Fork & Clone

1. Fork the repository to your GitHub account.
2. Clone your fork locally:

   ```bash
   git clone https://github.com/<your-username>/zmantra.git
   cd zmantra
   ```

### 2. Create a Branch

Always create a new branch for your work:

```bash
git checkout -b feature/your-feature-name
```

### 3. Make Changes

* Follow our **MVVM + Clean Architecture** structure.
* Write clean, testable, and accessible code.
* Ensure your changes do not break existing features.

### 4. Run Tests

Before submitting, make sure all tests pass:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

### 5. Commit Guidelines

Use clear, descriptive commit messages:

```bash
git commit -m "Fix: crash on TalkBack focus in ShakeFragment"
git commit -m "Feat: add support for custom Excel question sets"
```

### 6. Push & Pull Request

Push your branch and open a Pull Request (PR):

```bash
git push origin feature/your-feature-name
```

Then, open a PR against the `main` branch.

---

## ✅ Contribution Types

We appreciate contributions in the following areas:

* **Bug Fixes** – Resolve crashes, accessibility issues, or performance bugs.
* **Features** – Add new game modes, question types, or TTS enhancements.
* **Accessibility** – Improve TalkBack, keyboard navigation, and voice feedback.
* **Documentation** – Enhance developer or user docs (MkDocs, README, etc.).
* **Testing** – Write or improve unit, integration, and UI tests.

---

## 🧑‍🤝‍🧑 Code Style & Practices

* Follow **Kotlin coding standards**.
* Use **View Binding**, **Hilt**, and **MVVM + Clean Architecture**.
* Accessibility is a priority → always ensure TalkBack works.
* Write **unit tests** for ViewModels and business logic.

---

## 📜 License

By contributing, you agree that your contributions will be licensed under the same license as the project (see [LICENSE](LICENSE)).

