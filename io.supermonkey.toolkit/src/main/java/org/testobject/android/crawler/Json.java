package org.testobject.android.crawler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Erik Nijkamp
 * @since 30.05.14
 */
public class Json {

	public static JSONObject toJson(String json) {
		try {
			return new JSONObject(json);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

}
