
## 🧩 `DialogUtils.kt` – Unified Dialog and Feedback Handler

This utility centralizes animated result feedback in **zMantra**, providing both **inline overlays** and fallback **AlertDialogs** for results like *"Excellent"*, *"Wrong"*, *"Try Again"*, etc.

It also integrates:

* 🎙️ `TTSUtility` for speech feedback
* 🖼️ `Glide` for animated GIFs
* 🔁 Consistent timing and user experience

---

### 📁 Location

`com.zendalona.zmantra.utility.common.DialogUtils`

---

## 🧠 Usage Overview

Call methods like `showResultDialog()`, `showRetryDialog()`, or `showCorrectAnswerDialog()` from your game fragment to show user feedback. Internally, they delegate to a shared `showCustomDialog()` which prefers inline overlay (if present) or gracefully falls back to a standard dialog.

---

## ✅ Supported Dialog Types

| Method                      | Purpose                                | Overlay or Dialog    | TTS | Example Visual |
| --------------------------- | -------------------------------------- | -------------------- | --- | -------------- |
| `showResultDialog()`        | Final grading (Excellent, Good, etc.)  | ✅ Inline or fallback | ✅   |                |
| `showRetryDialog()`         | Wrong answer (attempt remaining)       | ✅ Inline or fallback | ✅   |                |
| `showCorrectAnswerDialog()` | Show correct answer after failures     | ✅ Inline or fallback | ✅   |                |
| `showNextDialog()`          | Congratulate and move to next question | ✅ Inline or fallback | ✅   |                |

---

## 🧱 Inline Overlay Structure

If the following views are present in the fragment layout, the dialog is shown inline:

* `R.id.feedbackOverlay` (ViewGroup)
* `R.id.messageTextView` (TextView)
* `R.id.gifImageView` (ImageView)

Fallback to AlertDialog if any view is missing.

---

## 💡 Sample Integration

```kotlin
DialogUtils.showRetryDialog(
    context = requireContext(),
    inflater = layoutInflater,
    ttsUtility = ttsUtility,
    message = "Try again!",
    onContinue = { loadNextQuestion() }
)
```

---

## 🗂️ Grade → Message and GIF Mapping

Defined in `appreciationData`:

| Grade         | Messages (`R.string.*`) | GIFs (`R.drawable.*`)       |
| ------------- | ----------------------- | --------------------------- |
| `"Excellent"` | excellent\_1..5         | dialog\_excellent\_1..3     |
| `"Very Good"` | very\_good\_1..5        | dialog\_very\_good\_1..3    |
| `"Good"`      | good\_1..5              | dialog\_good\_1..3          |
| `"Not Bad"`   | not\_bad\_1..5          | dialog\_not\_bad\_1..3      |
| `"Okay"`      | okay\_1..5              | dialog\_okay\_1..3          |
| `"Wrong"`     | wrong\_answer           | dialog\_wrong\_answer\_1..3 |

---

## 🧩 `showCustomDialog()` Parameters

| Param               | Description                                |
| ------------------- | ------------------------------------------ |
| `context`           | Activity context                           |
| `inflater`          | For inflating fallback layout              |
| `grade`             | Grade category (`"Good"`, `"Wrong"`, etc.) |
| `ttsUtility`        | Optional text-to-speech speaker            |
| `message`           | Custom override text                       |
| `drawableRes`       | Custom override image                      |
| `speakText`         | Custom override for TTS                    |
| `vibrationDuration` | Device vibration duration (default: 150ms) |
| `onContinue`        | Callback after 2.5 seconds                 |

---

## 🔁 Dialog Lifecycle

* Visible for **2.5 seconds**
* Plays TTS immediately
* Auto-dismisses and calls `onContinue()`

---



## 🚀 Recommendations

* Use `showResultDialog()` in `onGameOver()`
* Use `showRetryDialog()` after each wrong answer attempt
* Use `showCorrectAnswerDialog()` on final failure
* Use `showNextDialog()` after correct answer

