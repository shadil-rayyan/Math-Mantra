# ✏️ Drawing Game Mode (Developer Docs)

The **DrawingFragment** implements the **Drawing Game Mode** in zMantra.  
This mode challenges learners to **draw specific shapes** (e.g., circle, square, triangle) on a custom canvas.  
It is designed to be **hands-on and exploratory**, allowing learners to practice geometric understanding through free drawing.  

---

## 📂 Location
`com.zendalona.zmantra.presentation.features.game.drawing.DrawingFragment`

---

## 🔑 Core Responsibilities
1. **Render Custom Drawing Canvas**  
   - Uses `DrawingView` (a custom `View`) for capturing freehand drawing.  
   - Clears and resets canvas when needed.  

2. **Load & Present Questions**  
   - Reads questions from `GameQuestion` list.  
   - Each question provides a shape name (`expression`).  
   - Generates accessible instructions (e.g., *"Draw a Circle"*).  

3. **User Interaction**  
   - **Reset button** → clears canvas.  
   - **Submit button** → moves to next question.  

4. **Accessibility Support**  
   - Announces task instructions using TalkBack.  
   - Announces canvas reset action.  
   - Announces movement to next question.  

5. **Game Flow**  
   - Sequentially cycles through `GameQuestion`s.  
   - Ends session with completion announcement → auto-navigates back after delay.  

---

## ⚙️ Key Fields
| Variable | Type | Purpose |
|----------|------|---------|
| `_binding` / `binding` | `FragmentGameDrawingBinding` | View binding for layout. |
| `drawingView` | `DrawingView?` | Custom canvas for drawing shapes. |
| `isFirstQuestion` | `Boolean` | Ensures first question text gets TalkBack focus. |
| `currentQuestion` | `GameQuestion?` | Holds currently active question. |
| `currentIndex` | `Int` | Tracks which question index is being shown. |
| `questions` | `List<GameQuestion>` | All questions for this mode. |

---

## 🔄 Lifecycle
1. **onCreateView()**  
   - Inflates layout and inserts `DrawingView` into container.  

2. **onViewCreated()**  
   - Sets listeners for Reset & Submit buttons.  

3. **onQuestionsLoaded()**  
   - Loads all available questions.  
   - If empty → disables buttons & shows "No questions available".  
   - Otherwise → starts with `loadQuestionAt(0)`.  

4. **loadQuestionAt(index: Int, questions: List<GameQuestion>)**  
   - Prepares instruction (e.g., *"Draw a Square"*).  
   - Updates UI and accessibility announcements.  
   - Clears canvas.  
   - Resets `attemptCount`.  
   - If no more questions → announces "Task completed" and exits fragment.  

5. **checkAnswer()**  
   - Currently a **placeholder**.  
   - Shows "Moving to next question" message.  
   - Advances to the next question.  
   - In the future → can be extended for **automatic drawing recognition**.  

6. **onResume()/onPause()**  
   - Forwards lifecycle to `DrawingView` for any resource handling.  

---

## 🎨 Drawing Logic
- `DrawingView` handles all drawing gestures (not included here, but assumed to provide: `clearCanvas()`, `onResume()`, `onPause()`).  
- User input is **not validated against shapes** yet — currently free drawing only.  

---

## 🎧 Accessibility
- Announces drawing tasks (e.g., *"Please draw a triangle"*).  
- Announces canvas reset (e.g., *"Canvas cleared"*).  
- Announces navigation to next question.  
- Ensures focus is given to first question text for TalkBack.  

---

## 🧪 Testing Notes
- Verify **Reset button** clears canvas properly.  
- Confirm **Submit button** moves to next question.  
- Validate **completion behavior**:  
  - At end of questions → "Task completed" announcement.  
  - Navigates back after 3 seconds.  
- Test **TalkBack focus** on first question.  
- Check **canvas usability** across devices and screen densities.  

---
