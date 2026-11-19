
#include "MainWindow.h"

#include <QVideoWidget>
#include <QPushButton>
#include <QSlider>
#include <QLabel>
#include <QComboBox>
#include <QFileDialog>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QWidget>
#include <QUrl>
#include <QFileInfo>
#include <QMessageBox>
#include <QKeyEvent>

MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent)
{
    setWindowTitle("Cinema Surround Player");
    resize(1100, 720);

    player = new QMediaPlayer(this);
    audioOutput = new QAudioOutput(this);

    player->setAudioOutput(audioOutput);
    audioOutput->setVolume(0.80);

    videoWidget = new QVideoWidget(this);
    player->setVideoOutput(videoWidget);

    openButton = new QPushButton("Ouvrir");
    playButton = new QPushButton("Lecture");
    stopButton = new QPushButton("Stop");
    fullscreenButton = new QPushButton("Plein écran");

    positionSlider = new QSlider(Qt::Horizontal);
    positionSlider->setRange(0, 0);

    volumeSlider = new QSlider(Qt::Horizontal);
    volumeSlider->setRange(0, 100);
    volumeSlider->setValue(80);
    volumeSlider->setMaximumWidth(150);

    audioDeviceBox = new QComboBox();
    audioDeviceBox->setMinimumWidth(220);

    statusLabel = new QLabel("Prêt");
    timeLabel = new QLabel("00:00 / 00:00");

    auto *central = new QWidget(this);
    auto *mainLayout = new QVBoxLayout(central);

    auto *controlLayout = new QHBoxLayout();
    controlLayout->addWidget(openButton);
    controlLayout->addWidget(playButton);
    controlLayout->addWidget(stopButton);
    controlLayout->addWidget(fullscreenButton);
    controlLayout->addStretch();

    auto *audioLayout = new QHBoxLayout();
    audioLayout->addWidget(new QLabel("Sortie audio :"));
    audioLayout->addWidget(audioDeviceBox);
    audioLayout->addSpacing(15);
    audioLayout->addWidget(new QLabel("Volume :"));
    audioLayout->addWidget(volumeSlider);

    mainLayout->addWidget(videoWidget, 1);
    mainLayout->addWidget(positionSlider);
    mainLayout->addWidget(timeLabel);
    mainLayout->addLayout(controlLayout);
    mainLayout->addLayout(audioLayout);
    mainLayout->addWidget(statusLabel);

    setCentralWidget(central);

    connect(openButton, &QPushButton::clicked,
            this, &MainWindow::openFile);

    connect(playButton, &QPushButton::clicked,
            this, &MainWindow::playPause);

    connect(stopButton, &QPushButton::clicked,
            this, &MainWindow::stopPlayback);

    connect(fullscreenButton, &QPushButton::clicked,
            this, &MainWindow::toggleFullscreen);

    connect(volumeSlider, &QSlider::valueChanged,
            this, [this](int value) {
        audioOutput->setVolume(value / 100.0);
    });

    connect(positionSlider, &QSlider::sliderPressed,
            this, [this]() {
        sliderPressed = true;
    });

    connect(positionSlider, &QSlider::sliderReleased,
            this, [this]() {
        player->setPosition(positionSlider->value());
        sliderPressed = false;
    });

    connect(player, &QMediaPlayer::positionChanged,
            this, &MainWindow::updatePosition);

    connect(player, &QMediaPlayer::durationChanged,
            this, &MainWindow::updateDuration);

    connect(player, &QMediaPlayer::playbackStateChanged,
            this, [this](QMediaPlayer::PlaybackState state) {
        if (state == QMediaPlayer::PlayingState)
            playButton->setText("Pause");
        else
            playButton->setText("Lecture");
    });

    connect(player, &QMediaPlayer::mediaStatusChanged,
            this, [this](QMediaPlayer::MediaStatus status) {
        if (status == QMediaPlayer::EndOfMedia) {
            playButton->setText("Lecture");
            statusLabel->setText("Lecture terminée");
        }
    });

    connect(player, &QMediaPlayer::errorOccurred,
            this, [this](QMediaPlayer::Error error,
                         const QString &message) {
        Q_UNUSED(error);
        statusLabel->setText("Erreur : " + message);
    });

    connect(audioDeviceBox,
            &QComboBox::currentIndexChanged,
            this,
            &MainWindow::selectAudioDevice);

    connect(&QMediaDevices::defaultAudioOutputChanged,
            this, [this](const QAudioDevice &) {
        refreshAudioDevices();
    });

    refreshAudioDevices();
}

void MainWindow::openFile()
{
    const QString filename = QFileDialog::getOpenFileName(
        this,
        "Ouvrir une vidéo",
        QString(),
        "Fichiers vidéo (*.mp4 *.mkv *.avi *.mov *.wmv *.m4v *.webm);;"
        "Tous les fichiers (*.*)"
    );

    if (filename.isEmpty())
        return;

    player->stop();
    player->setSource(QUrl::fromLocalFile(filename));

    setWindowTitle(
        "Cinema Surround Player - " +
        QFileInfo(filename).fileName()
    );

    statusLabel->setText("Fichier chargé");
    player->play();
}

void MainWindow::playPause()
{
    if (player->source().isEmpty()) {
        openFile();
        return;
    }

    if (player->playbackState() == QMediaPlayer::PlayingState) {
        player->pause();
        statusLabel->setText("Pause");
    } else {
        player->play();
        statusLabel->setText("Lecture");
    }
}

void MainWindow::stopPlayback()
{
    player->stop();
    player->setPosition(0);
    statusLabel->setText("Arrêt");
}

void MainWindow::updatePosition(qint64 position)
{
    if (!sliderPressed)
        positionSlider->setValue(static_cast<int>(position));

    timeLabel->setText(
        formatTime(position) + " / " +
        formatTime(player->duration())
    );
}

void MainWindow::updateDuration(qint64 duration)
{
    positionSlider->setRange(0, static_cast<int>(duration));

    timeLabel->setText(
        formatTime(player->position()) + " / " +
        formatTime(duration)
    );
}

void MainWindow::refreshAudioDevices()
{
    const QAudioDevice previousDevice = audioOutput->device();

    audioDevices = QMediaDevices::audioOutputs();

    audioDeviceBox->blockSignals(true);
    audioDeviceBox->clear();

    int selectedIndex = -1;

    for (int i = 0; i < audioDevices.size(); ++i) {
        const QAudioDevice &device = audioDevices.at(i);

        audioDeviceBox->addItem(device.description());

        if (device.id() == previousDevice.id())
            selectedIndex = i;
    }

    if (selectedIndex < 0) {
        const QAudioDevice defaultDevice =
            QMediaDevices::defaultAudioOutput();

        for (int i = 0; i < audioDevices.size(); ++i) {
            if (audioDevices.at(i).id() == defaultDevice.id()) {
                selectedIndex = i;
                break;
            }
        }
    }

    if (selectedIndex >= 0)
        audioDeviceBox->setCurrentIndex(selectedIndex);

    audioDeviceBox->blockSignals(false);

    if (selectedIndex >= 0)
        audioOutput->setDevice(audioDevices.at(selectedIndex));
}

void MainWindow::selectAudioDevice(int index)
{
    if (index < 0 || index >= audioDevices.size())
        return;

    audioOutput->setDevice(audioDevices.at(index));
    statusLabel->setText(
        "Sortie audio : " + audioDevices.at(index).description()
    );
}

void MainWindow::toggleFullscreen()
{
    if (isFullScreen())
        showNormal();
    else
        showFullScreen();
}

QString MainWindow::formatTime(qint64 milliseconds) const
{
    const qint64 seconds = milliseconds / 1000;
    const qint64 hours = seconds / 3600;
    const qint64 minutes = (seconds % 3600) / 60;
    const qint64 remainingSeconds = seconds % 60;

    if (hours > 0) {
        return QString("%1:%2:%3")
            .arg(hours, 2, 10, QChar('0'))
            .arg(minutes, 2, 10, QChar('0'))
            .arg(remainingSeconds, 2, 10, QChar('0'));
    }

    return QString("%1:%2")
        .arg(minutes, 2, 10, QChar('0'))
        .arg(remainingSeconds, 2, 10, QChar('0'));
}
