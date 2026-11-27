# Android TV port (Android 6.0+)

This is the first Android TV application shell for the Cinema Surround Player project.

## Build
The GitHub Actions workflow `.github/workflows/android-build.yml` builds a debug APK using JDK 17 and Gradle 8.7. The app has `minSdk 23` (Android 6.0).

## Current status
- Android TV / D-pad-friendly landscape interface.
- Video selection through Android's document picker.
- Basic playback controls using Android's platform VideoView/MediaPlayer stack.
- Windows Qt application remains unchanged.

## Important audio limitation
This initial port does **not** yet include the Windows FFmpeg pipeline or a verified Dolby Surround / Pro Logic II decoder. Android's platform player sends decoded audio to the system audio path; it does not expose decoded PCM here for the existing surround processor. No Dolby certification, Pro Logic II decoding, 5.1 output, or HDMI passthrough is claimed at this stage. Those require a separate FFmpeg/MediaCodec audio pipeline, PCM processing integration, and tests against the Coocaa TV's actual audio outputs.

The APK is a compatibility and playback foundation, not the completed surround conversion.
