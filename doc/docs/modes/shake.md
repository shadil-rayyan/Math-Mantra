
# Developer Documentation – Shake Game Feature

This document provides technical details for developers working on the **Shake Game** feature of **z.Mantra**. It explains the structure, logic, and extensibility of the `ShakeFragment` and related components.

---

## 📂 Location

```
com.zendalona.zmantra.presentation.features.game.shake
```

---

## 📄 Core Class: `ShakeFragment`

The `ShakeFragment` implements a game where the player answers questions by shaking the device.

It extends:

* **`BaseGameFragment`** → provides shared game logic (TTS, answer submission, game flow).
* **`SensorEventListener`** → listens to accelerometer data for detecting shakes.

---

## 🏗️ Lifecycle & Flow

1. **Initialization**

   * `SensorManager` and accelerometer sensor are registered in `onResume`.
   * GIF animation (`rotate your phone`) is loaded if defined.

2. **Loading Questions**

   * `onQuestionsLoaded()` receives a list of `GameQuestion` objects.
   * Each question specifies:

     * `expression`: Instruction (e.g., *"Shake 3 times"*).
     * `answer`: Correct shake count.
     * `timeLimit`: Allowed time to complete (default = 10s if undefined).

3. **Starting a Question**

   * `startQuestion()`:

     * Resets counters (`count`, `wrongAttempts`, `answerChecked`).
     * Updates UI (`ringMeTv`, `ringCount`).
     * Announces question using TTS + accessibility.

4. **Shake Detection**

   * `onSensorChanged()` calculates acceleration from accelerometer values:

     ```kotlin
     val acceleration = sqrt((x*x + y*y + z*z).toDouble()).toFloat()
     if (acceleration > 12f) onShakeDetected()
     ```
   * Threshold = **12f** (tuned to filter small device movements).

5. **Shake Handling**

   * `onShakeDetected()`:

     * Increments count.
     * Announces count via TTS + Accessibility.
     * If count exceeds expected → triggers `checkAnswer(forceWrong = true)`.
     * If count == expected → delayed `checkAnswer()` after 1.5s.

6. **Answer Checking**

   * `checkAnswer()` compares `count` with `question.answer`.
   * If correct → calls `proceedToNextQuestion()`.
   * If wrong → resets and retries (after 1s).
   * Time is measured using `firstShakeTime`.

7. **Game Completion**

   * When all questions are done, `endGame()` is triggered from `BaseGameFragment`.

---

## 🔑 Important Variables

| Variable           | Purpose                                                |
| ------------------ | ------------------------------------------------------ |
| `count`            | Current shake count.                                   |
| `wrongAttempts`    | Tracks how many times the user failed before retrying. |
| `answerChecked`    | Prevents duplicate validation calls.                   |
| `firstShakeTime`   | Timestamp of first shake for time measurement.         |
| `isShakingAllowed` | Prevents multiple detections within 500ms.             |
| `shakeHandler`     | Controls shake debounce.                               |
| `gameHandler`      | Controls answer validation timing.                     |

---

## 🎯 Accessibility Support

* Each question instruction (`ringMeTv`) has `contentDescription` set and announced via `announce()`.
* Each shake count (`ringCount`) is announced live.
* Focus automatically moves to the instruction on the first question.

---

## ⚙️ Extensibility

### 1. **Change Shake Sensitivity**

Adjust threshold in `onSensorChanged()`:

```kotlin
if (acceleration > 10f) { ... } // More sensitive
```

### 2. **Custom Shake Animation**

Replace GIF in `getGifResource()`:

```kotlin
override fun getGifResource(): Int = R.drawable.my_custom_shake_gif
```

### 3. **Add Difficulty Levels**

Modify shake threshold/time limit dynamically based on question difficulty.

### 4. **Data Model (`GameQuestion`)**

Ensure `GameQuestion` has:

```kotlin
expression: String
answer: Int
timeLimit: Double?
```

---

## 🧪 Testing Notes

* **Manual Testing**

  * Verify shake counts are detected properly on different devices.
  * Check that accessibility announcements trigger correctly.
  * Test edge cases: 0 shakes, excessive shakes, timeouts.

* **Automated Testing**

  * Use `SensorManager` mocks for shake detection.
  * Validate game flow with sample `GameQuestion` inputs.

---

## 📌 Example Question Flow

1. App loads `GameQuestion("Shake 3 times", 3, 10)`.
2. Instruction announced: *"Shake 3 times"*.
3. Player shakes:

   * Shake 1 → Count = 1
   * Shake 2 → Count = 2
   * Shake 3 → Count = 3 → Validation after 1.5s.
4. Correct → Next question.

---
