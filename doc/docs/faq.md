
# ❓ Frequently Asked Questions (FAQ)

This document answers common questions about **zMantra**, the accessible math learning app.

---

## 📱 General

### 1. What is zMantra?

zMantra is an **accessible math learning app** designed for visually impaired students. It supports multiple interaction modes (tap, shake, draw, speech) and provides **audio feedback** with **TalkBack/TTS integration**.

### 2. Who can use zMantra?

* Students with visual impairments
* Teachers who want to create or upload question sets
* Anyone who wants to practice math with accessibility support

### 3. Which devices are supported?

* Minimum: Android 8.0 (API 26)
* Target: Android 15 (API 35)

---

## 🧑‍🏫 Features

### 4. What game modes are available?

* **Tap Mode** – Answer by tapping options
* **Shake Mode** – Answer by shaking the device
* **Drawing Mode** – Write answers on screen
* **Number Line & Angle Mode** – Interactive visual + audio math tasks
* **Quick Play** – Fast practice session with cached/excel-loaded questions

### 5. Can I add my own questions?

Yes ✅. You can prepare **Excel files** with questions and load them into the app.

### 6. Does it support multiple languages?

Currently, **English + Malayalam** are supported (voice + text). More languages may be added in the future.

---

## 🛠️ Technical

### 7. What architecture does the app use?

zMantra follows **MVVM + Clean Architecture**, with proper separation of concerns for:

* `domain` → business logic & use cases
* `data` → repositories, models, question loaders
* `presentation` → fragments, ViewModels, accessibility integration

### 8. How are questions loaded?

* From **Excel files** using Apache POI
* Cached in **QuestionCache** for faster reloading
* Planned: JSON and user-uploaded sets

### 9. Which libraries are used?

Some key ones:

* **Glide** → GIF & image loading
* **Apache POI** → Excel reading
* **Lottie** → animations
* **Hilt** → dependency injection
* **Exp4j** → math expression parsing
* **Espresso** → UI testing

---

## ♿ Accessibility

### 10. How does zMantra support blind users?

* Full **TalkBack support**
* **TTSUtility** for reading out questions, hints, and answers
* Inline result feedback with **GIF + speech**
* Strict **retry logic (3 attempts per question)** with automatic answer reveal

### 11. Can deaf users use it?

Yes. Although the app is focused on visually impaired students, **textual feedback and animations** also support hearing-impaired learners.

---

## 🚀 Development & Contribution

### 12. How can I contribute?

See [CONTRIBUTING.md](contribute.md). You can help by:

* Fixing bugs
* Adding new game modes
* Improving accessibility
* Enhancing documentation

### 13. How do I run the project locally?

```bash
git clone https://github.com/<your-username>/zmantra.git
cd zmantra
./gradlew assembleDebug
```

Then install the APK on an Android device or emulator.

---

## 📄 Licensing

### 14. Is zMantra free?

Yes ✅. It’s open-source and licensed under the [MIT License](license).


