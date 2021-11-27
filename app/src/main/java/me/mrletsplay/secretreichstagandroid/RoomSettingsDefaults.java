package me.mrletsplay.secretreichstagandroid;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.mrletsplay.srweb.game.GameMode;
import me.mrletsplay.srweb.game.RoomSettings;
import me.mrletsplay.srweb.game.state.board.GameBoardAction;
import me.mrletsplay.srweb.game.state.board.GameBoardActionField;

public class RoomSettingsDefaults {

	private static Map<GameMode, Map<String, RoomSettings>> defaults = new HashMap<>();

	public static void load(Context context) {
		try {
			InputStream i = context.getResources().openRawResource(R.raw.defaults);
			int l = i.available();
			byte[] bytes = new byte[l];
			i.read(bytes);
			i.close();
			JSONObject defs = new JSONObject(new String(bytes, StandardCharsets.UTF_8));
			defaults.put(GameMode.SECRET_HITLER, load(defs.getJSONObject("SECRET_HITLER")));
			defaults.put(GameMode.SECRET_REICHSTAG, load(defs.getJSONObject("SECRET_REICHSTAG")));
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
	}

	private static Map<String, RoomSettings> load(JSONObject object) throws JSONException {
		Map<String, RoomSettings> presets = new LinkedHashMap<>();
		Iterator<String> keys = object.keys();
		while(keys.hasNext()) {
			String k = keys.next();
			JSONObject settings = object.getJSONObject(k);
			presets.put(k, loadSettings(settings));
		}
		return presets;
	}

	private static RoomSettings loadSettings(JSONObject settings) throws JSONException {
		RoomSettings s = new RoomSettings();
		s.setLiberalCardCount(settings.getInt("liberalCards"));
		s.setFascistCardCount(settings.getInt("fascistCards"));
		if(settings.has("communistCards")) s.setCommunistCardCount(settings.getInt("communistCards"));

		s.setLiberalBoard(loadBoard(settings.getJSONArray("liberalActions")));
		s.setFascistBoard(loadBoard(settings.getJSONArray("fascistActions")));
		if(settings.has("communistActions")) s.setCommunistBoard(loadBoard(settings.getJSONArray("communistActions")));

		return s;
	}

	private static List<GameBoardActionField> loadBoard(JSONArray board) throws JSONException {
		System.out.println(board);
		List<GameBoardActionField> fields = new ArrayList<>();
		for(int i = 0; i < board.length(); i++) {
			if(board.isNull(i)) continue;
			String a = board.getString(i);
			GameBoardActionField f = new GameBoardActionField();
			f.setFieldIndex(i);
			f.setAction(GameBoardAction.valueOf(a));
			fields.add(f);
		}
		return fields;
	}

	public static Map<String, RoomSettings> getDefaults(GameMode mode) {
		return defaults.get(mode);
	}
}
