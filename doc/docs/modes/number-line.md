
# 📏 Number Line Game Mode (Developer Docs)

The **Number Line Game** is an interactive mode in zMantra where learners move along a number line to reach a target position.  
It supports **spatial number sense development**, **incremental navigation**, and **accessibility via TalkBack**.

---

## 📂 Location
- `com.zendalona.zmantra.presentation.features.game.numberline.NumberLineFragment`
- `com.zendalona.zmantra.presentation.features.game.numberline.NumberLineViewModel`
- `com.zendalona.zmantra.presentation.features.game.numberline.customView.NumberLineView`

---

## 🔑 Core Responsibilities
1. **NumberLineFragment**  
   - Hosts UI for the number line task.  
   - Presents questions (e.g., *"Move to 3"*).  
   - Manages navigation via left/right buttons.  
   - Observes state from `NumberLineViewModel`.  
   - Triggers accessibility announcements.  
   - Validates when the learner reaches the correct answer.

2. **NumberLineViewModel**  
   - Holds number line state (`start`, `end`, `currentPosition`).  
   - Provides methods to move left/right and shift the visible range when boundaries are reached.  
   - Encapsulates logic for updating LiveData to drive the UI.

3. **NumberLineView (CustomView)**  
   - Draws the **line, numbers, and mascot emoji**.  
   - Updates dynamically as learner moves.  
   - Sends **custom accessibility descriptions** of the line and position.  
   - Handles TalkBack focus events with announcements.

---

## ⚙️ Fragment Fields
| Variable | Type | Purpose |
|----------|------|---------|
| `questions` | `List<GameQuestion>` | Sequence of tasks (`"Move to X"`, `answer=X`). |
| `currentIndex` | `Int` | Index of current question. |
| `answer` | `Int` | Target position for current question. |
| `questionDesc` | `String` | Human-readable description. |
| `questionStartTime` | `Long` | Timestamp for timing responses. |
| `isFirstQuestion` | `Boolean` | Ensures focus is set on first question only. |
| `currentPosLabel` | `String` | String resource for "Current position". |
| `answerCheckRunnable` | `Runnable?` | Delayed check of correctness after movement. |

---

## 🔄 Flow

### 1. Setup
- `onCreate()` → initialize `NumberLineViewModel` and set speech rate.  
- `onCreateView()` → bind UI, attach click listeners, set up LiveData observers.  

### 2. Load Questions
- `onQuestionsLoaded()` → fallback default if none provided (`"Move to 3"`).  
- Calls `askNextQuestion()`.  

### 3. Ask Question
- Updates `questionDesc` + `answer`.  
- Displays in `numberLineQuestion`.  
- Posts **accessibility announcement**:  
  *"Move to 3"*.  
- On first question, auto-focuses question text.

### 4. Move on Number Line
- **Left/Right buttons** → `viewModel.moveLeft()` / `viewModel.moveRight()`.  
- If end of range reached → range **shifts** (e.g., -5..5 → 6..16).  
- Observers redraw `NumberLineView` and update `currentPositionTv`.  

### 5. Check Answer
- After each move, `answerCheckRunnable` is delayed (2s).  
- If current position == target → `getGrade()` and `showResultDialog()`.  
- Otherwise, learner continues adjusting until correct.

### 6. Next Question
- After correct answer, `askNextQuestion()` loads the next one.  
- Ends game when all questions are complete.

---

## 🎨 NumberLineView (CustomView)
- Draws:
  - **Horizontal line** across screen.
  - **Tick marks/numbers** (`start → end`).  
  - **Mascot emoji 👨‍🦽** above the current position.  
- Accessibility:
  - Announces **current position** when updated.  
  - Custom `AccessibilityDelegateCompat` → announces position when view gains focus.  
  - Overrides `onInitializeAccessibilityNodeInfo()` with custom description:  
    *"Number line from -5 to 5, current position is 0"*.

---

## 📡 ViewModel Responsibilities
- State management with LiveData:
  - `lineStart`, `lineEnd`, `currentPosition`.  
- Movement:
  - **Right:** `current++` or shift range right.  
  - **Left:** `current--` or shift range left.  
- Shifting:
  - Maintains window size of 10 numbers.  
  - Shifts in chunks (`-5..5 → 6..16`).  
- Logs for debugging (`Log.d`).  

---

## 🎧 Accessibility Features
- **Question**: spoken via TTS.  
- **Number line**: announces *"Current position is X"* on updates.  
- **Focus events**: auto-announce when question or number line receives TalkBack focus.  
- **Custom content descriptions** for TalkBack context.  

---

## 🧪 Testing Notes
1. **UI**  
   - Verify line, numbers, and mascot render correctly at all ranges.  
   - Ensure shifting left/right updates range smoothly.  
2. **Accessibility**  
   - TalkBack announces questions and positions.  
   - Focus on `numberLineView` announces correct content.  
   - Position updates trigger announcements.  
3. **Game Flow**  
   - Correct position → result dialog → next question.  
   - Incorrect position → stays until corrected.  
4. **Performance**  
   - Verify handler callbacks cancel properly in `onPause()`.  
   - No memory leaks from binding (`binding=null` in `onDestroyView()`).  




