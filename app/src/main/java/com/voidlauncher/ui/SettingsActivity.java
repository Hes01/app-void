package com.voidlauncher.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.voidlauncher.R;
import com.voidlauncher.data.GestureMapping;
import com.voidlauncher.data.GestureRepository;
import java.util.List;

public class SettingsActivity extends Activity {

    private static final int REQ_RECORD = 1;

    private GestureRepository  repo;
    private List<GestureMapping> mappings;
    private ListView           listView;
    private TextView           emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        repo      = new GestureRepository(this);
        listView  = findViewById(R.id.listGestures);
        emptyText = findViewById(R.id.emptyText);

        listView.setEmptyView(emptyText);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> p, View v, final int pos, long id) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setMessage(R.string.delete_confirm)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface d, int w) {
                                repo.delete(mappings.get(pos).id);
                                loadData();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return true;
            }
        });

        findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivityForResult(
                        new Intent(SettingsActivity.this, RecordGestureActivity.class),
                        REQ_RECORD);
            }
        });

        loadData();
    }

    @Override
    protected void onActivityResult(int req, int result, Intent data) {
        if (req == REQ_RECORD && result == RESULT_OK) loadData();
    }

    private void loadData() {
        mappings = repo.getAll();
        String[] labels = new String[mappings.size()];
        for (int i = 0; i < mappings.size(); i++) {
            labels[i] = mappings.get(i).appName + "  " + sigToArrows(mappings.get(i).signatures[0]);
        }
        listView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, labels));
    }

    private String sigToArrows(int[] sig) {
        String[] arrows = {"→", "↗", "↑", "↖", "←", "↙", "↓", "↘"};
        StringBuilder sb = new StringBuilder();
        for (int d : sig) sb.append(arrows[d % 8]);
        return sb.toString();
    }
}
