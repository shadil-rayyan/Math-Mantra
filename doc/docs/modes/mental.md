# 🧮 Mental Calculation Game Mode (Developer Docs)

The **MentalCalculationFragment** implements the **Mental Arithmetic Game Mode** in zMantra.  
It challenges learners to solve arithmetic problems by listening to math expressions and entering answers.  
This mode emphasizes **auditory math comprehension**, **step-by-step reading**, and **time-limited problem solving**.  

---

## 📂 Location
`com.zendalona.zmantra.presentation.features.game.mentalcalculation.MentalCalculationFragment`

---

## 🔑 Core Responsibilities
1. **Display & Read Arithmetic Problems**  
   - Math expressions are broken into **tokens** (e.g., `"12 ÷ 3 + 5"` → `"12"`, `"÷"`, `"3"`, `"+"`, `"5"`).  
   - Tokens are revealed **sequentially with delays**, supporting cognitive load management.  
   - Uses `TTSHelper` and `AccessibilityUtils` for TalkBack users.  

2. **User Answer Input**  
   - Learner enters numeric answers into `EditText` (`answerEt`).  
   - Submission via **button** or **keyboard Enter/Done action**.  

3. **Validation & Feedback**  
   - Compares learner’s answer to `correctAnswer`.  
   - Provides **toast feedback** for empty or invalid inputs.  
   - Calls `handleAnswerSubmission()` from `BaseGameFragment` to standardize scoring and flow.  

4. **Accessibility**  
   - Uses **TalkBack announcements** for token-by-token reading.  
   - Prevents unwanted auto-keyboard popup (users manually open).  
   - Keeps **clear separation of Read Question vs Answer Input focus**.  

5. **Game Flow Control**  
   - Cycles through `GameQuestion` list sequentially.  
   - Ends session when all questions are attempted (`endGame()`).  

---

## ⚙️ Key Fields
| Variable | Type | Purpose |
|----------|------|---------|
| `binding` | `FragmentGameMentalCalculationBinding?` | View binding for layout. |
| `handler` | `Handler(Looper.getMainLooper())` | Manages delayed token reveals. |
| `questions` | `List<GameQuestion>` | All math problems for this session. |
| `currentIndex` | `Int` | Current question index. |
| `correctAnswer` | `Int` | Expected numeric result of current problem. |
| `startTime` | `Long` | Timestamp when problem begins (for timing). |
| `revealTokens` | `List<String>` | Tokens of current math expression. |
| `revealIndex` | `Int` | Position of next token to reveal. |
| `isRevealing` | `Boolean` | Prevents overlapping token reveal calls. |

---

## 🔄 Lifecycle
1. **onCreateView()**  
   - Inflates layout.  
   - Sets listeners for:  
     - **Read Question** button → `onReadQuestionClicked()`.  
     - **Submit Answer** button → `checkAnswer()`.  
     - **IME Action Done/Enter key** → `checkAnswer()`.  
   - Handles focus to prevent keyboard auto-popup when toggling between Read/Answer.  

2. **onQuestionsLoaded()**  
   - Loads provided `GameQuestion`s.  
   - If none provided → fallback default (`"1 + 2"`).  
   - Calls `loadNextQuestion()`.  

3. **onReadQuestionClicked()**  
   - Splits expression into tokens (numbers and operators).  
   - Begins sequential reveal (`showToken()` every 1s).  
   - Disables button while reading to avoid re-entry.  

4. **revealNextToken() / showToken()**  
   - Displays token visually and via TalkBack (`announceForAccessibility`).  
   - Replaces `/` with `÷` for better readability.  

5. **loadNextQuestion()**  
   - Resets UI and attempts.  
   - Reads expression tokens aloud if TalkBack is active.  
   - Re-enables input after reveal delay.  

6. **checkAnswer()**  
   - Validates input from `answerEt`.  
   - If empty → prompt user with toast.  
   - If non-numeric → error toast.  
   - Else → compare against `correctAnswer`.  
   - On correct: optional **bell sound effect** if `celebration == true`.  
   - On incorrect: clear input and refocus.  
   - On show correct: skip to next.  

7. **onPause()**  
   - Stops token reveals and TTS.  

8. **onDestroyView()**  
   - Clears binding to avoid leaks.  

---

## 🎧 Accessibility
- Token-by-token **spoken announcements** for math expressions.  
- Prevents keyboard interference when toggling focus.  
- Custom TTS formatting via `TTSHelper.formatMathText()`.  
- Explicit TalkBack instructions: *"Solve 12 ÷ 3 + 5"*.  

---

## 🧪 Testing Notes
- Verify **token reveal timing** (1 second per token).  
- Confirm **Read Question button** is disabled during reveal.  
- Test **answer validation**:  
  - Empty → "Enter answer".  
  - Wrong input → "Wrong answer".  
  - Correct → proceeds to next with optional bell sound.  
- Ensure **time tracking** works: elapsed vs `timeLimit`.  
- Check **TalkBack flow**:  
  - Announces "Solve expression" at start.  
  - Announces each token sequentially.  

---

