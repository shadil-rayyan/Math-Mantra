
# 🎮 Quick Play Mode (Developer Guide)

`QuickPlayFragment` implements the **Quick Play** mode in zMantra.  
It provides a simple Q&A flow with a text input box, immediate feedback, scoring, vibration, and sounds.  

File:  
`app/src/main/java/com/zendalona/zmantra/presentation/features/quickplay/QuickPlayFragment.kt`

---

## Lifecycle & Responsibilities

- Extends [`BaseGameFragment`](../architecture/base-game-fragment.md).  
- Loads questions passed from Excel (`assets/questions/{lang}.xlsx`) based on the *mode name*.  
- Presents one question at a time with a text field for answers.  
- Evaluates user input, updates score, gives feedback (sound + vibration).  
- Ends game when:
  - All questions are answered, OR  
  - Too many wrong answers (`>= 7`).  

---

## Key Properties

| Property            | Purpose                                                                 |
|---------------------|-------------------------------------------------------------------------|
| `questionList`      | Current batch of `GameQuestion`s loaded for this session.               |
| `wrongQuestionsSet` | Tracks incorrectly answered question indices. Ends game after 7 wrong.  |
| `currentIndex`      | Tracks current question number.                                         |
| `totalScore`        | Cumulative score across answered questions.                            |
| `currentQuestionTimeLimit` | Per-question time limit (default 60s, can be overridden by question). |
| `mediaPlayer`       | Plays "correct" or "wrong" feedback sounds.                             |
| `questionCategory`  | Mode/category name → must match `getModeName()` in questions file.      |
| `hintMode`          | Controls what type of hint screen is shown.                             |

---

## Navigation & Mode Name

The fragment declares its **mode name** for the framework:

```kotlin
override fun getModeName(): String = questionCategory ?: "default"
````

This must match the `mode` column in `questions/{lang}.xlsx`.

Example:

```excel
question                               | mode       | operands | difficulty | equation   | time
---------------------------------------|------------|----------|------------|------------|-----
"What is {a}+{b}?"                     | quickplay  | 1:5,1:5  | 1          | {a}+{b}    | 30
```

---

## Question Flow

### Loading

```kotlin
override fun onQuestionsLoaded(questions: List<GameQuestion>) {
    questionList.clear()
    questionList.addAll(questions)
    loadNextQuestion()
}
```

### Advancing

* `loadNextQuestion()` sets current question text and resets input.
* Announces the question for accessibility:

```kotlin
announce(binding.questionTv, questionText)
```

### Answer Checking

Handled in `checkAnswer()`:

* **Correct answer**

  * Vibrate short (`200ms`)
  * Grade based on speed → points added
  * Play `correct_sound`
  * Load next question

* **Incorrect answer**

  * Vibrate longer (`400ms`)
  * Play `wrong_sound`
  * Clear input (retry allowed)

* **"Show correct" case**

  * Vibrate long (`600ms`)
  * Add minimal score (grade = "Wrong Answer")
  * Add to `wrongQuestionsSet`
  * End game if 7 wrong

---

## Hints

The `showHint()` override launches a `HintFragment`:

```kotlin
override fun showHint() {
    val bundle = Bundle().apply { putString("mode", hintMode) }
    val hintFragment = HintFragment().apply { arguments = bundle }

    requireActivity().supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, hintFragment)
        .addToBackStack(null)
        .commit()
}
```

---

## Accessibility

* Initial focus moves to the question text for screen reader users:

```kotlin
Handler(Looper.getMainLooper()).postDelayed({
    binding.questionTv?.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
}, 300)
```

* Questions are also announced programmatically via `announce(...)`.

---

## Extending Quick Play

* **New categories:**
  Add rows in `{lang}.xlsx` with `mode=quickplay` and a distinct `category`.
  Pass the category when creating the fragment:

  ```kotlin
  val fragment = QuickPlayFragment.newInstance(category = "fractions", hintMode = "visual")
  ```

* **Custom feedback:**
  Extend `playSound()` or `VibrationUtils` for richer feedback.

* **Scoring rules:**
  Modify `GradingUtils.getPointsForGrade()` or adjust grading thresholds.

* **New Hint behavior:**
  Add new `hintMode` values and handle them in `HintFragment`.

---

## Common Pitfalls

* Make sure `getModeName()` matches the `mode` in Excel; otherwise, no questions load.
* Forgetting to release `MediaPlayer` can cause memory leaks → handled in `onDestroyView()`.
* If accessibility announcements overlap, check timing (`300ms` delay is a balance).

---

## Companion API

To launch:

```kotlin
val fragment = QuickPlayFragment.newInstance(
    category = "quickplay", 
    hintMode = "default"
)
supportFragmentManager.beginTransaction()
    .replace(R.id.fragment_container, fragment)
    .commit()
```

