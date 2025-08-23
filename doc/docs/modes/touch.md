
# TouchScreen (Developer Doc)

## Overview

`TouchScreenFragment` is a game mode in **z.Mantra** where players must place the correct number of fingers on the screen simultaneously to answer math-based touch questions.

This game uses **multi-touch detection** (`MotionEvent.pointerCount`) to validate answers, and integrates with **custom accessibility handling** to ensure TalkBack users can still interact using a **single tap gesture**.

---

## Class Location

```kotlin
package com.zendalona.zmantra.presentation.features.game.touchscreen
```

---

## Responsibilities

* Present **multi-finger touch** questions to the player.
* Detect the number of fingers (`pointerCount`) on the screen.
* Provide feedback for correct and incorrect attempts.
* Handle **attempt limits** and **celebration sounds**.
* Maintain accessibility support by customizing touch exploration.

---

## Key Components

### 1. State Variables

* `index`: Current question index.
* `inputLocked`: Prevents multiple simultaneous validations.
* `questionStartTime`: Tracks when a question started (for timing/grade calculation).
* `correctAnswer`: Expected number of fingers for the current question.
* `questionList`: All loaded questions.
* `isFirstQuestion`: Ensures the first instruction is auto-focused for accessibility.
* `handler`: Used for delayed execution.

---

### 2. Game Flow

#### Load Questions

```kotlin
override fun onQuestionsLoaded(questions: List<GameQuestion>)
```

* Stores the question list.
* Resets index to 0.
* Calls `startGame()`.

#### Start Game

```kotlin
private fun startGame()
```

* Ends the game if all questions are completed.
* Loads the next question, skipping any with an answer `< 3` (invalid for multi-touch).
* Prepares and announces the question.
* Sets up touch listener for finger detection.

#### Touch Handling

```kotlin
private fun setupTouchListener(question: GameQuestion)
```

* Detects number of fingers using `event.pointerCount`.
* If `pointerCount == correctAnswer` → correct.
* On `ACTION_UP`, if not already correct → incorrect attempt.

---

### 3. Answer Processing

#### Correct Answer

```kotlin
private fun handleCorrectAnswer(question: GameQuestion)
```

* Calculates elapsed time → assigns grade (`getGrade()`).
* Plays a celebration sound (`bell_ring`) if enabled.
* Shows result dialog.
* Moves to next question.

#### Incorrect Answer

```kotlin
private fun handleIncorrectAnswer(question: GameQuestion)
```

* Increments `attemptCount`.
* If `attemptCount >= maxAttempts` → show correct answer and move on.
* Otherwise → show retry dialog and unlock input.

---

### 4. Accessibility Features

* **Custom Accessibility Handling**

  * Uses `AccessibilityHelper` to **disable ExploreByTouch** when active, so players can use single-tap gestures without conflict from TalkBack.
  * Restores normal TalkBack navigation when paused.

```kotlin
override fun onResume() {
    AccessibilityHelper.disableExploreByTouch()
}
override fun onPause() {
    AccessibilityHelper.resetExploreByTouch()
}
```

* **Announcements**

  * Instructions and results are spoken aloud with `announceForAccessibility()` and `tts`.

---

### 5. Lifecycle Management

* **onResume**: Disables TalkBack’s explore-by-touch mode.
* **onPause**: Restores accessibility defaults, stops TTS, clears handler.
* **onDestroyView**: Releases binding reference.

---

## Example Question Flow

1. Question: `"Touch the screen with 3+2 fingers"`
2. Correct answer = **5 fingers**
3. Player interaction:

   * Player touches screen with 4 fingers → incorrect attempt.
   * Player retries with 5 fingers → correct, celebration sound plays.
   * Game moves to next question.

---




