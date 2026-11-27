package com.cinemasurround.player;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends Activity {
    private static final int REQUEST_MEDIA = 41;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private TextView status;
    private boolean passthroughRequested = true;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
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
        Button open = makeButton("OUVRIR");
        Button play = makeButton("LECTURE / PAUSE");
        Button stop = makeButton("STOP");
        Button passthrough = makeButton("PASSTHROUGH : AUTO");
        controls.addView(open);
        controls.addView(play);
        controls.addView(stop);
        controls.addView(passthrough);
        root.addView(controls, new LinearLayout.LayoutParams(-1, 62));

        status = new TextView(this);
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(13);
        status.setPadding(4, 8, 4, 4);
        status.setText("Prêt — Android 6.0+ | Dolby bitstream selon capacités HDMI déclarées");
        root.addView(status, new LinearLayout.LayoutParams(-1, 42));
        setContentView(root);

        open.setOnClickListener(v -> openMedia());
        play.setOnClickListener(v -> {
            if (player != null) {
                if (player.getPlayWhenReady()) player.setPlayWhenReady(false);
                else player.setPlayWhenReady(true);
            }
        });
        stop.setOnClickListener(v -> {
            if (player != null) {
                player.stop();
                status.setText("Lecture arrêtée");
            }
        });
        passthrough.setOnClickListener(v -> {
            passthroughRequested = !passthroughRequested;
            passthrough.setText(passthroughRequested ? "PASSTHROUGH : AUTO" : "PASSTHROUGH : OFF");
            status.setText(passthroughRequested
                ? "Mode automatique : AC-3/E-AC-3 transmis si Android/HDMI les annonce."
                : "Passthrough désactivé : décodage logiciel/PCM selon le décodeur disponible.");
            // ExoPlayer's Android audio sink negotiates encoded passthrough from
            // the HDMI AudioCapabilities broadcast. The switch controls policy
            // at the application layer; it cannot force unsupported TV hardware.
            if (player != null) {
                Uri current = player.getCurrentTag() instanceof Uri ? (Uri) player.getCurrentTag() : null;
                long position = player.getCurrentPosition();
                boolean playing = player.getPlayWhenReady();
                if (current != null) {
                    prepare(current, position, playing);
                }
            }
        });

        initializePlayer();
    }

    private Button makeButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(13);
        b.setFocusable(true);
        b.setFocusableInTouchMode(true);
        return b;
    }

    private void initializePlayer() {
        DefaultRenderersFactory renderers = new DefaultRenderersFactory(this);
        player = ExoPlayerFactory.newSimpleInstance(this, renderers, new DefaultTrackSelector());
        playerView.setPlayer(player);
        player.addListener(new com.google.android.exoplayer2.Player.EventListener() {
            @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == com.google.android.exoplayer2.Player.STATE_BUFFERING) status.setText("Mise en mémoire tampon…");
                else if (playbackState == com.google.android.exoplayer2.Player.STATE_READY) status.setText("Lecture prête. Bitstream AC-3/E-AC-3 uniquement si la sortie HDMI l'autorise.");
                else if (playbackState == com.google.android.exoplayer2.Player.STATE_ENDED) status.setText("Lecture terminée");
            }
            @Override public void onPlayerError(ExoPlaybackException error) {
                status.setText("Erreur de lecture : " + error.getMessage());
            }
        });
    }

    private void openMedia() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        try {
            startActivityForResult(intent, REQUEST_MEDIA);
        } catch (Exception e) {
            Toast.makeText(this, "Sélecteur de fichiers indisponible", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) { }
            prepare(uri, 0, true);
        }
    }

    private void prepare(Uri uri, long position, boolean playWhenReady) {
        if (player == null) initializePlayer();
        String userAgent = Util.getUserAgent(this, "CinemaSurroundPlayer");
        MediaSource source = new ExtractorMediaSource.Factory(
            new DefaultDataSourceFactory(this, userAgent))
            .createMediaSource(uri, uri);
        player.prepare(source);
        player.seekTo(position);
        player.setPlayWhenReady(playWhenReady);
        status.setText(passthroughRequested
            ? "Lecture : négociation automatique du passthrough Dolby AC-3/E-AC-3."
            : "Lecture : passthrough désactivé.");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE && player != null) {
            player.setPlayWhenReady(!player.getPlayWhenReady());
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP && player != null) {
            player.stop();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override protected void onStart() { super.onStart(); }
    @Override protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
