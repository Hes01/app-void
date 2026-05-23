package com.voidlauncher.ui;

import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.View;

class VibrationFeedback {

    static void onOpen(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    static void onLaunch(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        } else {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
    }

    static void onNoResults(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            v.performHapticFeedback(HapticFeedbackConstants.REJECT);
        } else {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
    }

    static void onCommand(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }
}
