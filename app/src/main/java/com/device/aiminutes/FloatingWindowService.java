package com.device.aiminutes;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FloatingWindowService extends Service {

    private WindowManager windowManager;
    private FrameLayout floatingView;
    private LinearLayout floatingBallWithStatus;
    private LinearLayout floatingBallNoStatus;

    private Handler timerHandler;
    private Runnable timerRunnable;
    private long startTime = 0;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private boolean isShowingStatus = true;

    private TextView tvStatus;
    private TextView tvTime;
    private int initialX, initialY;
    private float initialTouchX, initialTouchY;
    private int currentScreenSide = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        timerHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createFloatingWindow();
        return START_STICKY;
    }

    private void createFloatingWindow() {
        floatingView = new FrameLayout(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View contentView = inflater.inflate(R.layout.floating_ball, floatingView, true);

        floatingBallWithStatus = contentView.findViewById(R.id.floatingBallWithStatus);
        floatingBallNoStatus = contentView.findViewById(R.id.floatingBallNoStatus);
        tvStatus = contentView.findViewById(R.id.tvStatus);
        tvTime = contentView.findViewById(R.id.tvTime);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 300;

        setupTouchListener(params);
        setupClickListener(params);

        windowManager.addView(floatingView, params);

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning && !isPaused) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    tvTime.setText(formatTime(elapsedTime));
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        new Handler(Looper.getMainLooper()).postDelayed(this::switchToNoStatus, 3000);
        
        new Handler(Looper.getMainLooper()).postDelayed(this::dockToScreenEdge, 5000);
    }

    private void setupTouchListener(final WindowManager.LayoutParams params) {
        floatingBallWithStatus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        int screenWidth = windowManager.getDefaultDisplay().getWidth();
                        if (params.x < screenWidth / 2) {
                            params.gravity = Gravity.TOP | Gravity.START;
                            currentScreenSide = 0;
                        } else {
                            params.gravity = Gravity.TOP | Gravity.END;
                            currentScreenSide = 1;
                        }
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });

        floatingBallNoStatus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        int screenWidth = windowManager.getDefaultDisplay().getWidth();
                        if (params.x < screenWidth / 2) {
                            params.gravity = Gravity.TOP | Gravity.START;
                            currentScreenSide = 0;
                        } else {
                            params.gravity = Gravity.TOP | Gravity.END;
                            currentScreenSide = 1;
                        }
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void setupClickListener(final WindowManager.LayoutParams params) {
        floatingBallWithStatus.setOnClickListener(v -> {
            switchToNoStatus();
        });

        floatingBallNoStatus.setOnClickListener(v -> {
            switchToWithStatus();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            stopSelf();
        });
    }

    private void switchToNoStatus() {
        isShowingStatus = false;
        floatingBallWithStatus.setVisibility(View.GONE);
        floatingBallNoStatus.setVisibility(View.VISIBLE);
    }

    private void switchToWithStatus() {
        isShowingStatus = true;
        floatingBallWithStatus.setVisibility(View.VISIBLE);
        floatingBallNoStatus.setVisibility(View.GONE);
        startRecording();
    }

    private void dockToScreenEdge() {
        if (floatingView == null) return;

        WindowManager.LayoutParams params = (WindowManager.LayoutParams) floatingView.getLayoutParams();
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        
        if (currentScreenSide == -1 || currentScreenSide == 0) {
            params.x = 0;
            params.gravity = Gravity.TOP | Gravity.START;
        } else {
            params.x = screenWidth - floatingView.getWidth();
            params.gravity = Gravity.TOP | Gravity.END;
        }
        
        params.y = windowManager.getDefaultDisplay().getHeight() / 2;
        windowManager.updateViewLayout(floatingView, params);
    }

    private void startRecording() {
        isRunning = true;
        isPaused = false;
        startTime = System.currentTimeMillis();
        tvStatus.setText("转写中");
        timerHandler.post(timerRunnable);
    }

    private String formatTime(long milliseconds) {
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
