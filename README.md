# Walkbar

An Android app that places animated character companions along a video's progress bar, walking in sync with playback. Made for Reels, Shorts, TikTok, and other short form video.

Pick a character, choose where it should walk on the bar, and export the result as a video. Both pixel art and 3D style characters are supported.

## What it does

- A library of characters loaded from a manifest, with a picker in the editor
- Characters walk along the timeline in real time as the video plays
- Pixel art and 3D rendering paths, with separate math for walk cycles and 3D projection
- Safe zone and platform overlay guides so the character does not land on UI elements
- Video export through an OpenGL ES surface encoder
- Sample video generation for trying things without importing your own clip

## How it fits together

```
app/src/main/java/com/example/
  characters/   manifest loader, registry, renderers, walk cycle and 3D math
  export/       EGL surface encoder and video exporter
  media/        metadata helper and sample video generator
  model/        character, overlay config, export state, platform enums
  player/       playback manager that drives the walk sync
  ui/           editor, export, and home screens plus character picker
app/src/main/assets/characters/
  characters_manifest.json
```

## Build

Open the project in Android Studio, or:

```sh
./gradlew assembleDebug
```

Requires the Android SDK. A script (`generate-debug-keystore.sh`) is included if you need a debug keystore for local signing.

## Status

Work in progress. The character set and export pipeline are still evolving.

## Contact

Built by Saddex. Portfolio: https://saddexrnx.github.io/. Telegram: [@Saddex_x](https://t.me/Saddex_x).
