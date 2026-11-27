# Android TV port (Android 6.0+)

## Build
GitHub Actions builds a debug APK with JDK 17 and Gradle 8.7. Minimum Android API is 23 (Android 6.0).

## Audio target: the Coocaa TV's own speakers
This version uses Android's normal media audio route (USAGE_MEDIA / CONTENT_TYPE_MOVIE). It does not target HDMI ARC, optical output, or an external AV receiver.

For AC-3 (Dolby Digital) and E-AC-3 (Dolby Digital Plus) tracks, ExoPlayer asks Android for a compatible decoder. If the Coocaa firmware provides a Dolby-capable MediaCodec decoder, Android can decode the track and send the resulting audio through the television's internal audio pipeline. If the hardware decoder is unavailable, playback may fail or require a future software-decoding integration.

Important: this is not a promise of raw Dolby bitstream passthrough to the built-in speakers. Internal speakers normally need the TV's own decoder/audio pipeline; the app cannot force a proprietary Dolby decoder that the firmware does not expose to Android. The Dolby Audio logo alone does not confirm that an application can access AC-3/E-AC-3 decoding.

## Included in this iteration
- Remote-friendly landscape player UI and Android document picker.
- ExoPlayer 2.9.6 playback.
- Standard Android TV media audio routing, with no external receiver/HDMI passthrough control.
- Platform codec selection for audio formats supported by the Coocaa firmware.

## Not yet included
- The desktop FFmpeg DSP chain.
- A Dolby Surround / Pro Logic II matrix decoder.
- Guaranteed AC-3/E-AC-3 decoding on this exact Coocaa firmware. This must be tested on the television with sample files.

## Test on the Coocaa
1. Install the APK on the TV.
2. Play a known video containing an AC-3 5.1 audio track.
3. Check that sound comes from the TV's built-in speakers.
4. Repeat with an E-AC-3 track.
5. If either track fails, collect the on-screen error and the exact audio codec/channel format. The next step will be to add or select a compatible decoder path for this firmware.
