

### 📁 `docs/settings/contrast-theme.md`

````md
# 🎨 Contrast / Theme Settings

Users can choose between **Light**, **Dark**, or **System Default** themes.  
This improves accessibility, especially for visually impaired users or those with light sensitivity.

---

## 🧑‍💻 Available Options

- **Default** – Follows system settings  
- **Black on White** – Light theme  
- **White on Black** – Dark theme  

---

## ✅ How It Works

1. The settings screen includes a group of radio buttons for theme selection.
2. When a user selects a theme, the app applies it using:

   ```kotlin
   AppCompatDelegate.setDefaultNightMode(MODE)
````

3. The app then restarts to apply the theme using `requireActivity().recreate()` or equivalent.

---

## 🔁 Persistence

* The selected mode is saved in `SharedPreferences` under the key:

  ```text
  app_contrast_mode
  ```

* Available values:

  ```kotlin
  MODE_NIGHT_NO            // Light theme
  MODE_NIGHT_YES           // Dark theme
  MODE_NIGHT_FOLLOW_SYSTEM // System default
  ```

---

## ✅ Default Behavior

If the user doesn’t make a selection, the app defaults to the system’s light/dark mode setting.

---

## 🗂 Related Code

* `SettingFragment.kt` → `setupContrastRadioButtons()`
* Usage of `AppCompatDelegate` in `ThemeUtils.kt` (if present)

```

