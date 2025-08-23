
# Developer Documentation – TapFragment

## Overview

`TapFragment` is a game mode in **z.Mantra** where users solve questions by tapping on the screen a number of times equal to the answer.

This game mode is fully **accessible**, with custom accessibility handling that disables TalkBack’s default **two-tap requirement** and allows single-tap interaction for smoother gameplay.

---

## Class Location

```kotlin
package com.zendalona.zmantra.presentation.features.game.tap
```

---

## Responsibilities

* Load and display tap-based math questions (`GameQuestion`).
* Count user taps and compare with the expected answer.
* Handle accessibility announcements for visually impaired users.
* Enforce attempt limits and time limits.
* Move between questions until the game ends.

---

## Key Components

### 1. State Management

* `questionIndex`: Tracks the current question.
* `tapCount`: Number of taps the player has made for the current question.
* `attemptCount`: Number of attempts allowed per question (default: 3).
* `questionStartTime`: Time when the question started, used for tracking response time.
* `isFirstQuestion`: Ensures the first instruction is auto-focused for accessibility.

---

### 2. Game Flow

1. **Load Questions**

   ```kotlin
   override fun onQuestionsLoaded(questions: List<GameQuestion>)
   ```

   * Receives questions from the parent `BaseGameFragment`.
   * Starts the first question.

2. **Start Question**

   ```kotlin
   private fun startQuestion()
   ```

   * Resets state (`tapCount`, `attemptCount`, etc.).
   * Displays instruction text with accessibility announcements.
   * Prepares the screen for tapping.

3. **Handle Tap**

   ```kotlin
   private fun onTap()
   ```

   * Increments `tapCount` with each user tap.
   * If taps exceed the correct answer → handled as wrong attempt.
   * If taps match the correct answer → waits **3 seconds** before confirming correctness.

4. **Process Wrong Attempt**

   ```kotlin
   private fun processWrongAnswer()
   ```

   * Increments `attemptCount`.
   * If attempts exceed limit → show correct answer and move on.
   * Otherwise, reset `tapCount`.

5. **Check Answer**

   ```kotlin
   private fun checkAnswer(correctNow: Boolean)
   ```

   * Confirms if the tap count matches the expected answer.
   * Calls `handleAnswerSubmission()` from `BaseGameFragment`.

6. **Next Question**

   ```kotlin
   private fun goToNextQuestion()
   ```

   * Moves to the next question with a **1.2s delay**.

---

### 3. Accessibility Features

* **Custom Accessibility Handling**:

  * Uses `AccessibilityHelper` to **disable ExploreByTouch** during gameplay.
  * This ensures taps are directly interpreted as game input instead of TalkBack gestures.
  * Restores TalkBack defaults when fragment pauses.

* **Announcements**:

  * Instruction text and tap counts are read aloud using `announce()` for TalkBack users.

---

### 4. Lifecycle Management

* **onResume**
  Disables TalkBack’s touch exploration to allow **single-tap gameplay**.

  ```kotlin
  AccessibilityHelper.disableExploreByTouch()
  ```

* **onPause**
  Restores default TalkBack behavior and clears handler callbacks.

  ```kotlin
  AccessibilityHelper.resetExploreByTouch()
  handler.removeCallbacksAndMessages(null)
  tts.stop()
  ```

* **onDestroyView**
  Cleans up handler and binding.

---

## Example Question Flow

1. Question: `Tap 3 times`
2. User taps:

   * Tap 1 → UI shows `1` (announced).
   * Tap 2 → UI shows `2` (announced).
   * Tap 3 → triggers a 3-second delay.
   * If no extra taps → marked **correct**.
   * If user taps again (4th tap) → marked **wrong attempt**.




