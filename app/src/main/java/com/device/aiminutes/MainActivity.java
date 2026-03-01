package com.device.aiminutes;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView tvTimer;
    private TextView tvSourceText, tvTranslatedText;
    private TextView tvSourceTextHorizontal, tvTranslatedTextHorizontal;
    private ImageButton btnStartPause;
    private ImageButton btnSwapLanguages, btnSwapLanguagesHorizontal, btnSwapLanguagesMain;
    private ImageButton btnToggleLayout, btnToggleLayoutHorizontal;
    private ImageButton btnSpeakerRecognition;
    private ImageButton btnWhiteboard;
    private Button btnMode;
    private Spinner spinnerSourceLanguage, spinnerTargetLanguage;
    private Spinner spinnerFontSize;
    private SeekBar seekBarVertical, seekBarHorizontal;
    private LinearLayout layoutVertical, layoutHorizontal;
    private RelativeLayout splitterContainerVertical, splitterContainerHorizontal;

    private Handler timerHandler;
    private Runnable timerRunnable;
    private long startTime = 0;
    private boolean isRunning = false;
    private boolean isPaused = false;

    private boolean isVerticalLayout = true;
    private boolean isSpeakerRecognitionEnabled = false;
    private int currentFontSize = 16;

    private String[] languages = {"zh-CN", "en-US", "ja-JP", "ko-KR", "fr-FR", "de-DE", "es-ES", "ru-RU"};
    private String[] languageNames = {"中文", "英语", "日语", "韩语", "法语", "德语", "西班牙语", "俄语"};
    private String[] fontSizes = {"14sp", "16sp", "18sp", "20sp", "24sp"};
    private int[] fontSizeValues = {14, 16, 18, 20, 24};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupSpinners();
        setupListeners();
        setupTimer();
    }

    private void initViews() {
        tvTimer = findViewById(R.id.tvTimer);

        tvSourceText = findViewById(R.id.tvSourceText);
        tvTranslatedText = findViewById(R.id.tvTranslatedText);
        tvSourceTextHorizontal = findViewById(R.id.tvSourceTextHorizontal);
        tvTranslatedTextHorizontal = findViewById(R.id.tvTranslatedTextHorizontal);

        btnStartPause = findViewById(R.id.btnStartPause);
        btnSwapLanguages = findViewById(R.id.btnSwapLanguages);
        btnSwapLanguagesHorizontal = findViewById(R.id.btnSwapLanguagesHorizontal);
        btnSwapLanguagesMain = findViewById(R.id.btnSwapLanguagesMain);
        btnToggleLayout = findViewById(R.id.btnToggleLayout);
        btnToggleLayoutHorizontal = findViewById(R.id.btnToggleLayoutHorizontal);
        btnSpeakerRecognition = findViewById(R.id.btnSpeakerRecognition);
        btnWhiteboard = findViewById(R.id.btnWhiteboard);
        btnMode = findViewById(R.id.btnMode);

        spinnerSourceLanguage = findViewById(R.id.spinnerSourceLanguage);
        spinnerTargetLanguage = findViewById(R.id.spinnerTargetLanguage);
        spinnerFontSize = findViewById(R.id.spinnerFontSize);

        seekBarVertical = findViewById(R.id.seekBarVertical);
        seekBarHorizontal = findViewById(R.id.seekBarHorizontal);

        layoutVertical = findViewById(R.id.layoutVertical);
        layoutHorizontal = findViewById(R.id.layoutHorizontal);
        splitterContainerVertical = findViewById(R.id.splitterContainerVertical);
        splitterContainerHorizontal = findViewById(R.id.splitterContainerHorizontal);
    }

    private void setupSpinners() {
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, languageNames);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSourceLanguage.setAdapter(languageAdapter);
        spinnerTargetLanguage.setAdapter(languageAdapter);
        spinnerTargetLanguage.setSelection(1);

        ArrayAdapter<String> fontSizeAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, fontSizes);
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFontSize.setAdapter(fontSizeAdapter);
        spinnerFontSize.setSelection(1);
    }

    private void setupListeners() {
        btnStartPause.setOnClickListener(v -> toggleStartPause());

        btnSwapLanguages.setOnClickListener(v -> swapLanguages());
        btnSwapLanguagesHorizontal.setOnClickListener(v -> swapLanguages());
        btnSwapLanguagesMain.setOnClickListener(v -> swapLanguages());

        btnToggleLayout.setOnClickListener(v -> toggleLayout());
        btnToggleLayoutHorizontal.setOnClickListener(v -> toggleLayout());

        btnSpeakerRecognition.setOnClickListener(v -> toggleSpeakerRecognition());

        btnWhiteboard.setOnClickListener(v -> openWhiteboard());

        btnMode.setOnClickListener(v -> showModeMenu(v));

        spinnerFontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFontSize = fontSizeValues[position];
                updateFontSize();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        seekBarVertical.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextAreaRatioVertical(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarHorizontal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextAreaRatioHorizontal(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
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
        btnStartPause.setColorFilter(getResources().getColor(android.R.color.white, null));
        timerHandler.post(timerRunnable);
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            btnStartPause.setImageResource(android.R.drawable.ic_media_play);
            btnStartPause.setColorFilter(getResources().getColor(R.color.error, null));
        } else {
            btnStartPause.setImageResource(android.R.drawable.ic_media_pause);
            btnStartPause.setColorFilter(getResources().getColor(android.R.color.white, null));
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

    private void stopRecording() {
        isRunning = false;
        isPaused = false;
        btnStartPause.setImageResource(android.R.drawable.ic_media_play);
        btnStartPause.setColorFilter(getResources().getColor(android.R.color.white, null));
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void swapLanguages() {
        int sourcePos = spinnerSourceLanguage.getSelectedItemPosition();
        int targetPos = spinnerTargetLanguage.getSelectedItemPosition();
        spinnerSourceLanguage.setSelection(targetPos);
        spinnerTargetLanguage.setSelection(sourcePos);

        String sourceText = tvSourceText.getText().toString();
        String translatedText = tvTranslatedText.getText().toString();
        tvSourceText.setText(translatedText);
        tvTranslatedText.setText(sourceText);

        String sourceTextH = tvSourceTextHorizontal.getText().toString();
        String translatedTextH = tvTranslatedTextHorizontal.getText().toString();
        tvSourceTextHorizontal.setText(translatedTextH);
        tvTranslatedTextHorizontal.setText(sourceTextH);
    }

    private void toggleLayout() {
        isVerticalLayout = !isVerticalLayout;
        if (isVerticalLayout) {
            layoutVertical.setVisibility(View.VISIBLE);
            layoutHorizontal.setVisibility(View.GONE);
        } else {
            layoutVertical.setVisibility(View.GONE);
            layoutHorizontal.setVisibility(View.VISIBLE);

            splitterContainerHorizontal.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int height = splitterContainerHorizontal.getHeight();
                        if (height > 0) {
                            seekBarHorizontal.getLayoutParams().width = height;
                            seekBarHorizontal.requestLayout();
                            splitterContainerHorizontal.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
        }
    }

    private void toggleSpeakerRecognition() {
        isSpeakerRecognitionEnabled = !isSpeakerRecognitionEnabled;
        if (isSpeakerRecognitionEnabled) {
            btnSpeakerRecognition.setColorFilter(getResources().getColor(R.color.primary, null));
        } else {
            btnSpeakerRecognition.setColorFilter(getResources().getColor(R.color.text_secondary, null));
        }
    }

    private void updateFontSize() {
        tvSourceText.setTextSize(currentFontSize);
        tvTranslatedText.setTextSize(currentFontSize);
        tvSourceTextHorizontal.setTextSize(currentFontSize);
        tvTranslatedTextHorizontal.setTextSize(currentFontSize);
    }

    private void updateTextAreaRatioVertical(int progress) {
        View sourceView = layoutVertical.getChildAt(0);
        View splitter = layoutVertical.getChildAt(1);
        View targetView = layoutVertical.getChildAt(2);

        if (sourceView != null && targetView != null) {
            LinearLayout.LayoutParams sourceParams = (LinearLayout.LayoutParams) sourceView.getLayoutParams();
            LinearLayout.LayoutParams targetParams = (LinearLayout.LayoutParams) targetView.getLayoutParams();
            sourceParams.weight = progress;
            targetParams.weight = 100 - progress;
            sourceView.setLayoutParams(sourceParams);
            targetView.setLayoutParams(targetParams);
        }
    }

    private void updateTextAreaRatioHorizontal(int progress) {
        View sourceView = layoutHorizontal.getChildAt(0);
        View splitter = layoutHorizontal.getChildAt(1);
        View targetView = layoutHorizontal.getChildAt(2);

        if (sourceView != null && targetView != null) {
            LinearLayout.LayoutParams sourceParams = (LinearLayout.LayoutParams) sourceView.getLayoutParams();
            LinearLayout.LayoutParams targetParams = (LinearLayout.LayoutParams) targetView.getLayoutParams();
            sourceParams.weight = progress;
            targetParams.weight = 100 - progress;
            sourceView.setLayoutParams(sourceParams);
            targetView.setLayoutParams(targetParams);
        }
    }

    private void openWhiteboard() {
        Intent intent = new Intent(this, WhiteboardActivity.class);
        startActivity(intent);
    }

    private void showModeMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 0, 0, "全文模式");
        popup.getMenu().add(0, 1, 1, "字幕模式");
        popup.getMenu().add(0, 2, 2, "悬浮模式");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    btnMode.setText("全文模式");
                    return true;
                case 1:
                    btnMode.setText("字幕模式");
                    startActivity(new Intent(this, SubtitleActivity.class));
                    return true;
                case 2:
                    btnMode.setText("悬浮模式");
                    startService(new Intent(this, FloatingWindowService.class));
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
    }

    private String formatTime(long milliseconds) {
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
