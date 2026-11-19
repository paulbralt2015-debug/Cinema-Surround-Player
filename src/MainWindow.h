
#pragma once

#include <QMainWindow>
#include <QMediaPlayer>
#include <QAudioOutput>
#include <QMediaDevices>
#include <QAudioDevice>

class QVideoWidget;
class QPushButton;
class QSlider;
class QLabel;
class QComboBox;

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = nullptr);
    ~MainWindow() override = default;

private:
    void openFile();
    void playPause();
    void stopPlayback();
    void updatePosition(qint64 position);
    void updateDuration(qint64 duration);
    void refreshAudioDevices();
    void selectAudioDevice(int index);
    void toggleFullscreen();
    QString formatTime(qint64 milliseconds) const;

    QMediaPlayer *player = nullptr;
    QAudioOutput *audioOutput = nullptr;
    QVideoWidget *videoWidget = nullptr;

    QPushButton *openButton = nullptr;
    QPushButton *playButton = nullptr;
    QPushButton *stopButton = nullptr;
    QPushButton *fullscreenButton = nullptr;

    QSlider *positionSlider = nullptr;
    QSlider *volumeSlider = nullptr;

    QComboBox *audioDeviceBox = nullptr;

    QLabel *statusLabel = nullptr;
    QLabel *timeLabel = nullptr;

    bool sliderPressed = false;
    QList<QAudioDevice> audioDevices;
};
