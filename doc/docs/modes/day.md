
# 📅 Day Game Mode (Developer Docs)

The **DayFragment** implements the **Day Game Mode** in zMantra.  
This mode challenges players to calculate the **day of the week after a given offset**.  
For example: *"If today is Monday, what day will it be after 3 days?"*  

It provides a **multiple-choice button interface** (Monday–Sunday) and is fully accessible with TalkBack.  

---

## 📂 Location
`com.zendalona.zmantra.presentation.features.game.day.DayFragment`

---

## 🔑 Core Responsibilities
1. **Question Generation**  
   - Randomly selects a **start day** (Monday–Sunday).  
   - Applies the **offset (operand)** from `GameQuestion.answer`.  
   - Calculates the **correct day** using modulo arithmetic.  

2. **User Interaction**  
   - Renders **7 buttons** (Monday–Sunday).  
   - Player selects a button to answer.  
   - Handles correct/incorrect answers with attempt tracking.  

3. **Accessibility**  
   - Announces each new question (e.g., *"If today is Tuesday, what day will it be after 5 days?"*).  
   - Ensures first question text receives focus for TalkBack users.  

4. **Game Flow**  
   - Sequentially iterates through provided `GameQuestion`s.  
   - Tracks elapsed time per question.  
   - Limits incorrect attempts to 3 before showing the correct answer.  

---

## ⚙️ Key Fields
| Variable | Type | Purpose |
|----------|------|---------|
| `_binding` / `binding` | `FragmentGameDayBinding` | View binding for layout. |
| `days` | `List<String>` | Ordered list of weekdays. |
| `buttons` | `List<Button>` | References to 7 day buttons. |
| `correctDay` | `String` | Correct answer for current question. |
| `questionStartTime` | `Long` | Timestamp when question started. |
| `totalTime` | `Double` | Time limit for answering (30s). |
| `isFirstQuestion` | `Boolean` | Ensures TalkBack focus only for first question. |
| `questions` | `List<GameQuestion>` | List of all questions in this game mode. |
| `questionIndex` | `Int` | Current index in questions list. |

---

## 🔄 Lifecycle
1. **onCreateView()**  
   - Inflate layout and map weekday buttons.  

2. **onViewCreated()**  
   - Assigns click listeners for all 7 buttons → calls `checkAnswer()`.  

3. **onDestroyView()**  
   - Cleans up binding to prevent memory leaks.  

4. **onQuestionsLoaded()**  
   - Loads all `GameQuestion`s.  
   - If empty → shows "No questions available".  
   - Otherwise → starts with `generateQuestion()`.  

---

## 📝 Question Generation
```kotlin
val correctIndex = (startDayIndex + operand) % 7
correctDay = days[correctIndex]
````

* Random **start day** chosen.
* Operand (from question’s `answer`) applied as an offset.
* Uses modulo `% 7` to wrap around the week.
* Sets `correctDay`.

Example:

* Start = *Friday*, Operand = 3 → Answer = *Monday*.

---

## ✅ Answer Validation

* **checkAnswer(selected: String)**:

  * Compares `selected` (button text) to `correctDay`.
  * Tracks `elapsedTime`.
  * **Correct answer** → disables buttons, moves to next question.
  * **Incorrect answer** → increments `attemptCount`.

    * If attempts ≥ 3 → shows correct answer, disables buttons, moves to next question.
    * Else → allows retry.

---

## 🎧 Accessibility

* Announces question text aloud for TalkBack.
* Example: *"If today is Thursday, what day will it be after 2 days?"*
* Ensures **focus** on first question text for screen reader users.

---

## 🧪 Testing Notes

* Validate **day offset calculation** (including wrap-around, e.g., *Saturday + 3 = Tuesday*).
* Ensure **attempt limit (3)** works correctly.
* Check **time tracking** (30s limit passed to `handleAnswerSubmission`).
* Verify **accessibility announcements**.
* Confirm **buttons disable** after correct/incorrect completion.




