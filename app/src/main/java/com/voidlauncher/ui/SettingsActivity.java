package com.voidlauncher.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.voidlauncher.R;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SettingsActivity extends Activity {

    private static final Map<String, String> MORSE = new HashMap<>();
    static {
        MORSE.put(".-", "A");   MORSE.put("-...", "B"); MORSE.put("-.-.", "C");
        MORSE.put("-..", "D");  MORSE.put(".", "E");    MORSE.put("..-.", "F");
        MORSE.put("--.", "G");  MORSE.put("....", "H"); MORSE.put("..", "I");
        MORSE.put(".---", "J"); MORSE.put("-.-", "K");  MORSE.put(".-..", "L");
        MORSE.put("--", "M");   MORSE.put("-.", "N");   MORSE.put("---", "O");
        MORSE.put(".--.", "P"); MORSE.put("--.-", "Q"); MORSE.put(".-.", "R");
        MORSE.put("...", "S");  MORSE.put("-", "T");    MORSE.put("..-", "U");
        MORSE.put("...-", "V"); MORSE.put(".--", "W");  MORSE.put("-..-", "X");
        MORSE.put("-.--", "Y"); MORSE.put("--..", "Z");
        MORSE.put(".----", "1"); MORSE.put("..---", "2"); MORSE.put("...--", "3");
        MORSE.put("....-", "4"); MORSE.put(".....", "5"); MORSE.put("-....", "6");
        MORSE.put("--...", "7"); MORSE.put("---..", "8"); MORSE.put("----.", "9");
        MORSE.put("-----", "0"); MORSE.put("/", " ");
    }

    private static final String[] WHY = {
        "because 68kb is enough.",
        "because black uses no power.",
        "because the best UI is no UI.",
        "because fast is a feature.",
        "because you already know what you want.",
        "because nothing is also a design decision."
    };

    private TextView tvDecoded;
    private EditText etInput;
    private final Random rng = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setGravity(Gravity.CENTER);
        root.setPadding(40, 40, 40, 40);

        tvDecoded = new TextView(this);
        tvDecoded.setTextColor(Color.GRAY);
        tvDecoded.setTextSize(24f);
        tvDecoded.setTypeface(Typeface.MONOSPACE);
        tvDecoded.setGravity(Gravity.CENTER);
        tvDecoded.setText("");

        etInput = new EditText(this);
        etInput.setBackgroundColor(Color.TRANSPARENT);
        etInput.setTextColor(Color.WHITE);
        etInput.setTextSize(32f);
        etInput.setTypeface(Typeface.MONOSPACE);
        etInput.setGravity(Gravity.CENTER);
        etInput.setHint(".... . .-.. .-.. ---");
        etInput.setHintTextColor(0x33FFFFFF);

        root.addView(tvDecoded);
        root.addView(etInput);

        etInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                decode(s.toString());
            }
        });

        setContentView(root);
        showKeyboard();
    }

    private void decode(String raw) {
        StringBuilder sb = new StringBuilder();
        String[] parts = raw.split("\\s+");
        for (String part : parts) {
            String val = MORSE.get(part);
            if (val != null) sb.append(val);
        }
        String decoded = sb.toString();
        tvDecoded.setText(decoded);

        checkEggs(decoded);
    }

    private void checkEggs(String decoded) {
        if (decoded.equals("HELLO")) {
            egg("hello, human");
        } else if (decoded.equals("WHY")) {
            egg(WHY[rng.nextInt(WHY.length)]);
        } else if (decoded.equals("CREDITS")) {
            egg("hes.");
        } else if (decoded.equals("GS")) {
            egg("and counting.");
        } else if (decoded.equals("MAKELOVE")) {
            egg("not war?");
        }
    }

    private void egg(final String response) {
        etInput.setEnabled(false);
        tvDecoded.setTextColor(Color.WHITE);
        tvDecoded.setText(response);
        
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                finish();
            }
        }, 3000);
    }

    private void showKeyboard() {
        etInput.requestFocus();
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(etInput, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);
    }
}
