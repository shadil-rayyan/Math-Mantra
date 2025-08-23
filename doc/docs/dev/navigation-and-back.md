# Navigation & Back Handling (Internal)

This document explains how fragment navigation and back behavior are implemented in the app. It is intended for developers working on features, bug fixes, and refactors.

- Root container: `R.id.fragment_container`
- Host: `MainActivity` (`app/src/main/java/com/zendalona/zmantra/MainActivity.kt`)
- Pattern: Manual FragmentManager transactions; no Navigation Component graph.
- Back model: Single back stack. Forward navigations must call `.addToBackStack(null)`.

## Architecture overview

- __Activity-owned back stack__
  - All screens are Fragments hosted by `MainActivity`.
  - `supportFragmentManager` is the single source of truth for navigation state.
- __Shared navigation API__
  - `MainActivity` implements a `FragmentNavigation` interface used by screens like `GameFragment` to request navigation.
  - Forward navigations replace `R.id.fragment_container` and add to the back stack.
- __Toolbar Up__
  - Up arrow visibility reflects back stack count (> 0 shows Up).
  - `onSupportNavigateUp()` delegates to system back.

## Entry/root setup

- App starts by setting a root fragment (e.g., `LandingPageFragment`) without adding it to the back stack.
- Never add the root to the back stack; otherwise exiting the app via back becomes awkward.

## Forward navigation (replace + back stack)

Preferred approach from fragments:

```kotlin
requireActivity().supportFragmentManager
    .beginTransaction()
    .setCustomAnimations(
        android.R.anim.fade_in,
        android.R.anim.fade_out,
        android.R.anim.fade_in,
        android.R.anim.fade_out
    )
    .replace(R.id.fragment_container, MyNextFragment.newInstance(args))
    .addToBackStack(null)
    .commit()
```

Or via the shared interface exposed by `MainActivity`:

```kotlin
(fragment.requireActivity() as FragmentNavigation)
    .loadFragment(MyNextFragment.newInstance(args), transit = FragmentTransaction.TRANSIT_FRAGMENT_FADE)
```

References:
- `presentation/features/game/GameFragment.kt` (calls `FragmentNavigation.loadFragment`)
- `presentation/features/learning/LearningFragment.kt` (direct transaction with `.addToBackStack(null)`)
- `core/base/BaseGameFragment.kt`, `presentation/features/quickplay/QuickPlayFragment.kt` (open `HintFragment` with back stack)

## Back handling

Back behavior is unified around the system back dispatcher and the activity back stack.

- __System Back__
  - Default: pops `supportFragmentManager` back stack.
  - Use when possible: `requireActivity().onBackPressedDispatcher.onBackPressed()` for programmatic back.
- __Toolbar Up__
  - Shown when back stack count > 0.
  - `onSupportNavigateUp()` calls `onBackPressed()` to match system back behavior.
- __Explicit pop__
  - `parentFragmentManager.popBackStack()` pops the current entry without dispatching the standard back callbacks. Use when you deliberately want a simple pop (e.g., `ScorePageFragment.goBack()`).

Examples in code:
- `core/utility/common/ScorePageFragment.kt`: registers an `OnBackPressedCallback`, and both auto-close and OK button call `goBack()` → `popBackStack()`.
- `presentation/features/hint/HintFragment.kt`: in-UI “Go Back” triggers `onBackPressedDispatcher.onBackPressed()`.
- `presentation/features/userguide/UserGuideFragment.kt`: same as above.
- `presentation/features/game/drawing/DrawingFragment.kt`: triggers back a few seconds after completion.
- `core/base/BaseGameFragment.kt`: pops back if no questions are available.
- `core/utility/common/endGame.kt`: `EndScore` completes by calling `onBackPressedDispatcher.onBackPressed()`.

## Toolbar logic in MainActivity

- Observes/polls `supportFragmentManager.backStackEntryCount` to toggle Up arrow.
- Sets accessibility content description (e.g., `R.string.back_button_label`).
- Delegates Up to `onBackPressed()` ensuring equivalence with system Back.

Key file: `app/src/main/java/com/zendalona/zmantra/MainActivity.kt`

## Adding a new screen: checklist

1. __Create fragment__
   - Provide `newInstance(...)` with necessary args via `Bundle`.
2. __Navigate forward__
   - Use shared `FragmentNavigation.loadFragment(...)` when available, or perform a transaction with `.addToBackStack(null)`.
3. __Handle Back (if needed)__
   - If you added to back stack, default back works. For in-UI back buttons, prefer `onBackPressedDispatcher.onBackPressed()`.
   - Only use `popBackStack()` when you explicitly want a silent pop (no dispatcher callbacks).
4. __Accessibility__
   - If the screen has critical announcements, use the app’s announce utility (see `BaseGameFragment`).
5. __Edge cases__
   - On empty data/error, either replace with an error UI or pop back with a clear message.


