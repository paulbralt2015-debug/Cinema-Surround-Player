
#pragma once

#include <vector>

class SurroundProcessor
{
public:
    enum Mode {
        Stereo = 2,
        Surround51 = 6,
        Surround71 = 8
    };

    explicit SurroundProcessor(int sampleRate = 48000);

    void setMode(Mode mode);
    void setSurroundLevel(float level);
    void setCenterLevel(float level);
    void setLfeLevel(float level);

    int outputChannels() const;

    // Entrée : audio stéréo intercalé en float [-1, 1].
    // Sortie : audio multicanal intercalé en float [-1, 1].
    void process(
        const float *input,
        int frames,
        std::vector<float> &output
    );

    void reset();

private:
    int sampleRate;
    Mode mode = Surround51;

    float surroundLevel = 0.75f;
    float centerLevel = 1.0f;
    float lfeLevel = 0.8f;

    float lfeState = 0.0f;
    float lfeCoefficient = 0.0f;

    float clampSample(float value) const;
};
