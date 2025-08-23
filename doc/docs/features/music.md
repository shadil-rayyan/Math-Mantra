

# Background Music

The app includes looped background music functionality that can be toggled by the user from the Settings screen.

## How It Works

The `BackgroundMusicPlayer` singleton in `utility/settings/BackgroundMusicPlayer.kt` manages all music playback operations using Android's `MediaPlayer`.

### Lifecycle:

- **Initialization**:
    - `BackgroundMusicPlayer.initialize(context)` is called once in `SettingFragment.onViewCreated()` to set up the `MediaPlayer`.
    - It loads the `R.raw.drums_sound` audio and prepares it to loop.

- **Start/Pause**:
    - Users can start or pause the music (via a toggle switch, currently commented out).
    - `startMusic()` plays the music if not already playing.
    - `pauseMusic()` stops it temporarily.

- **Stop**:
    - `stopMusic()` is used to permanently stop and release the media player.

- **Volume**:
    - Default volume is 0.5 (range: 0.0 - 1.0).
    - Can be increased or decreased using UI buttons (currently commented out).
    - Changes are applied live and persisted in `SharedPreferences`.

### Accessibility

The volume level is announced when adjusted to improve accessibility for visually impaired users.



