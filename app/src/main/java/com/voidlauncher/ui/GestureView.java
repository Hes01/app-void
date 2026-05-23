package com.voidlauncher.ui;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

public class GestureView extends View {

    public interface Listener {
        void onTap();
        void onLongPress();
    }

    private Listener listener;
    private boolean longFired = false;

    public GestureView(Context context) {
        super(context);
        setBackgroundColor(android.graphics.Color.TRANSPARENT);
        setLongClickable(true);
        setOnLongClickListener(v -> {
            longFired = true;
            if (listener != null) listener.onLongPress();
            return true;
        });
    }

    public void setListener(Listener l) { this.listener = l; }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        super.onTouchEvent(e);
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:   longFired = false; break;
            case MotionEvent.ACTION_UP:     if (!longFired && listener != null) listener.onTap(); break;
            case MotionEvent.ACTION_CANCEL: longFired = false; break;
        }
        return true;
    }
}
