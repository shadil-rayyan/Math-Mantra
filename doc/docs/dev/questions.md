

# Writing Custom Questions

Questions come from Excel per language: `app/src/main/assets/questions/{lang}.xlsx` (e.g., `en.xlsx`, `ml.xlsx`).

## Columns (per row)

* **C0**: Question template (e.g., `What is {a} + {b}?`)
* **C1**: Mode (must match fragment’s [`getModeName()`](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:230:4-230:48), e.g., `tap`, `shake`)
* **C2**: Operands spec (see Grammar)
* **C3**: Difficulty (`"1"`..`"5"`)
* **C4**: Answer expression (e.g., `{a}+{b}`)
* **C5**: Time limit (optional; default 20)

## Operand Grammar

* **List**: `1,2,5` → randomly pick one
* **Range**: `5:10` → random int inclusive
* **Product pair**: `2,3;3:5` → pick left and right, multiply
* **Non-numeric modes** (e.g., `direction`, `drawing`): literals allowed; UI handles evaluation

---

## Examples

### Numeric (math-based)

* **Addition (difficulty 1)**

  * C0: `What is {a} + {b}?`
  * C1: `tap`
  * C2: `1:5,1:5`
  * C3: `1`
  * C4: `{a}+{b}`
  * C5: `20`

* **Applied word problems (difficulty 1, quickplay mode)**

  * C0: `Tom walks {a} km and then walks {b} km more. How far did he walk in total?`

  * C1: `quickplay`

  * C2: `1:5,1:4`

  * C3: `1`

  * C4: `{a}+{b}`

  * C5: `30`

  * C0: `Sam has {a} pencils and finds {b} more. How many pencils does he have now?`

  * C1: `quickplay`

  * C2: `1:9,1:9`

  * C3: `1`

  * C4: `{a}+{b}`

  * C5: `30`

  * C0: `Leo distributes {a} puzzle pieces in boxes of {b}. Remaining pieces?`

  * C1: `quickplay`

  * C2: `10:50,2:5`

  * C3: `1`

  * C4: `{a}%{b}`

  * C5: `30`

### Non-numeric (instructional)

* **Angle instruction**

  * C0: `Rotate to {dir}`
  * C1: `angle`
  * C2: `left,right,up,down`
  * C3: `2`
  * C4: `0` (unused)
  * C5: `20`

---

## Tips

* Ensure **Mode names** exactly match your fragment’s `getModeName()`.
* Keep difficulties balanced; splash preloading hits current difficulty first.
* Validate answer expressions with **exp4j** (avoid divide-by-zero).

---

## Localization

Add the same rows per language file (e.g., `en.xlsx`, `ml.xlsx`) to localize content.

