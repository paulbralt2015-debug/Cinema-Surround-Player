package com.cinemasurround.player;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.MediaItem;

public class MainActivity extends Activity {
    private static final int REQUEST_MEDIA = 41;

    private PlayerView playerView;
    private ExoPlayer player;
    private TextView status;
    private Uri currentUri;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(15, 18, 24));
        root.setPadding(24, 14, 24, 12);

        TextView title = new TextView(this);
        title.setText("CINEMA SURROUND PLAYER");
        title.setTextColor(Color.WHITE);
        title.setTextSize(23);
        title.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(title, new LinearLayout.LayoutParams(-1, 48));

        playerView = new PlayerView(this);
        playerView.setUseController(true);
        playerView.setBackgroundColor(Color.BLACK);
        root.addView(playerView, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);
        controls.setOrientation(LinearLayout.HORIZONTAL);

        Button open = makeButton("OUVRIR");
        Button play = makeButton("LECTURE / PAUSE");
        Button stop = makeButton("STOP");
        controls.addView(open);
        controls.addView(play);
        controls.addView(stop);
        root.addView(controls, new LinearLayout.LayoutParams(-1, 62));

        status = new TextView(this);
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(13);
        status.setPadding(4, 8, 4, 4);
        status.setText("Prêt — sortie audio du téléviseur");
        root.addView(status, new LinearLayout.LayoutParams(-1, 42));
        setContentView(root);

        open.setOnClickListener(v -> openMedia());
        play.setOnClickListener(v -> {
            if (player != null && currentUri != null) {
                player.setPlayWhenReady(!player.getPlayWhenReady());
            } else {
                openMedia();
            }
        });
        stop.setOnClickListener(v -> {
            if (player != null) {
                player.stop();
                status.setText("Lecture arrêtée");
            }
        });

        initializePlayer();
    }

    private Button makeButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(14);
        button.setFocusable(true);
        button.setFocusableInTouchMode(true);
        return button;
    }

    private void initializePlayer() {
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
        player = new ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .build();
        player.setAudioAttributes(audioAttributes, true);

        playerView.setPlayer(player);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    status.setText("Chargement de la vidéo et de la piste audio…");
                } else if (playbackState == Player.STATE_READY) {
                    status.setText("Lecture — audio envoyé au système audio du téléviseur");
                } else if (playbackState == Player.STATE_ENDED) {
                    status.setText("Lecture terminée");
                } else if (playbackState == Player.STATE_IDLE && currentUri == null) {
                    status.setText("Prêt — sortie audio du téléviseur");
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                String detail = error.getMessage() == null ? "Erreur de lecture" : error.getMessage();
                status.setText("Erreur audio/vidéo : " + detail);
                Toast.makeText(MainActivity.this,
                    "Impossible de lire ce fichier : " + detail, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openMedia() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQUEST_MEDIA);
        } catch (Exception e) {
            Toast.makeText(this, "Sélecteur de fichiers indisponible", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_MEDIA || resultCode != RESULT_OK ||
            data == null || data.getData() == null) {
            return;
        }

        currentUri = data.getData();
        try {
            getContentResolver().takePersistableUriPermission(
                currentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // Some document providers grant only temporary read access.
        }

        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(currentUri));

        player.stop();
        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
        status.setText("Ouverture du fichier — décodage audio par Android si disponible");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playerView != null) {
            playerView.setPlayer(null);
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }
}