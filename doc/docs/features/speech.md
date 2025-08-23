# Speech Rate & TTS

- Central TTS helper for consistent speech output across screens.

## Key Files
- [core/utility/common/TTSUtility.java](cci:7://file:///home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/home/shadilrayyan/AndroidStudioProjects/MathsMathra/app/src/main/java/com/zendalona/zmantra/core/utility/common/TTSUtility.java:0:0-0:0)
- Settings under `presentation/features/setting/util/*`

## Behavior
- Initialized in base fragment lifecycle, stopped on view destroy.
- Speech rate and language respect user preferences and locale.

## TODO
- Document how to change speech rate/pitch.
- Add examples of announcing questions and results.