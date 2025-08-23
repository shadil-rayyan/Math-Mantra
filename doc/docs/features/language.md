

### 📁 `docs/settings/language.md`

````md
# 🌐 Language Selection

This app supports multiple languages — users can switch them anytime from the Settings screen.

---

## ✅ Supported Languages

| Display Label     | Code     |
|-------------------|----------|
| System Default    | default  |
| English           | en       |
| Malayalam         | ml       |

> ✅ You can easily add more by updating the language arrays and translation files.

---

## 🧠 How It Works

Language settings are handled by a helper class: `LocaleHelper.kt`.

When the user selects a language from the dropdown:

1. The app **stores the language code** in SharedPreferences.
2. Then calls:
   ```kotlin
   LocaleHelper.setLocale(context, langCode)
````

3. Finally, the activity is **restarted** using:

   ```kotlin
   requireActivity().recreate()
   ```

   to apply the new language.

---

## 🔁 Persistence

The selected language is saved using this key in preferences:

```text
Locale.Helper.Selected.Language
```

If the user selects **“System Default”**, the preference is cleared and the app uses the phone’s current system language.

---

## 🧑‍🎓 How to Add a New Language

1. Create a new folder in `res/` with the locale code. For example:

   ```
   res/values-hi/strings.xml   // Hindi
   res/values-ta/strings.xml   // Tamil
   ```

2. Add your translated strings in that `strings.xml`.

3. Update:

    * `res/values/strings.xml` → Add to `language_levels` string array.
    * `SettingFragment.kt` → Add the new code to `languageCodeMap`.

Example:

```kotlin
private val languageCodeMap = mapOf(
    0 to "default",
    1 to "en",
    2 to "ml",
    3 to "hi"  // Hindi
)
```

---

## 🗂 Related Code

* `SettingFragment.kt` → `setupLanguageSpinner()`
* `utility/settings/LocaleHelper.kt`
* `res/values/strings.xml` → `language_levels` array

