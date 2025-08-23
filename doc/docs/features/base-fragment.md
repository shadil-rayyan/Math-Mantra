

## 📄 `BaseGameFragment.kt` Developer Documentation

### 📌 Overview

`BaseGameFragment` is an **abstract fragment** that provides shared logic for all game-mode fragments in the zMantra app. It handles:

* Question loading (from cache or Excel fallback)
* Answer grading with retry logic
* Accessibility announcements
* Hint system integration
* GIF animations (optional)
* TTS (Text-to-Speech)
* Dialog feedback (result, retry, next, correct answer)

Any game fragment (e.g., Tap, Shake, Drawing) should extend `BaseGameFragment` to reuse this logic.

---

### 🧠 Key Responsibilities

| Feature             | Description                                                                               |
| ------------------- | ----------------------------------------------------------------------------------------- |
| 🧠 Question Loading | Loads questions from `QuestionCache`, or falls back to Excel using `ExcelQuestionLoader`. |
| 📣 TTS Utility      | Sets up a reusable `TTSUtility` instance for spoken feedback.                             |
| 🧩 Retry Logic      | Allows `maxAttempts` per question before revealing the correct answer.                    |
| 📊 Grading          | Uses `GradingUtils` to assign grades based on response time.                              |
| 🧩 Hints            | Integrates with `HintFragment` for game-mode-specific hints.                              |
| 🖼 GIF Support      | Optionally loads mode-specific GIFs using Glide.                                          |
| 🔊 Accessibility    | Announces important actions for TalkBack users.                                           |
| 📦 Dialog System    | Uses `DialogUtils` to show result, retry, next, and answer dialogs.                       |

---

### 🔧 How to Use

To create a new game mode fragment, extend `BaseGameFragment`:

```kotlin
class MyGameModeFragment : BaseGameFragment() {

    override fun getModeName(): String = "myGameMode"

    override fun onQuestionsLoaded(questions: List<GameQuestion>) {
        // TODO: display questions and start interaction
    }

    override fun getGifImageView(): ImageView? = binding.myGifView
    override fun getGifResource(): Int = R.drawable.my_mode_gif
}
```

---

### ⚙️ Lifecycle Setup

| Method            | Purpose                                      |
| ----------------- | -------------------------------------------- |
| `onCreate()`      | Initializes `lang`, `difficulty`, and `tts`. |
| `onViewCreated()` | Triggers question loading.                   |
| `onDestroyView()` | Shuts down TTS to avoid memory leaks.        |

---

### 🔄 Question Loading Logic

```kotlin
loadQuestions()
```

* Checks if questions exist in `QuestionCache`.
* If not, loads from Excel and caches them.
* Fails gracefully if no questions are found.

Callback:

```kotlin
protected abstract fun onQuestionsLoaded(questions: List<GameQuestion>)
```

---

### 🧪 Answer Evaluation

```kotlin
handleAnswerSubmission(
    userAnswer = ...,
    correctAnswer = ...,
    elapsedTime = ...,
    timeLimit = ...,
    onCorrect = { ... },
    onIncorrect = { ... },
    onShowCorrect = { correctAnswer -> ... }
)
```

* Shows retry dialog on wrong answer.
* Shows correct answer after 3 failed attempts.
* Resets `attemptCount` upon success or after max failures.

---

### 🧠 Hint Integration

```kotlin
override fun showHint() {
    // Launches HintFragment with current mode as argument
}
```

---

### ♿ Accessibility

```kotlin
announce(view, "Your message")
announceNextQuestion(view)
```

* Speaks content using TalkBack if enabled.
* Automatically announces transition to the next question.

---

### 🎞 GIF Support (Optional)

```kotlin
loadGifIfDefined()
```

Override `getGifImageView()` and `getGifResource()` in child fragment to show a GIF.

---

### 📊 Dialogs Available

| Method                            | Description                    |
| --------------------------------- | ------------------------------ |
| `showResultDialog(grade)`         | Shown after correct answer     |
| `showRetryDialog()`               | Shown after incorrect answer   |
| `showCorrectAnswerDialog(answer)` | Shown after 3 wrong attempts   |
| `showNextDialog()`                | Used to indicate next question |

---

### 🧩 Overridable Properties

| Property      | Default              | Purpose                          |
| ------------- | -------------------- | -------------------------------- |
| `maxAttempts` | 3                    | Retry limit per question         |
| `mode`        | From `getModeName()` | Used in hint and question loader |

---

### 🧪 Abstract Methods to Implement

| Method                         | Description                                                    |
| ------------------------------ | -------------------------------------------------------------- |
| `getModeName()`                | Unique string for current game mode (used in hint + cache key) |
| `onQuestionsLoaded(questions)` | Called when questions are ready to use                         |

---

### 📁 Dependencies

* `TTSUtility`: Manages speech output
* `GradingUtils`: Converts time to grade
* `DialogUtils`: Manages feedback dialogs
* `ExcelQuestionLoader`: Reads Excel-based questions
* `QuestionCache`: Stores and retrieves cached questions
* `LocaleHelper` & `DifficultyPreferences`: User settings
* `HintFragment`: Displays hint for current game mode

---


