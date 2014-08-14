package io.supermonkey.crawler.device.android;

import io.supermonkey.crawler.device.Device;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import io.supermonkey.crawler.hierarchy.Selector;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 30.05.14
 */
public class AndroidInput implements Device.Input {

	private static final Log log = LogFactory.getLog(AndroidInput.class);

	private final JsonSelectorTranslator translator = new JsonSelectorTranslator();
	private final JsonWire uiautomatorWire;

	public AndroidInput(JsonWire uiautomatorWire) {
		this.uiautomatorWire = uiautomatorWire;
	}

	@Override
	public void click(Selector selector) {

		try {

			String selectorQuery = translator.translate(selector);
			log.info("Finding element by query: " + selectorQuery);

			String paramContext = "\"context\" : \"dummy\"";
			String paramStrategy = "\"strategy\" : \"dynamic\"";
			String paramSelector = "\"selector\" : " + selectorQuery;
			String paramsFind = "{" + paramContext + ", " + paramStrategy + ", " + paramSelector + "}";

			JSONObject findResponse = toJson(uiautomatorWire.sendCommand("find", paramsFind));
			int elementId = findResponse.getInt("ELEMENT");

			String elementIdParam = "\"elementId\" : \"" + elementId + "\"";
			String paramsClick = "{" + elementIdParam + "}";
			uiautomatorWire.sendCommand("element:click", paramsClick);

		} catch(JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private static JSONObject toJson(String json) {
		try {
			return new JSONObject(json);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

}
