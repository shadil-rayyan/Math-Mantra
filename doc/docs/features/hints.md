


# 💡 Hint System

The Hint System provides users with **contextual assistance** for each game mode — helpful especially for new or visually impaired users.

## 🎯 Purpose

- Allow users to access hints when they’re stuck.
- Offer accessibility-aware guidance (via TalkBack or custom service).
- Load hints dynamically based on the current game **mode** and **language**.

---

## 🧩 How It Works

### 1. 👆 Hint Button in Toolbar

The top-right **"hint" icon** is controlled by the current fragment. Each screen decides whether the icon is shown using:

```kotlin
interface HintIconVisibilityController {
    fun shouldShowHintIcon(): Boolean
}
````

### 2. 💬 Triggering the Hint

When the hint icon is tapped:

```kotlin
interface Hintable {
    fun showHint()
}
```

* If the fragment implements `Hintable`, it displays the hint.
* If not, `MainActivity` loads a fallback `HintFragment` using a default text file.

---

## 📂 Hint Content Source

### ✅ Excel-Based Hint Lookup

Hints are stored in localized Excel files located at:

```
assets/hint/{language}.xlsx
```

### 🧠 Code: `ExcelHintReader`

```java
public static String getHintFromExcel(Context context, String language, String mode)
```

* Reads `mode` and `hint` columns from the Excel file.
* Matches the current mode to return the correct hint.

### ✅ Fallback Example

If no hint is found for the mode, or the Excel file fails to load, the fallback path is:

```kotlin
"en/hint/default.txt"
```

---

## 🧪 Example Hint File (`en.xlsx`)

| Mode       | Hint Text                              |
| ---------- | -------------------------------------- |
| tap        | Tap the correct answer to continue.    |
| shake      | Shake the device when the answer is X. |
| numberline | Drag the marker to the correct value.  |

> These Excel files are stored in the `assets/hint/` folder. They are easy to localize.

---

## 🔗 Related Classes

| File                                       | Purpose                                        |
| ------------------------------------------ | ---------------------------------------------- |
| `MainActivity.kt`                          | Loads the toolbar, handles hint menu logic.    |
| `Hintable`, `HintIconVisibilityController` | Interfaces for fragment-based hint control.    |
| `HintFragment.kt`                          | Fallback fragment to show default hint file.   |
| `ExcelHintReader.java`                     | Reads localized hint from Excel based on mode. |

---

## ♿ Accessibility Considerations

* Hint content is **spoken aloud** using TTS when shown.
* Compatible with **TalkBack** and custom accessibility service.
* Ensures users with visual impairments receive the same guidance.

---

## 🧑‍💻 Developer Tips

* Add a new hint? Update the corresponding `.xlsx` file in `assets/hint/`.
* For a new game mode, ensure the mode string matches the one passed to `getHintFromExcel(...)`.
* To hide the hint icon for a fragment, return `false` from `shouldShowHintIcon()`.

---

## 🛠 Sample Code Snippets

### Show Hint from Fragment

```kotlin
override fun showHint() {
    val hintText = ExcelHintReader.getHintFromExcel(requireContext(), "en", "tap")
    DialogUtils.showInlineResult(requireActivity(), "Hint", hintText, R.drawable.hint)
}


