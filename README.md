

<div align="center">

# zMantra – Accessible Math Learning for All

**Empowering inclusive learning through touch, sound, and interaction.**

[![Platform](https://img.shields.io/badge/platform-Android-green.svg)]()
[![Language](https://img.shields.io/github/languages/top/shadil-rayyan/Math-Mantra)]()
[![Build](https://img.shields.io/github/actions/workflow/status/shadil-rayyan/Math-Mantra/android.yml?label=build)]()
[![Last Commit](https://img.shields.io/github/last-commit/shadil-rayyan/Math-Mantra)]()
[![Issues](https://img.shields.io/github/issues/shadil-rayyan/Math-Mantra)]()
[![Stars](https://img.shields.io/github/stars/shadil-rayyan/Math-Mantra?style=social)]()

</div>

---

## 🎯 Overview

**zMantra** is a fun, inclusive math learning app for Android, thoughtfully designed for both **visually impaired** and **sighted learners**. The app combines **audio-based guidance**, **gesture-based interaction**, and **playful math challenges** to make math education engaging and barrier-free.

> **Built with love using Kotlin, accessibility-first design, and real-world pedagogy.**

---

## 🚀 Demo

![zMantra Demo](https://raw.githubusercontent.com/shadil-rayyan/Math-Mantra/refactoring/.github/.preview/out.gif)


---

## 📚 Background & Contributions

There was a lack of **fully accessible math learning apps** for Android that truly catered to the needs of **visually impaired learners**. Many existing apps either lacked TalkBack support or failed to offer inclusive interaction models.

To address this gap, zMantra was developed by taking inspiration from the **GSoC 2024 Math Mantra project**, especially for features like:

* 🔔 **Bell Ring Mode**
* 🥁 **Drum Mode**
* 📈 **Number Line Mode**

While these served as initial references, the majority of the app—including the **architecture, UX flows, and implementation**—has been **rewritten in Kotlin** and **refactored** for modern standards. Major additions and improvements include:

* 🚀 **New Modes**: Quick Play, Learning Mode, and Game Mode
* 🎛️ **Customizable Settings** for accessibility and experience
* 🧩 **Expanded Gameplay Logic**, accessibility services, and hints system

zMantra is now a **standalone, robust, and modern educational game** built from the ground up with inclusivity as its core.

---


## ✨ Key Features

- 🎧 **Fully accessible** with TalkBack and custom AccessibilityService
- 📊 **Real-world math** via story-based problems
- 🧠 **Multiple game modes** – shake, tap, draw, touch, and more
- 🗣️ **Text-to-Speech (TTS)** powered guidance and instructions
- 🧩 **Quick Play** for short sessions and practice
- 🎮 **Game Mode** with interactive activities
- 🎓 **Learning Mode** for deeper concept understanding
- ⚙️ Customizable settings: speech rate, language, difficulty, contrast, music volume

---

## 🛠️ Tech Stack

| Technology                      | Description                                               |
| ------------------------------- | --------------------------------------------------------- |
| **Kotlin (XML)**                | Core language with traditional Android XML layouts        |
| **MVVM Architecture**           | Decoupled, scalable, and testable app structure           |
| **Glide**                       | Smooth loading of images and GIFs                         |
| **Lottie**                      | Beautiful animations using JSON-based vector graphics     |
| **Apache POI**                  | Parsing Excel files for dynamic question and hint loading |
| **exp4j**                       | Lightweight math expression evaluation engine             |
| **TextToSpeech (TTS)**          | Provides spoken feedback and instructions to users        |
| **Custom AccessibilityService** | Enables gesture control and TalkBack enhancements         |
| **ViewBinding**                 | Safe and efficient view access from layouts               |
| **Espresso / JUnit / Mockito**  | Robust UI and unit testing support                        |
| **Version Catalog (libs.**\*)   | Modern dependency management in Gradle via `libs.*`       |

---



## 🧑‍💻 Interaction Modes

| Mode | Description |
|------|-------------|
| **Bell Ring** | Shake the phone a number of times |
| **Drum Play** | Tap the screen to match numbers |
| **Drawing** | Sketch shapes on screen |
| **Touch Count** | Tap the screen to submit number answers |
| **Stereo Sound** | Identify numbers using audio in left/right ears |
| **Number Line** | Move a character on a visual/aural number line |
| **MCQ** | Audio-based multiple-choice questions |
| **Compass Navigation** | Rotate the phone to point North, South, etc. |
| **Day Calculation** | Identify future days from today |
| **Mental Math** | Timed arithmetic questions |

---

## ♿ Accessibility Highlights

- 📱 Full TalkBack support across activities
- ✋ Haptic and auditory cues for feedback
- 👆 Custom gestures for visually impaired learners
- 🎨 High contrast Dark Mode for low-vision users
- 🧠 Simple, consistent layout for easy memory-based navigation

---

## ⚙️ Settings & Customization

- Language: Switch between supported TTS languages
- Difficulty: Easy / Medium / Hard
- Music & TTS volume
- Speech rate
- Enable/disable background music
- Dark Mode

---

## 📌 Roadmap

Here’s what’s planned for future releases:

* ✅ **User-defined Questions**: Allow users to import or create their own questions
* ⌨️ **Custom Keyboard**: Develop a large, accessible keyboard optimized for non-visual input
* 👥 **Multiplayer Mode**: Enable peer-to-peer or local multiplayer game sessions
* ✍️ **Drawing Answer Recognition**: Evaluate math answers drawn by the user
* 🧱 **Tiler Frame Mode**: Introduce a spatial reasoning mode for tactile visual learners

---


## 🧪 Status

* **Main branch**: ✅ Stable
* **Development**: 🛠️ Ongoing with regular enhancements
* **Beta testing**: ✅ Welcomed — reach out via Issues

---

## 🙏 Credits

* **Design & Development**: Shadil A M
* **Artwork, Documentation, Testing, Mentors**: \[To be added]
* **Special thanks** to all the testers, educators, and learners who shaped zMantra.
---

<div align="center">

> 📘 *"Mathematics is for everyone. With zMantra, we make sure it's also reachable."*

</div>


