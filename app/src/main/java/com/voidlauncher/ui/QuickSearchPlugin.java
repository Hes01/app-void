package com.voidlauncher.ui;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import com.voidlauncher.core.CommandRouter;
import com.voidlauncher.data.AliasRepository;
import java.util.List;

class QuickSearchPlugin {

    private final LauncherActivity launcher;
    private final AliasRepository  aliases;
    private final Dialog           dialog;
    private final List<String>     filteredNames;
    private final List<String>     filteredPkgs;

    QuickSearchPlugin(LauncherActivity launcher, AliasRepository aliases, Dialog dialog,
                      List<String> filteredNames, List<String> filteredPkgs) {
        this.launcher      = launcher; this.aliases       = aliases;
        this.dialog        = dialog;
        this.filteredNames = filteredNames; this.filteredPkgs = filteredPkgs;
    }

    void routeCommand(String raw, QuickSearchAdapter adapter) {
        CommandRouter cmd = CommandRouter.parse(raw);
        String pkg = aliases.resolve(cmd.alias);
        if (pkg == null) { adapter.notifyDataSetChanged(); return; }
        VibrationFeedback.onCommand(hapticView());
        if (cmd.isUninstall()) {
            dialog.dismiss();
            Intent del = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + pkg));
            del.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launcher.startActivity(del);
            return;
        }
        if (cmd.isList())       { queryPlugin(pkg, adapter); return; }
        if (cmd.isDeleteItem()) { deletePlugin(pkg, cmd.deleteId(), adapter); return; }
        String rawArgs = cmd.rawArgs();
        filteredNames.add(cmd.alias + (rawArgs != null ? "  " + rawArgs : "  _"));
        filteredPkgs.add(rawArgs != null ? pkg + "\t" + rawArgs : pkg);
        adapter.notifyDataSetChanged();
    }

    void queryPlugin(String pkg, QuickSearchAdapter adapter) {
        Uri uri = Uri.parse("content://" + pkg + ".provider/items");
        try (Cursor c = launcher.getContentResolver().query(uri, null, null, null, null)) {
            if (c == null) return;
            filteredNames.clear(); filteredPkgs.clear();
            while (c.moveToNext()) {
                int    id    = c.getInt(c.getColumnIndexOrThrow("_id"));
                String title = c.getString(c.getColumnIndexOrThrow("title"));
                filteredNames.add(id + "  →  " + title);
                filteredPkgs.add(pkg + ":" + id);
            }
        } catch (Exception ignored) {}
        adapter.notifyDataSetChanged();
    }

    private void deletePlugin(String pkg, String id, QuickSearchAdapter adapter) {
        Uri uri = Uri.parse("content://" + pkg + ".provider/items");
        try { launcher.getContentResolver().delete(uri, "_id=?", new String[]{id}); }
        catch (Exception ignored) {}
        queryPlugin(pkg, adapter);
    }

    private View hapticView() { return launcher.getWindow().getDecorView(); }
}
