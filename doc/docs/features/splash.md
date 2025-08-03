Here’s a developer-friendly doc section for your `SplashScreen` logic, ideal for inclusion in your MkDocs developer docs:

---

### 📁 `docs/app/splash-screen.md`

````md
# 🚀 Splash Screen

The splash screen shows a welcome GIF and preloads all required questions before opening the main UI.

This improves performance and ensures that the first game interaction is instant, even with accessibility features.

---

## 🎯 Purpose

- ✅ Preload **questions** for the selected difficulty level.
- ✅ Announce progress if **Accessibility/TalkBack** is on.
- ✅ Display a loading **GIF** with a **progress bar**.

---

## 🧪 Key Behaviors

### 1. 🧠 Question Preloading

We preload questions **based on the currently selected difficulty**. This ensures no lag when users start playing.

```kotlin
QuestionCache.preloadCurrentDifficultyModes(context, lang) { progress ->
    progressBar.setProgress(progress, true)
}
````

After launching `MainActivity`, we also **preload other difficulties in the background**:

```kotlin
QuestionCache.preloadOtherDifficultyModes(context, lang)
```

---

### 2. ♿ Accessibility Announcements

If TalkBack is enabled, the app **announces loading status** every 1.5 seconds:

```kotlin
gifImageView.announceForAccessibility("Loading questions, please wait")
```

We use a repeating `Handler` postDelayed loop to announce periodically.

---

### 3. 🎞️ Animated Welcome + Progress

We use Glide to display a welcoming GIF while the user waits:

```kotlin
Glide.with(this)
    .asGif()
    .load(R.drawable.dialog_welcome_1)
    .into(gifImageView)
```

The progress is shown via a Material `LinearProgressIndicator`.

---

## 🧩 Related Files

| File                         | Purpose                                   |
| ---------------------------- | ----------------------------------------- |
| `SplashScreen.kt`            | Entry point activity; does all preloading |
| `QuestionCache.kt`           | Handles Excel-based question preloading   |
| `LocaleHelper.kt`            | Detects current language preference       |
| `activity_splash_screen.xml` | Layout with GIF + progress bar            |

---

## 🏁 Launch Flow

The splash screen is marked as the `LAUNCHER` activity in `AndroidManifest.xml`:

```xml
<activity
    android:name=".SplashScreen"
    android:exported="true"
    android:theme="@style/Theme.ZMantra"
    android:label="@string/app_name">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

After preloading, it launches `MainActivity`.

---

## 📦 Permissions Required

In `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.VIBRATE" />
```

These are used later during in-game audio and vibration feedback.

---

## 🧠 Pro Tip for Devs

To **simulate slow devices**, use `adb shell am start -S` to repeatedly start the splash and test performance:

```bash
adb shell am start -n com.zendalona.zmantra/.SplashScreen
```

---

```

Let me know if you'd like a visual flowchart or YAML metadata (`mkdocs.yml`) update.
```
