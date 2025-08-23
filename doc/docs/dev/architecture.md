# Architecture Overview

App follows a layered approach with Hilt DI, strong accessibility, and Excel-driven content.

## Layers
- Presentation: Fragments and view logic (e.g., [GameFragment](cci:2://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/presentation/features/game/GameFragment.kt:23:0-133:1), per-mode fragments) with ViewBinding.
- Domain: Models and use cases (e.g., [GameQuestion](cci:2://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/model/GameQuestion.kt:3:0-8:1), [LoadQuestionsUseCase](cci:2://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/domain/usecase/LoadQuestionsUseCase.kt:5:0-11:1)).
- Data: Excel loaders and caches (Apache POI + in-memory cache).

## Key Components
- Base gameplay: [core/base/BaseGameFragment.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/base/BaseGameFragment.kt:0:0-0:0)
  - Loads questions (cache → Excel), manages TTS, hint menu, validation, and announcements.
- Data loading: [core/utility/excel/ExcelQuestionLoader.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/excel/ExcelQuestionLoader.kt:0:0-0:0)
  - Reads `assets/questions/{lang}.xlsx` and filters by `mode` and `difficulty`.
- Cache: [core/utility/excel/QuestionCache.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/excel/QuestionCache.kt:0:0-0:0)
  - Preloads on splash; keyed by `lang-mode-difficulty`.

## DI
- Hilt setup in [ZMantra.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/ZMantra.kt:0:0-0:0) (`@HiltAndroidApp`).
- Modules wire repositories/use cases (e.g., [di/UserGuideModule.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/di/UserGuideModule.kt:0:0-0:0)).

## Accessibility & TTS
- Announcements in base fragment; TalkBack-friendly flows.
- TTS lifecycle via [core/utility/common/TTSUtility.java](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/common/TTSUtility.java:0:0-0:0).

## Startup
- [SplashScreen.kt](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/SplashScreen.kt:0:0-0:0) preloads questions and navigates to main hub.

## Tech Highlights
- Kotlin + some Java utilities
- AndroidX, Material, Glide, Lottie
- Apache POI (Excel), exp4j (expressions)