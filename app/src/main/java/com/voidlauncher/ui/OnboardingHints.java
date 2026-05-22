package com.voidlauncher.ui;

import android.animation.Animator;
import com.hes01.voidlauncher.R;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

class OnboardingHints {

    private static final String PREF = "void_onboarding";
    private static final String KEY  = "done";

    private static String[] hints(Context ctx) {
        return new String[]{
            ctx.getString(R.string.hint_tap),
            ctx.getString(R.string.hint_swipe),
            ctx.getString(R.string.hint_all),
            ctx.getString(R.string.hint_void),
            ctx.getString(R.string.hint_alias_launch)
        };
    }

    static boolean isDone(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                  .getBoolean(KEY, false);
    }

    static void show(FrameLayout root) {
        Context ctx = root.getContext();

        TextView tv = new TextView(ctx);
        tv.setTextColor(VoidTheme.FG3);
        tv.setTextSize(VoidTheme.TEXT_MD);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setLetterSpacing(0.05f);
        tv.setGravity(Gravity.CENTER);
        tv.setAlpha(0f);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        lp.bottomMargin = QuickSearchLayout.dp(ctx, 80);
        root.addView(tv, lp);

        cycleHint(root, tv, hints(ctx), 0);
    }

    private static void cycleHint(FrameLayout root, TextView tv, String[] hints, int index) {
        if (index >= hints.length) {
            tv.animate().alpha(0f).setDuration(600)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override public void onAnimationEnd(Animator a) {
                            root.removeView(tv);
                            root.getContext()
                                .getSharedPreferences(PREF, Context.MODE_PRIVATE)
                                .edit().putBoolean(KEY, true).apply();
                        }
                    }).start();
            return;
        }
        tv.setText(hints[index]);
        tv.animate().alpha(1f).setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator a) {
                        tv.postDelayed(() ->
                            tv.animate().alpha(0f).setDuration(500)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override public void onAnimationEnd(Animator a2) {
                                            cycleHint(root, tv, hints, index + 1);
                                        }
                                    }).start(),
                        2000);
                    }
                }).start();
    }
}
