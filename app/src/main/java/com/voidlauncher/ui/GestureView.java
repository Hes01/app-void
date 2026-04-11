package com.voidlauncher.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GestureView extends View {

    public interface Listener {
        void onTap();
    }

    private Listener listener;

    public GestureView(Context context) { this(context, null); }

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(android.graphics.Color.TRANSPARENT);
    }

    public void setListener(Listener l) { this.listener = l; }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getActionMasked() == MotionEvent.ACTION_UP) {
            if (listener != null) listener.onTap();
        }
        return true;
    }
}
