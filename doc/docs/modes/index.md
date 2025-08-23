# 🧑‍💻 Interaction Modes

zMantra offers a variety of **game modes** designed for accessible math learning, supporting different input styles such as shake, tap, drawing, and audio-based interaction. Each mode focuses on unique skills while maintaining accessibility through speech and haptic feedback.

## Available Modes

| Mode               | Description                                                                 |
|--------------------|-----------------------------------------------------------------------------|
| [Bell Ring (Shake)](shake.md) | Shake the phone a number of times to answer.                          |
| [Drum Play (Tap)](tap.md)     | Tap the screen to match numbers or submit answers.                   |
| [Drawing](drawing.md)         | Sketch shapes or write numbers on the screen as answers.             |
| [Touch Count](touch.md)       | Tap the screen multiple times to submit number answers.              |
| [Stereo Sound](stereo.md)     | Identify numbers based on audio cues from left/right ears.           |
| [Number Line](number-line.md) | Move a character along a number line using touch or gestures.        |
| [MCQ](quick-learning.md)      | Answer audio-based multiple-choice questions.                        |
| [Compass Navigation](compass.md) | Rotate the phone to point North, South, East, or West.             |
| [Day Calculation](day.md)     | Calculate and identify future days from today’s date.                |
| [Mental Math](mental.md)      | Solve timed arithmetic questions of varying difficulty.              |
| [Angle Mode](angle.md)        | Rotate to match angle instructions (left, right, up, down).          |

---

## Tips for Developers
- Each mode is implemented as a `Fragment` extending [`BaseGameFragment`](../architecture/base-game-fragment.md).  
- **Mode name strings** must match the fragment’s `getModeName()` implementation.  
- Question sets are stored in `assets/questions/{lang}.xlsx` and tied to specific modes.  

