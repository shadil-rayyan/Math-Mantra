
# 📐 Angle Game Mode (Developer Docs)

The **AngleFragment** implements the **Angle Game Mode** in zMantra.  
This mode challenges players to **rotate their phone** to achieve a target **relative angle**.  
It integrates the **Rotation Sensor Utility**, accessibility announcements, and validation logic to provide an interactive experience for both sighted and TalkBack users.  

---

## 📂 Location
`com.zendalona.zmantra.presentation.features.game.angle.AngleFragment`

---

## 🔑 Core Responsibilities
1. **Sensor Management**  
   - Uses a `RotationSensorUtility` to capture **azimuth, pitch, roll** from the device sensors.  
   - Tracks the **base azimuth** (starting orientation).  
   - Calculates the **relative rotation** of the device in degrees.  

2. **Game Question Flow**  
   - Each `GameQuestion` provides a **target angle** (answer).  
   - Player must rotate their phone until the **relative angle** matches the target (±5° tolerance).  
   - If held for 5 seconds, the question is marked as answered and a grade is assigned.  

3. **Accessibility & Feedback**  
   - Announces each new question text (e.g., *"Rotate your phone by 45°"*).  
   - Every 3 seconds, if TalkBack is active, announces the **current rotation angle**.  
   - This ensures **blind users** receive continuous audio feedback.  

4. **Game Result**  
   - Uses `getGrade()` with fixed parameters (`elapsedTime = 1.0`, `limit = 10.0`).  
   - Shows a result dialog after each question.  

---

## ⚙️ Key Fields
| Variable | Type | Purpose |
|----------|------|---------|
| `_binding` / `binding` | `FragmentGameAngleBinding` | View binding for layout. |
| `rotationSensorUtility` | `RotationSensorUtility` | Handles rotation sensor updates. |
| `targetRotation` | `Float` | Target angle (from question). |
| `baseAzimuth` | `Float` | Reference orientation (initial azimuth). |
| `questionAnswered` | `Boolean` | Prevents re-validation after answering. |
| `currentIndex` | `Int` | Index of the current question. |
| `isFirstQuestion` | `Boolean` | Ensures focus only for the first question. |
| `angleUpdateHandler` | `Handler` | Manages repeated announcements. |
| `angleUpdateRunnable` | `Runnable?` | Announces current angle every 3s. |
| `holdRunnable` | `Runnable?` | Checks if angle is held for 5s. |
| `isHolding` | `Boolean` | Tracks whether device is held at correct angle. |

---

## 🔄 Lifecycle
1. **onCreateView()**  
   - Inflate layout, initialize `RotationSensorUtility`.  
   - Set up `RotationListener` → calls `handleRotationChange()`.  

2. **onStart()**  
   - Registers sensor listener.  

3. **onStop() / onDestroyView()**  
   - Unregisters sensor listener.  
   - Removes callbacks to prevent memory leaks.  

4. **onQuestionsLoaded()**  
   - Resets state.  
   - Shows first question (or no-questions message).  

5. **showNextQuestion()**  
   - Loads next `GameQuestion`.  
   - Updates `targetRotation`.  
   - Announces question text.  
   - Starts recurring **accessibility announcements** of current angle.  

6. **handleRotationChange()**  
   - Sets `baseAzimuth` if unset.  
   - Calculates **relative angle** from azimuth difference.  
   - Updates UI (`rotationAngleText`).  
   - Calls `validateAngle()`.  

7. **validateAngle()**  
   - Checks if current angle ≈ `targetRotation` (±5°).  
   - If within range, start **5-second hold timer**.  
   - If held successfully → mark answered, grade, show result.  
   - If moved away → reset hold state.  

---

## 📐 Rotation Logic
```kotlin
val relativeAzimuth = (azimuth - baseAzimuth + 360) % 360
binding.rotationAngleText.text =
    getString(R.string.relative_angle_template, relativeAzimuth.toInt())
````

* `baseAzimuth`: The initial device orientation when question starts.
* `relativeAzimuth`: Normalized rotation difference (0–360°).
* UI displays angle, TalkBack announces updates.

---

## 🎧 Accessibility

* At question start → *"Rotate your phone by X°"*.
* Every 3 seconds → *"Current angle: Y°"*.
* Focus request on first question ensures TalkBack reads aloud automatically.
* Uses `AccessibilityUtils().isSystemExploreByTouchEnabled()` to detect TalkBack.

---

## 🧪 Testing Notes

* **Angle tolerance**: ±5° around target.
* **Hold duration**: 5 seconds (can be adjusted).
* Edge cases:

  * No questions → "No questions available".
  * Device rotation sensors unavailable → test fallback.
  * Reset when leaving fragment (lifecycle cleanup).

---


