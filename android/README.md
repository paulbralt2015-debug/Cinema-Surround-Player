# Android TV port (Android 6.0+)

## Build
GitHub Actions builds a debug APK with JDK 17 and Gradle 8.7. Minimum Android API is 23 (Android 6.0).

## Audio target: the Coocaa TV's own speakers
The app uses Android's normal media audio route (USAGE_MEDIA / CONTENT_TYPE_MOVIE). It does not target HDMI ARC, optical output, or an external AV receiver.

For AC-3 (Dolby Digital) and E-AC-3 (Dolby Digital Plus) tracks, ExoPlayer 2.19.1 queries Android's available media decoders. When the Coocaa firmware exposes a compatible decoder, Android can decode the track and route the resulting audio through the television's internal audio pipeline. The app does not force raw bitstream passthrough to the built-in speakers.

The Dolby Audio logo alone does not prove that the firmware exposes AC-3/E-AC-3 decoding to third-party applications. Actual support must be verified on the TV.

## Included in this iteration
- On-device diagnostic indicating whether Android advertises AC-3 and E-AC-3 decoder components (capability listing only; it does not prove successful playback).
- Remote-friendly landscape player UI and Android document picker.
- ExoPlayer 2.19.1 playback engine.
- Standard Android TV media audio routing, with no external receiver/HDMI passthrough control.
- Platform codec selection for formats supported by the Coocaa firmware.

## Not yet included
- The desktop FFmpeg DSP chain.
- A Dolby Surround / Pro Logic II matrix decoder.
- Guaranteed AC-3/E-AC-3 decoding on this exact Coocaa firmware.
- Complete subtitle, audio-track selection, and advanced container compatibility.

## Test on the Coocaa
1. Install the APK on the TV.
2. Play a known video containing an AC-3 5.1 audio track.
3. Check that sound comes from the TV's built-in speakers.
4. Repeat with an E-AC-3 track.
5. If a track fails, note the on-screen error and the codec/channel format.
