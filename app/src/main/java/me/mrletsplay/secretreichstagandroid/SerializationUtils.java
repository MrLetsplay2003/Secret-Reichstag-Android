package me.mrletsplay.secretreichstagandroid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SerializationUtils {

	@SuppressWarnings("unchecked")
	public static <T> T cast(JSONObject obj) {
		try {
			Class<T> javaClass = (Class<T>) Class.forName(obj.getString("_class"));
			if(javaClass.isEnum()) {
				return (T) javaClass.getMethod("valueOf", String.class).invoke(null, obj.getString("jsEnumName"));
			}
			T t = javaClass.newInstance();
			Iterator<String> k = obj.keys();
			while(k.hasNext()) {
				String key = k.next();
				Field f = null;
				try {
					f = t.getClass().getDeclaredField(key);
				}catch(NoSuchFieldException e) {
					continue;
				}
				f.setAccessible(true);
				if(obj.isNull(key)) {
					f.set(t, null);
					continue;
				}
				Object o = obj.get(key);
				if(o instanceof JSONObject) {
					if(f.getType().equals(JSONObject.class)) {
						f.set(t, o);
					}else {
						f.set(t, cast((JSONObject) o));
					}
				}else if(o instanceof JSONArray) {
					f.set(t, castList((JSONArray) o));
				}else {
					f.set(t, o);
				}
			}
			return t;
		}catch(JSONException | ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> List<T> castList(JSONArray arr) {
		try {
			List<T> ts = new ArrayList<>();
			for (int i = 0; i < arr.length(); i++) {
				JSONObject obj = (JSONObject) arr.getJSONObject(i);
				ts.add(cast(obj));
			}
			return ts;
		}catch(JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public static JSONObject serialize(Object obj) {
		try {
			if(obj == null) return null;
			JSONObject j = new JSONObject();
			j.put("_class", obj.getClass().getCanonicalName());
			if(obj instanceof Enum<?>) {
				j.put("jsEnumName", ((Enum<?>) obj).name());
				return j;
			}

			for(Field f : obj.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				Object val = f.get(obj);
				if(val instanceof List<?>) {
					j.put(f.getName(), serializeList((List<?>) f.get(obj)));
				}else if(val instanceof Number || val instanceof Boolean || val instanceof String) {
					j.put(f.getName(), val);
				}else {
					j.put(f.getName(), serialize(f.get(obj)));
				}
			}

			return j;
		}catch(JSONException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static JSONArray serializeList(List<?> list) {
		if(list == null) return null;
		JSONArray arr = new JSONArray();
		for(Object o : list) {
			arr.put(serialize(o));
		}
		return arr;
	}

}
