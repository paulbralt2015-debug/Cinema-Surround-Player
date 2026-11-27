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
import android.widget.VideoView;
import android.widget.MediaController;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQUEST_VIDEO = 41;
    private VideoView videoView;
    private TextView status;
    private Button playButton;

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
        root.setPadding(28, 18, 28, 18);

        TextView title = new TextView(this);
        title.setText("CINEMA SURROUND PLAYER");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(title, new LinearLayout.LayoutParams(-1, 56));

        videoView = new VideoView(this);
        videoView.setBackgroundColor(Color.BLACK);
        LinearLayout.LayoutParams videoParams =
            new LinearLayout.LayoutParams(-1, 0, 1f);
        videoParams.setMargins(0, 12, 0, 12);
        root.addView(videoView, videoParams);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);

        Button open = makeButton("OUVRIR");
        playButton = makeButton("LECTURE / PAUSE");
        Button stop = makeButton("STOP");
        controls.addView(open);
        controls.addView(playButton);
        controls.addView(stop);
        root.addView(controls, new LinearLayout.LayoutParams(-1, 64));

        status = new TextView(this);
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(14);
        status.setText("Prêt — Android 6.0+");
        status.setPadding(4, 12, 4, 4);
        root.addView(status, new LinearLayout.LayoutParams(-1, 42));

        setContentView(root);

        open.setOnClickListener(v -> openVideo());
        playButton.setOnClickListener(v -> togglePlayback());
        stop.setOnClickListener(v -> {
            if (videoView != null) {
                videoView.stopPlayback();
                status.setText("Lecture arrêtée");
            }
        });

        videoView.setOnPreparedListener(mp -> {
            status.setText("Vidéo prête — sortie audio gérée par Android");
            videoView.start();
        });
        videoView.setOnErrorListener((mp, what, extra) -> {
            status.setText("Erreur de lecture (" + what + "/" + extra + ")");
            return true;
        });
    }

    private Button makeButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(16);
        b.setFocusable(true);
        b.setFocusableInTouchMode(true);
        return b;
    }

    private void openVideo() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        try {
            startActivityForResult(intent, REQUEST_VIDEO);
        } catch (Exception e) {
            Toast.makeText(this, "Sélecteur de fichiers indisponible", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) { }
                videoView.setVideoURI(uri);
                status.setText("Chargement de la vidéo…");
            }
        }
    }

    private void togglePlayback() {
        if (videoView.isPlaying()) {
            videoView.pause();
            status.setText("Pause");
        } else {
            videoView.start();
            status.setText("Lecture");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            togglePlayback();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            videoView.stopPlayback();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
