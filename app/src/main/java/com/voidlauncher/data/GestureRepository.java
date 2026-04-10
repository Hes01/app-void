package com.voidlauncher.data;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GestureRepository {

    private static final String PREFS = "void_data";
    private static final String KEY   = "gestures";

    private final SharedPreferences prefs;

    public GestureRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<GestureMapping> getAll() {
        List<GestureMapping> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs.getString(KEY, "[]"));
            for (int i = 0; i < arr.length(); i++) list.add(fromJson(arr.getJSONObject(i)));
        } catch (JSONException ignored) {}
        return list;
    }

    public void save(GestureMapping m) {
        List<GestureMapping> list = getAll();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id.equals(m.id)) { list.set(i, m); persist(list); return; }
        }
        list.add(m);
        persist(list);
    }

    public void delete(String id) {
        List<GestureMapping> list = getAll();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id.equals(id)) { list.remove(i); break; }
        }
        persist(list);
    }

    public static String newId() { return UUID.randomUUID().toString(); }

    private void persist(List<GestureMapping> list) {
        try {
            JSONArray arr = new JSONArray();
            for (GestureMapping m : list) arr.put(toJson(m));
            prefs.edit().putString(KEY, arr.toString()).apply();
        } catch (JSONException ignored) {}
    }

    private JSONObject toJson(GestureMapping m) throws JSONException {
        // sigs: array de arrays [[1,2],[1,2],[1,3]]
        JSONArray sigs = new JSONArray();
        for (int[] sig : m.signatures) {
            JSONArray s = new JSONArray();
            for (int d : sig) s.put(d);
            sigs.put(s);
        }
        JSONObject o = new JSONObject();
        o.put("id", m.id);
        o.put("pkg", m.appPackage);
        o.put("name", m.appName);
        o.put("sigs", sigs);
        return o;
    }

    private GestureMapping fromJson(JSONObject o) throws JSONException {
        JSONArray sigsArr = o.getJSONArray("sigs");
        int[][] sigs = new int[sigsArr.length()][];
        for (int i = 0; i < sigsArr.length(); i++) {
            JSONArray s = sigsArr.getJSONArray(i);
            sigs[i] = new int[s.length()];
            for (int j = 0; j < s.length(); j++) sigs[i][j] = s.getInt(j);
        }
        return new GestureMapping(o.getString("id"), o.getString("pkg"), o.getString("name"), sigs);
    }
}
