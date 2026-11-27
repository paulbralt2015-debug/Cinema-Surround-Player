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
    private Uri currentUri;

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
        Button passthrough = makeButton("DOLBY HDMI : AUTO");
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
        passthrough.setOnClickListener(v -> status.setText(
            "AC-3/E-AC-3 : passthrough automatique si le téléviseur déclare cette sortie compatible."
        ));
