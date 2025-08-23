
### 📁 `docs/settings/difficulty.md`

````md
# 🧠 Difficulty Setting

Users can choose their preferred difficulty level, which directly affects the types of questions they receive in the game.

---

## 🎯 Purpose

Allow users to play at a level that suits their skills:

- **SIMPLE** – For beginners or kids.
- **CHALLENGING** – For advanced learners looking for tough problems.

---

## 🧑‍🏫 Available Levels

Defined in the `Difficulty` enum:

- `SIMPLE`
- `EASY`
- `MEDIUM`
- `HARD`
- `CHALLENGING`

These levels can be expanded or renamed as needed.

---

## 🧠 How It Works

1. When the user selects a level, it’s saved using:

   ```kotlin
   DifficultyPreferences.setDifficulty(context, level)
````

2. To retrieve the current level anywhere in the app:

   ```kotlin
   val level = DifficultyPreferences.getDifficulty(context)
   ```

3. Game fragments (like Tap Mode, Shake Mode) use this level to load questions dynamically based on difficulty.

---

## 🔁 Persistence

The selected difficulty is stored in `SharedPreferences` under this key:

```text
pref_difficulty_level
```

---

## 🗂 Related Code

* `SettingFragment.kt` → `setupDifficultyRadioButtons()`
* `utility/settings/DifficultyPreferences.kt`
* `model/Difficulty.kt` → defines the enum values

```
