# Android TV port (Android 6.0+)

## Build
GitHub Actions builds a debug APK with JDK 17 and Gradle 8.7. Minimum Android API is 23 (Android 6.0).

## Included in this iteration
- Remote-friendly landscape player UI.
- ExoPlayer 2.9.6 media playback and file selection.
- AC-3 (Dolby Digital) and E-AC-3 (Dolby Digital Plus) encoded HDMI passthrough negotiation through Android's reported audio sink capabilities.
- A UI switch to request automatic passthrough or disable passthrough.

## Critical passthrough limitations
The app cannot force bitstream output if the Coocaa firmware/HDMI audio path does not advertise AC-3/E-AC-3 support. ExoPlayer negotiates passthrough based on Android's HDMI audio-capability information. This must be verified on the actual TV and receiver. The TV's optical/ARC/eARC path, firmware settings, connected receiver, and file container/codec all affect the result.

This implementation does not claim DTS, TrueHD, Dolby Atmos, or any other encoded format passthrough. It does not yet include the desktop FFmpeg DSP chain or a Dolby Pro Logic II matrix decoder. The passthrough switch is a policy/status control; the ExoPlayer audio sink makes the actual per-track passthrough decision. A forced passthrough mode is intentionally not exposed because Android 6 TV firmware cannot be assumed to support it.

## Validation still required
1. Build the APK in Actions.
2. Install on the Coocaa Android 6 TV.
3. Connect an AC-3/E-AC-3-capable AV receiver over HDMI ARC/optical as supported by the TV.
4. Confirm the receiver's front-panel input format during playback.
5. Test a known AC-3 sample and an E-AC-3 sample. If Android reports only PCM, the TV firmware/output path is not exposing passthrough to the app.
