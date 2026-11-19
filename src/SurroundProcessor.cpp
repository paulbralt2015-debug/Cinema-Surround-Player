
#include "SurroundProcessor.h"

#include <algorithm>
#include <cmath>

SurroundProcessor::SurroundProcessor(int rate)
    : sampleRate(std::max(8000, rate))
{
    const float cutoff = 120.0f;
    const float pi = 3.14159265358979323846f;

    lfeCoefficient =
        1.0f - std::exp(-2.0f * pi * cutoff / sampleRate);
}

void SurroundProcessor::setMode(Mode newMode)
{
    mode = newMode;
    reset();
}

void SurroundProcessor::setSurroundLevel(float level)
{
    surroundLevel = std::clamp(level, 0.0f, 1.5f);
}

void SurroundProcessor::setCenterLevel(float level)
{
    centerLevel = std::clamp(level, 0.0f, 1.5f);
}

void SurroundProcessor::setLfeLevel(float level)
{
    lfeLevel = std::clamp(level, 0.0f, 1.5f);
}

int SurroundProcessor::outputChannels() const
{
    return static_cast<int>(mode);
}

float SurroundProcessor::clampSample(float value) const
{
    return std::clamp(value, -1.0f, 1.0f);
}

void SurroundProcessor::reset()
{
    lfeState = 0.0f;
}

void SurroundProcessor::process(
    const float *input,
    int frames,
    std::vector<float> &output)
{
    if (!input || frames <= 0)
        return;

    const int channels = outputChannels();

    output.resize(static_cast<size_t>(frames) * channels);

    for (int i = 0; i < frames; ++i) {
        const float left = input[i * 2];
        const float right = input[i * 2 + 1];

        if (mode == Stereo) {
            output[i * 2] = left;
            output[i * 2 + 1] = right;
            continue;
        }

        // Canal central : informations communes gauche/droite.
        const float center =
            (left + right) * 0.5f * centerLevel;

        // Informations différentes : effet surround arrière.
        const float difference =
            (left - right) * 0.5f * surroundLevel;

        // Extraction des basses pour le canal LFE.
        const float mono = (left + right) * 0.5f;

        lfeState += lfeCoefficient * (mono - lfeState);

        const float lfe = lfeState * lfeLevel;

        if (mode == Surround51) {
            // Ordre : FL, FR, FC, LFE, BL, BR.
            output[i * 6 + 0] = clampSample(left);
            output[i * 6 + 1] = clampSample(right);
            output[i * 6 + 2] = clampSample(center);
            output[i * 6 + 3] = clampSample(lfe);
            output[i * 6 + 4] = clampSample(difference);
            output[i * 6 + 5] = clampSample(-difference);
        }
        else {
            // Ordre : FL, FR, FC, LFE, BL, BR, SL, SR.
            output[i * 8 + 0] = clampSample(left);
            output[i * 8 + 1] = clampSample(right);
            output[i * 8 + 2] = clampSample(center);
            output[i * 8 + 3] = clampSample(lfe);

            output[i * 8 + 4] =
                clampSample(difference * 0.7f);

            output[i * 8 + 5] =
                clampSample(-difference * 0.7f);

            output[i * 8 + 6] =
                clampSample((left - right * 0.5f) * surroundLevel * 0.5f);

            output[i * 8 + 7] =
                clampSample((right - left * 0.5f) * surroundLevel * 0.5f);
        }
    }
}
