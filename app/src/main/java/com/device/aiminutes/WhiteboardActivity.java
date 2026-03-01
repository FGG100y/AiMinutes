package com.device.aiminutes;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WhiteboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TextView textView = new TextView(this);
        textView.setText("白板功能预留\n\n点击返回退出白板");
        textView.setTextSize(18);
        textView.setGravity(android.view.Gravity.CENTER);
        
        setContentView(textView);
    }
}
