
# 🧭 Compass Game Mode (Developer Docs)

The **CompassFragment** implements the **Compass Game Mode** in zMantra.  
This mode challenges players to physically rotate their device toward a target **compass direction** (e.g., *North, South-East, West*).  
It integrates Android’s **accelerometer** and **magnetometer** sensors, custom accessibility announcements, and real-time feedback.  

---

## 📂 Location
`com.zendalona.zmantra.presentation.features.game.compass.CompassFragment`

---

## 🔑 Core Responsibilities
1. **Sensor Management**  
   - Uses `SensorManager` to register the **Accelerometer** and **Magnetometer**.  
   - Calculates device orientation with `SensorManager.getRotationMatrix()`.  
   - Converts orientation to **azimuth (0–360°)**.  

2. **Game Question Flow**  
   - Loads compass questions (`GameQuestion` objects).  
   - Each question specifies a **target direction** (e.g., "North").  
   - Players must rotate the device and hold it in the target direction for 3 seconds.  

3. **Accessibility & Feedback**  
   - Announces each new question with **TalkBack / TTS**.  
   - Periodically announces the **current facing direction** (every 10 seconds).  
   - Provides spoken and visual feedback on correct orientation.  

4. **Game Result**  
   - Calculates grade based on **time taken vs question’s time limit**.  
   - Shows result dialog, then proceeds to the next question.  

---

## ⚙️ Key Fields
| Variable | Type | Purpose |
|----------|------|---------|
| `sensorManager` | `SensorManager` | Access to device sensors. |
| `magnetometer`, `accelerometer` | `Sensor?` | Required sensors for orientation. |
| `rotationMatrix`, `orientation` | `FloatArray` | Store orientation math results. |
| `currentAzimuth` | `Float` | Current compass azimuth in degrees. |
| `targetDirection` | `Float` | Target direction in degrees (from question). |
| `questions` | `MutableList<GameQuestion>` | Loaded compass questions. |
| `holdHandler` | `Handler` | Used to check if user holds correct direction for 3s. |
| `directionAnnounceHandler` | `Handler` | Announces direction every 10s for accessibility. |

---

## 🔄 Lifecycle
1. **onAttach()** → Initialize `SensorManager`, get sensors.  
2. **onCreateView()** → Inflate layout, load compass directions from `strings.xml`.  
3. **onViewCreated()** → Announce "Turn to ..." instruction.  
4. **onQuestionsLoaded()** → Populate questions & call `generateNewQuestion()`.  
5. **generateNewQuestion()** →  
   - Set new `targetDirection`.  
   - Announce question.  
   - Start periodic announcements.  
6. **onSensorChanged()** →  
   - Collect accelerometer & magnetometer values.  
   - Compute azimuth.  
   - Call `updateCompassUI()`.  
7. **updateCompassUI()** → Update rotation + check if holding correct direction.  
8. **checkIfHoldingCorrectDirection()** →  
   - If user holds within ±22.5° of target for 3s → trigger success.  

---

## 📐 Orientation Logic
```kotlin
SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)
SensorManager.getOrientation(rotationMatrix, orientation)

val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
val azimuthFixed = (azimuth + 360) % 360
````

* `azimuth` → Raw compass heading.
* `azimuthFixed` → Normalized value (0–360°).

---

## 🎧 Accessibility

* **Announcements**:

  * At question start → `"Turn to North"`
  * Every 10s → `"Currently facing East"`
* **TalkBack users** can still navigate UI, but spoken feedback is automatic.
* This ensures **blind learners** can orient their device correctly.

---

## 🧪 Testing Notes

* Test in environments **without strong magnetic interference**.
* Validate with both **sighted mode** (UI compass rotates) and **TalkBack mode** (spoken feedback only).
* Edge cases:

  * Missing sensors → show `"No compass questions found"` and end game.
  * Holding at borderline angle (22.5° threshold).

---


