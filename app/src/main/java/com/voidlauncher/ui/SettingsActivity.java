package com.voidlauncher.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import com.voidlauncher.R;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView label = new TextView(this);
        label.setText(R.string.settings_title);
        label.setTextColor(Color.WHITE);
        label.setTextSize(18f);
        label.setTypeface(Typeface.MONOSPACE);
        label.setGravity(Gravity.CENTER);

        LinearLayout root = new LinearLayout(this);
        root.setBackgroundColor(Color.BLACK);
        root.setGravity(Gravity.CENTER);
        root.addView(label);

        setContentView(root);
    }
}
