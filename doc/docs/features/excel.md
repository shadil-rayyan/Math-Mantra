Here’s a single polished **developer documentation page** for your `ExcelQuestionLoader.kt` logic. It’s structured in a clean MkDocs format with clear sections: overview, features, usage, flow diagram, and example input.

---

### 📁 `docs/architecture/data/excel-loader.md`

````md
# 📊 ExcelQuestionLoader

The `ExcelQuestionLoader` is a utility object responsible for dynamically generating questions from Excel spreadsheets at runtime. It supports customizable operands, templated questions, and on-the-fly answer evaluation using algebraic expressions.

This powers many game modes, including **Tap**, **Shake**, **Draw**, and **Direction** modes.

---

## ✅ Key Responsibilities

- Load and parse `.xlsx` files from `assets/questions/`.
- Filter questions based on:
  - Game mode (`tap`, `shake`, `drawing`, `direction`, etc.)
  - Difficulty level (`1`, `2`, `3`...)
- Replace operand variables in question templates.
- Evaluate mathematical expressions using `exp4j`.

---

## 🧩 Excel Sheet Format

| Column Index | Field             | Description                                     |
|--------------|------------------|-------------------------------------------------|
| 0            | Template          | Templated question with `{x}` placeholders      |
| 1            | Mode              | Game mode (e.g. tap, shake, drawing)            |
| 2            | Operands          | Operand values, can include `:`, `,`, or `;`    |
| 3            | Difficulty        | Integer from 1–3                                |
| 4            | Answer Expression | e.g., `{x} + {y}` (evaluated with `exp4j`)      |
| 5            | Time Limit        | Time in seconds                                 |

---

## 🔢 Operand Syntax Reference

- `3,5,8` → Pick random from list
- `10:15` → Random int in range (inclusive)
- `3;4` → Multiply: `3 * 4 = 12`
- `A3*B2*` → Variable extraction pattern

---

## 🧠 How It Works

```kotlin
val questions = ExcelQuestionLoader.loadQuestionsFromExcel(
    context, lang = "en", mode = "tap", difficulty = "2"
)
````

### 📦 Internals

| Function                   | Purpose                                                                    |
| -------------------------- | -------------------------------------------------------------------------- |
| `loadQuestionsFromExcel()` | Main entry. Reads `.xlsx` file from assets and returns a filtered list.    |
| `loadQuestionsFromSheet()` | Filters rows by mode and difficulty, processes each into a `GameQuestion`. |
| `extractVariables()`       | Detects `{x}`, `{y}`, etc. used in operand fields.                         |
| `parseInputRange()`        | Converts operand syntax (`:`, `,`, `;`) into concrete values.              |
| `replaceVariables()`       | Replaces `{x}` with generated values in the template and answer formula.   |
| `evaluateEquation()`       | Uses `exp4j` to compute the correct answer if required.                    |

---

## 🧪 Example

| Field    | Value              |
| -------- | ------------------ |
| Template | What is {x} + {y}? |
| Operands | 1:5*6:9*           |
| Answer   | {x} + {y}          |

✅ Output:

* `x = 3`, `y = 7`
* Rendered Q: `What is 3 + 7?`
* Answer: `10`

---

## ⚙️ Used In

* `BaseGameFragment` (question loading)
* Splash Screen (preload & cache)
* Any game mode that depends on dynamic Excel-driven question generation

---

## 📚 Related Files

* `BaseGameFragment.kt`
* `GameQuestion.kt`
* `questions/en.xlsx`
* `libs/exp4j`

---

## 🧼 Notes

* All parsing is done safely with `try/catch`, so malformed rows are ignored.
* Randomized values make question generation feel varied even with static templates.
* Can be extended in the future to support image-based or audio-based Excel rows.

```

---

Let me know if you'd like a second version in Malayalam or if you want this embedded into your existing MkDocs `mkdocs.yml` structure.
```
