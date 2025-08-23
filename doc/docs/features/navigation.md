

# 🔙 Back Navigation

## Overview

Back navigation in **zMantra** follows a **single-activity, fragment-back-stack** approach.

* All forward navigations push the fragment onto the back stack.
* Pressing **Back** pops the stack until the root (e.g., Landing page).
* When no fragments remain, the app exits.

**Back can be triggered via:**

* System **Back button** / gesture
* Toolbar **Up arrow**
* In-UI “Go Back” buttons
* Programmatic back after certain flows

---

## How It’s Wired

### Main Activity toolbar + Up navigation

**File:** `app/src/main/java/com/zendalona/zmantra/MainActivity.kt`

* Listens for back stack changes to toggle the toolbar Up arrow:

```kotlin
supportFragmentManager.addOnBackStackChangedListener {
    val canGoBack = supportFragmentManager.backStackEntryCount > 0
    supportActionBar?.setDisplayHomeAsUpEnabled(canGoBack)
}
```

* Delegates toolbar **Up** → **Back**:

```kotlin
override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
}
```

### Navigating forward

Always use the `MainActivity` helper to ensure proper back stack behavior:

```kotlin
(requireActivity() as MainActivity)
    .loadFragment(MyNextFragment(), MainActivity.TRANSIT_FRAGMENT_OPEN)
```

This applies:
`.replace(...).addToBackStack(null).commit()`

---

## Default Back behavior

* Pops the **current fragment** from the stack.
* If stack is empty (e.g., `LandingPageFragment`), **Back exits the app**.

---

## Fragment-Specific Handling

### Score result screen

**File:** `core/utility/common/ScorePageFragment.kt`

* Intercepts Back with a lifecycle-aware callback:

```kotlin
requireActivity().onBackPressedDispatcher.addCallback(
    this,
    object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            goBack() // cleanup + pop
        }
    }
)
```

* Also handles **toolbar Home (Up)** in:

```kotlin
override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
        goBack()
        return true
    }
    return super.onOptionsItemSelected(item)
}
```

### Hint & User Guide screens

**Files:**

* `presentation/features/hint/HintFragment.kt`
* `presentation/features/userguide/UserGuideFragment.kt`

In-UI **“Go Back”** button →

```kotlin
requireActivity().onBackPressedDispatcher.onBackPressed()
```

### Auto-back after completing a flow

* **Drawing game** → auto-return after announcing completion:

```kotlin
requireActivity().onBackPressedDispatcher.onBackPressed()
```

* **EndGame utility** → on score completion:

```kotlin
EndScore.endGameWithScore(... onComplete = {
    activity.onBackPressedDispatcher.onBackPressed()
})
```

---

## Accessibility

* Toolbar **Up arrow** has a spoken label:

```kotlin
supportActionBar?.setHomeActionContentDescription(R.string.back_button_label)
```

---

## Guidelines for New Screens

✅ **Always navigate via `MainActivity.loadFragment(...)`**
✅ **Register a back callback** if custom cleanup is required
✅ **Use in-UI Back buttons** sparingly, but wire them via:

```kotlin
backButton.setOnClickListener {
    requireActivity().onBackPressedDispatcher.onBackPressed()
}
```

✅ **For fragments with toolbar menus**, handle `android.R.id.home` locally

