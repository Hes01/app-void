package com.voidlauncher.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GestureView extends View {

    public interface Listener {
        void onTwoFingerTap();
    }

    private Listener listener;
    private boolean  fired = false;

    public GestureView(Context context) { this(context, null); }

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(android.graphics.Color.TRANSPARENT);
    }

    public void setListener(Listener l) { this.listener = l; }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                fired = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (!fired && e.getPointerCount() == 2) {
                    fired = true;
                    if (listener != null) listener.onTwoFingerTap();
                }
                break;
            case MotionEvent.ACTION_UP:
                fired = false;
                break;
        }
        return true;
    }
}
