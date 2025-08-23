
## 📖 What is zMantra?

**zMantra** is an **accessible, inclusive math learning application for Android** designed with an **accessibility-first philosophy**. The project’s mission is to make mathematics engaging, interactive, and barrier-free for both **visually impaired and sighted learners**.

Unlike traditional math apps that focus only on visual interaction, zMantra combines **audio guidance**, **gesture-based controls**, and **multiple interaction modes** (e.g., shake, tap, draw, stereo sound) to create an **immersive learning environment** for users of all abilities.

The app is built entirely in **Kotlin** with **MVVM + Clean Architecture principles**, ensuring scalability, testability, and long-term maintainability. It leverages **Android accessibility APIs**, **custom AccessibilityService**, and **Text-to-Speech (TTS)** for fully voice-guided navigation and learning.

---

## 🎯 Core Objectives

* **Accessibility-First**: Provide a fully navigable math learning experience with TalkBack, haptic cues, and custom gestures.
* **Inclusivity**: Equal focus on both **visually impaired** and **sighted learners**.
* **Engagement**: Learning through **playful, interactive game modes** rather than passive reading.
* **Extensibility**: Designed with clean separation of concerns to support future modes (e.g., Multiplayer, Drawing Recognition).

---

## 🛠️ Technical Highlights

* **Language & Frameworks**: Kotlin (XML layouts, ViewBinding)
* **Architecture**: MVVM + Clean Architecture (presentation, domain, data layers)
* **Accessibility**: TalkBack, TTS, custom AccessibilityService, haptic feedback
* **Game Logic**: Gesture-based input (tap, shake, draw, compass, stereo sound)
* **Dynamic Content**: Excel-based question parsing with Apache POI
* **Animations & Media**: Glide (GIFs), Lottie (vector animations)
* **Testing**: Espresso (UI), JUnit + Mockito (unit tests)
* **Dependency Management**: Gradle Version Catalog (`libs.*`)

---

## 🎮 Interaction Modes

Each learning activity is implemented as a **fragment** extending a shared `BaseGameFragment` to enforce consistent game flow (question loading, grading, retries, accessibility feedback).

Examples of modes:

* **Bell Ring Mode**: Shake the device to answer.
* **Touch Count Mode**: Tap screen to enter numbers.
* **Stereo Sound Mode**: Identify numbers based on left/right audio cues.
* **Drawing Mode**: Sketch math shapes.
* **Number Line Mode**: Navigate along a number line visually + aurally.
* **Quick Play & Game Modes**: Short practice sessions or full challenge sessions.

---

## ♿ Accessibility Commitment

* **TalkBack-first UI/UX**
* **Custom gestures** for input without visuals
* **TTS guidance** for every action
* **High contrast Dark Mode**
* **Consistent layout design** for easy mental navigation

---

## 🔮 Roadmap for Developers

* **User-Defined Question Sets** (Excel/JSON import)
* **Custom Accessible Keyboard**
* **Multiplayer Mode**
* **AI-powered Drawing Recognition**
* **New Spatial Reasoning Modes**

---

## 📌 Why zMantra Matters

Developers contributing to zMantra are not just building another math app—they are shaping an **inclusive educational tool** that gives **visually impaired learners equal access** to mathematics.

By combining **modern Android development practices** with **inclusive design principles**, zMantra sets a benchmark for **accessible educational technology** on mobile platforms.

