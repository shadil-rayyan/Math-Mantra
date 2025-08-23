
# Developer Documentation – Stereo Game Feature

This document describes the **Stereo Game** feature implemented in `SterioFragment`.
It covers the structure, lifecycle, logic, and accessibility of the feature to guide future developers.

---

## 📂 Location

```
com.zendalona.zmantra.presentation.features.game.sterio
```

---

## 📄 Core Class: `SterioFragment`

The **Stereo Game** challenges users to solve math problems, but with a **spatial audio twist**:

* The **first number** is read from the **left** audio channel.
* The **operator** (e.g., `+`, `-`, `*`, `/`) is read in the **center** channel.
* The **second number** is read from the **right** audio channel.

This helps learners, especially those with **visual impairments**, practice **audio-based problem solving** while using **headphones**.

It extends:

* **`BaseGameFragment`** → handles shared game flow, accessibility announcements, and answer validation.

---

## 🏗️ Lifecycle & Flow

1. **Initialization**

   * UI is set up in `onCreateView()` with three key buttons:

     * **Read Question** → triggers question playback.
     * **Answer Input** → text field for answer.
     * **Submit Answer** → validates input.
   * Accessibility labels are applied for TalkBack support.

2. **Text-to-Speech & SoundPool**

   * `TextToSpeech` is initialized with the system locale (fallback: English).
   * Speech synthesis is written to `.wav` files in cache.
   * `SoundPool` plays synthesized audio with **channel-specific balance**.

3. **Loading Questions**

   * `onQuestionsLoaded()` provides a list of `GameQuestion`s.
   * If empty, a default question (`5 - 2`) is created.
   * Each expression is parsed using regex:

     ```
     (\d+)\s*([-+*/])\s*(\d+)
     ```

     → Extracts `numA`, operator, `numB`.

4. **Reading Question**

   * `readQuestionAloud()`:

     * Plays `numA` in **left channel**.
     * Plays operator in **center**.
     * Plays `numB` in **right channel**.
     * Uses delays (`1500ms`, `3000ms`) for sequencing.

   * If no headphones are connected, falls back to **single TTS output**.

5. **Submitting Answer**

   * User types into `answerEt` and presses submit.
   * `submitAnswer()`:

     * Validates non-empty input.
     * Measures elapsed time.
     * Calls `handleAnswerSubmission()` from `BaseGameFragment`.
     * On correct → next question. On incorrect → handled accordingly.

6. **Game Completion**

   * When all questions are answered → calls `endGame()`.

---

## 🔑 Important Variables

| Variable            | Purpose                                                   |
| ------------------- | --------------------------------------------------------- |
| `numA`, `numB`      | Operands extracted from question.                         |
| `correctAnswer`     | Expected correct result.                                  |
| `currentIndex`      | Tracks current question index.                            |
| `questionStartTime` | Timestamp for elapsed time tracking.                      |
| `ttsSynthesizer`    | Android Text-to-Speech engine for generating audio files. |
| `soundPool`         | Used for stereo playback of synthesized audio.            |
| `soundMap`          | Cache for mapping text → soundId in SoundPool.            |

---

## 🎧 Stereo Audio Logic

* **Left channel** → First number (e.g., `5`).
* **Center channel** → Operator (e.g., `-`).
* **Right channel** → Second number (e.g., `2`).

Volume balancing is applied:

```kotlin
val leftVolume = when (channel) { "right" -> 0.0f else -> 1.0f }
val rightVolume = when (channel) { "left" -> 0.0f else -> 1.0f }
```

---

## 🎯 Accessibility Support

* **TalkBack Content Descriptions**

  * `readQuestionBtn` → "Read question aloud"
  * `answerEt` → "Answer input field"
  * `submitAnswerBtn` → "Submit your answer"
* **Announcements**

  * New question readiness.
  * Error prompts ("Enter answer before submitting").
* **Focus management**

  * Focus shifts automatically to `readQuestionBtn` after question load.

---

## ⚙️ Extensibility

### 1. Support More Operators

Currently supports `+ - * /` via regex. Extend regex for more complex expressions if needed.

### 2. Dynamic Audio Channels

Add support for **more spatial cues** (e.g., diagonal, depth simulation) by adjusting SoundPool pan/balance.

### 3. Adaptive Difficulty

Vary:

* Operand size (single vs. double digits).
* Time limit (`15.0s` default).
* Delay between reads.

### 4. Alternate Input Modes

Replace text input with:

* Voice recognition (Speech-to-Text).
* Multiple-choice buttons.

---

## 🧪 Testing Notes

* **Manual Testing**

  * Test with headphones (wired & Bluetooth).
  * Ensure left/right balance plays correctly.
  * Check TalkBack announcements.
  * Validate input errors & correct answer flow.

* **Automated Testing**

  * Mock `AudioManager` for headphone detection.
  * Test regex parsing for expressions.
  * Verify `handleAnswerSubmission` logic with sample `GameQuestion`s.

---

## 📌 Example Flow

1. Question: `"5 - 2"`.
2. Stereo playback:

   * Left: `"5"`
   * Center: `"-"`
   * Right: `"2"`
3. User enters `"3"` → correct.
4. Next question loads.

---

