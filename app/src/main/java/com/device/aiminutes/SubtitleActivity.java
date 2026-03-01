package com.device.aiminutes;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SubtitleActivity extends AppCompatActivity {

    private TextView tvSubtitleSource;
    private TextView tvSubtitleTranslated;
    private TextView tvTimer;
    private ImageButton btnStartPause;
    private ImageButton btnBackToMain;

    private Handler timerHandler;
    private Runnable timerRunnable;
    private long startTime = 0;
    private boolean isRunning = false;
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | 
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | 
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_subtitle);

        initViews();
        setupListeners();
        setupTimer();
    }

    private void initViews() {
        tvSubtitleSource = findViewById(R.id.tvSubtitleSource);
        tvSubtitleTranslated = findViewById(R.id.tvSubtitleTranslated);
        tvTimer = findViewById(R.id.tvTimer);
        btnStartPause = findViewById(R.id.btnStartPause);
        btnBackToMain = findViewById(R.id.btnBackToMain);
    }

    private void setupListeners() {
        btnStartPause.setOnClickListener(v -> toggleStartPause());
        btnBackToMain.setOnClickListener(v -> finish());
    }

    private void setupTimer() {
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning && !isPaused) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    tvTimer.setText(formatTime(elapsedTime));
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
    }

    private void toggleStartPause() {
        if (!isRunning) {
            startRecording();
        } else {
            togglePause();
        }
    }

    private void startRecording() {
        isRunning = true;
        isPaused = false;
        startTime = System.currentTimeMillis();
        btnStartPause.setImageResource(android.R.drawable.ic_media_pause);
        timerHandler.post(timerRunnable);
        
        tvSubtitleSource.setText("正在识别语音...");
        tvSubtitleTranslated.setText("正在翻译...");
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            btnStartPause.setImageResource(android.R.drawable.ic_media_play);
        } else {
            btnStartPause.setImageResource(android.R.drawable.ic_media_pause);
            startTime = System.currentTimeMillis() - getCurrentElapsedTime();
            timerHandler.post(timerRunnable);
        }
    }

    private long getCurrentElapsedTime() {
        String timeStr = tvTimer.getText().toString();
        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return (hours * 3600 + minutes * 60 + seconds) * 1000L;
    }

    private String formatTime(long milliseconds) {
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void updateSubtitle(String sourceText, String translatedText) {
        runOnUiThread(() -> {
            tvSubtitleSource.setText(sourceText);
            tvSubtitleTranslated.setText(translatedText);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }
}
