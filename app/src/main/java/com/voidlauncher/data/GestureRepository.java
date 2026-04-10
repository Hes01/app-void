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

    public static String newId() {
        return UUID.randomUUID().toString();
    }

    private void persist(List<GestureMapping> list) {
        try {
            JSONArray arr = new JSONArray();
            for (GestureMapping m : list) arr.put(toJson(m));
            prefs.edit().putString(KEY, arr.toString()).apply();
        } catch (JSONException ignored) {}
    }

    private JSONObject toJson(GestureMapping m) throws JSONException {
        JSONArray sig = new JSONArray();
        for (int d : m.signature) sig.put(d);
        JSONObject o = new JSONObject();
        o.put("id", m.id);
        o.put("pkg", m.appPackage);
        o.put("name", m.appName);
        o.put("sig", sig);
        return o;
    }

    private GestureMapping fromJson(JSONObject o) throws JSONException {
        JSONArray sigArr = o.getJSONArray("sig");
        int[] sig = new int[sigArr.length()];
        for (int i = 0; i < sigArr.length(); i++) sig[i] = sigArr.getInt(i);
        return new GestureMapping(o.getString("id"), o.getString("pkg"), o.getString("name"), sig);
    }
}
