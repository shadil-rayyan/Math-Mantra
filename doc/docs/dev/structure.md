# Structure & Architecture

Math Mantra is an Android app organized with clear separation of concerns, strong accessibility, and content-driven gameplay sourced from localized Excel files.

- Presentation layer: Fragments/UI and feature orchestration
- Domain layer: Use cases, models, repository interfaces
- Data layer: Excel-backed content loader and in-memory cache
- Core utilities: TTS, dialogs, grading, accessibility helpers
- DI: Hilt modules for wiring repositories and use cases
- Startup: Splash preload + background caching

## Repository Layout

- App code: `app/src/main/java/com/zendalona/zmantra/`
- Assets: `app/src/main/assets/`
  - Questions Excel: `assets/questions/{lang}.xlsx` (e.g., `en.xlsx`, `ml.xlsx`, etc.)
  - User guide HTML: `assets/userguide/{lang}.html`
- Resources: `app/src/main/res/`

## Presentation Layer

- Game hub
  - [presentation/features/game/GameFragment.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/presentation/features/game/GameFragment.kt:0:0-0:0)
  - Hosts buttons to navigate to game mode fragments via `FragmentNavigation`.

- Base game foundation
  - [core/base/BaseGameFragment.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:0:0-0:0)
  - Responsibilities:
    - Reads `lang` from `LocaleHelper` and `difficulty` from `DifficultyPreferences`.
    - Manages `TTSUtility` lifecycle.
    - Sets up hint menu ([setupHintMenu](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:74:4-96:5)) and opens `HintFragment`.
    - Loads questions: try [QuestionCache.getQuestions(...)](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/repository/QuestionRepository.kt:5:4-5:82); fallback to [ExcelQuestionLoader.loadQuestionsFromExcel(...)](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/excel/ExcelQuestionLoader.kt:97:4-117:5); then cache with [QuestionCache.putQuestions(...)](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/excel/QuestionCache.kt:86:4-89:5).
    - Accessibility announcements ([announce](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:108:4-114:5), [announceNextQuestion](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:116:4-118:5)).
    - Unified validation flow via [handleAnswerSubmission(...)](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:202:4-227:5) using `GradingUtils` and `DialogUtils`.
    - Optional GIF support with Glide ([loadGifIfDefined](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:63:4-72:5)).
  - Child fragments must implement:
    - [getModeName()](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:230:4-230:48): String that matches Excel Mode column.
    - [onQuestionsLoaded(questions: List<GameQuestion>)](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:229:4-229:75): Render and handle gameplay.

- Example feature fragments
  - Under `presentation/features/game/`: `shake/ShakeFragment.kt`, `tap/TapFragment.kt`, `angle/AngleFragment.kt`, `compass/CompassFragment.kt`, `numberline/NumberLineFragment.kt`, `drawing/DrawingFragment.kt`, `touchscreen/TouchScreenFragment.kt`, etc.

- Settings and User Guide
  - Locale and difficulty:
    - [presentation/features/setting/util/LocaleHelper.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/presentation/features/setting/util/LocaleHelper.kt:0:0-0:0)
    - [presentation/features/setting/util/DifficultyPreferences.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/presentation/features/setting/util/DifficultyPreferences.kt:0:0-0:0)
  - User guide:
    - `presentation/features/userguide/UserGuideFragment.kt`
    - [presentation/features/userguide/UserGuideViewModel.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/presentation/features/userguide/UserGuideViewModel.kt:0:0-0:0)

## Domain Layer

- Models
  - [domain/model/GameQuestion.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/model/GameQuestion.kt:0:0-0:0)
    - `expression: String`
    - `answer: Int`
    - `timeLimit: Int = 20`
    - `celebration: Boolean = false`
  - [domain/model/Hintable.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/model/Hintable.kt:0:0-0:0)
  - [domain/model/HintIconVisibilityController.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/model/HintIconVisibilityController.kt:0:0-0:0)

- Repository interfaces
  - [domain/repository/QuestionRepository.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/repository/QuestionRepository.kt:0:0-0:0)
  - [domain/repository/UserGuideRepository.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/repository/UserGuideRepository.kt:0:0-0:0)

- Use cases
  - [domain/usecase/LoadQuestionsUseCase.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/usecase/LoadQuestionsUseCase.kt:0:0-0:0)
    - [repository.getQuestions(mode, difficulty)](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/repository/QuestionRepository.kt:5:4-5:82)
  - [domain/usecase/userguide/GetUserGuideHtmlUseCase.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/usecase/userguide/GetUserGuideHtmlUseCase.kt:0:0-0:0)
    - [repository.getUserGuideHtml(language)](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/repository/UserGuideRepository.kt:3:4-3:58)

Note: Question loading currently uses [BaseGameFragment](cci:2://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:33:0-231:1) + `ExcelQuestionLoader` + `QuestionCache` directly. [QuestionRepository](cci:2://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/repository/QuestionRepository.kt:4:0-6:1)/[LoadQuestionsUseCase](cci:2://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/usecase/LoadQuestionsUseCase.kt:5:0-11:1) exists for future alignment with a repository abstraction.

## Data Layer

- Excel loader
  - [core/utility/excel/ExcelQuestionLoader.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/excel/ExcelQuestionLoader.kt:0:0-0:0)
  - Reads from `assets/questions/{lang}.xlsx` via Apache POI.
  - Filters by `mode` and `difficulty` columns.
  - Renders variableized templates and evaluates answers via exp4j (numeric modes).
  - Columns per row:
    - C0: Question template (supports `{a}`, `{b}`, ...)
    - C1: Mode (e.g., `shake`, `tap`, `angle`, ...)
    - C2: Operand spec (grammar below)
    - C3: Difficulty (string `"1"`..`"5"`)
    - C4: Answer expression template (uses same variables)
    - C5: Time limit (int; default 20)

- Operand grammar
  - Comma list: `1,2,5` → pick one randomly
  - Range: `5:10` → inclusive random
  - Multiplicative pair: `2,3;3:5` → pick left and right, multiply
  - For non-numeric modes like `direction` and `drawing`, operands map to literal options; answers default to `0` and are handled by UI logic.

- Cache
  - [core/utility/excel/QuestionCache.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/excel/QuestionCache.kt:0:0-0:0)
  - Key: `"$lang-$mode-$difficulty"`
  - APIs:
    - [getQuestions(lang, mode, difficulty)](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/repository/QuestionRepository.kt:5:4-5:82)
    - [putQuestions(lang, mode, difficulty, questions)](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/excel/QuestionCache.kt:86:4-89:5)
    - [preloadCurrentDifficultyModes(context, lang, onProgress)](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/excel/QuestionCache.kt:16:4-43:5)
    - [preloadOtherDifficultyModes(context, lang)](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/excel/QuestionCache.kt:45:4-67:5)
    - [clearCache()](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/excel/QuestionCache.kt:91:4-93:5)

- User guide repository
  - [data/repository/userguide/UserGuideRepositoryImpl.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/data/repository/userguide/UserGuideRepositoryImpl.kt:0:0-0:0)
  - Reads `assets/userguide/{lang}.html`.

## Dependency Injection (Hilt)

- App init
  - [ZMantra.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/ZMantra.kt:0:0-0:0) annotated with `@HiltAndroidApp`.
  - On startup:
    - [LocaleHelper.onAttach(this)](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/presentation/features/game/GameFragment.kt:29:4-36:5) applies saved locale.
    - Contrast mode applied via `AppCompatDelegate.setDefaultNightMode(...)`.

- Modules
  - [di/UserGuideModule.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/di/UserGuideModule.kt:0:0-0:0)
  - Provides [UserGuideRepository](cci:2://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/repository/UserGuideRepository.kt:2:0-4:1) and [GetUserGuideHtmlUseCase](cci:2://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/usecase/userguide/GetUserGuideHtmlUseCase.kt:5:0-11:1).
  - Future: Provide a concrete [QuestionRepository](cci:2://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/repository/QuestionRepository.kt:4:0-6:1) when adopted app-wide.

## Startup Flow

- [SplashScreen.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/SplashScreen.kt:0:0-0:0)
  - Shows welcome GIF via Glide.
  - If accessibility enabled, periodically announces “Loading questions”.
  - Preloads current-difficulty questions via [QuestionCache.preloadCurrentDifficultyModes](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/excel/QuestionCache.kt:16:4-43:5).
  - Navigates to `MainActivity` on completion.
  - Background-preloads other difficulties afterward.

## Accessibility & TTS

- Accessibility service
  - [core/utility/accessibility/MathsManthraAccessibilityService.java](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/accessibility/MathsManthraAccessibilityService.java:0:0-0:0)
  - A11y-first: fragments announce next-question transitions and key events.

- TTS
  - [core/utility/common/TTSUtility.java](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/common/TTSUtility.java:0:0-0:0)
  - Created in [BaseGameFragment.onCreate](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/ZMantra.kt:10:4-22:5), shut down in [onDestroyView](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:58:4-61:5).

- Dialogs and grading
  - [core/utility/common/DialogUtils.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/common/DialogUtils.kt:0:0-0:0)
  - [core/utility/common/GradingUtils.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/common/GradingUtils.kt:0:0-0:0)

## Localization & Difficulty

- Localization
  - [LocaleHelper.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/presentation/features/setting/util/LocaleHelper.kt:0:0-0:0) manages language.
  - Excel per language in `assets/questions/{lang}.xlsx`.
  - User guide per language in `assets/userguide/{lang}.html`.

- Difficulty
  - [core/Enum/Diffculty.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/Enum/Diffculty.kt:0:0-0:0) → `object Difficulty { SIMPLE=1, EASY=2, MEDIUM=3, HARD=4, CHALLENGING=5 }`
  - [DifficultyPreferences.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/presentation/features/setting/util/DifficultyPreferences.kt:0:0-0:0) persists selection.
  - Excel filtering uses difficulty strings (`"1"`..`"5"`).

## Adding a New Game Mode

1. Create fragment under `presentation/features/game/<mode>/<Mode>Fragment.kt` extending [BaseGameFragment](cci:2://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:33:0-231:1).
2. Implement:
   - `override fun getModeName() = "<mode>"` (must match Excel Mode column).
   - `override fun onQuestionsLoaded(questions: List<GameQuestion>)` to render and handle logic.
3. Use [handleAnswerSubmission(...)](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:202:4-227:5) for validation, and `DialogUtils` to show results/retry/correct-answer dialogs.
4. Provide optional GIF via [getGifImageView()](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:42:4-42:49) and [getGifResource()](cci:1://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:43:4-43:42).
5. Add navigation from [GameFragment](cci:2://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/presentation/features/game/GameFragment.kt:23:0-133:1).
6. Add content rows into `assets/questions/{lang}.xlsx` with correct Mode and Difficulty.
7. Verify splash preloading logs and cache hits.

## Assets & Resources

- Excel questions: `app/src/main/assets/questions/{lang}.xlsx`
- User guide HTML: `app/src/main/assets/userguide/{lang}.html`
- Drawables/GIFs: `app/src/main/res/drawable*`

## Key Dependencies

- Hilt: DI
- Apache POI: Excel parsing
- exp4j: Expression evaluation
- Glide: GIF loading
- AndroidX: Fragments, Lifecycle, Preferences
