package io.supermonkey.crawler.device.android;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.TimeoutException;
import io.supermonkey.crawler.device.App;
import io.supermonkey.crawler.device.View;
import io.supermonkey.crawler.hierarchy.*;
import org.json.JSONException;
import org.json.JSONObject;
import io.supermonkey.crawler.device.Device;
import io.supermonkey.crawler.util.Retry;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 30.05.14
 */
public class AndroidInspector implements Device.Inspector {

	private final IDevice device;
	private final JsonWire uiautomatorWire;
	private final JsonWire toolkitWire;

	public AndroidInspector(IDevice device, JsonWire uiautomatorWire, JsonWire toolkitWire) {
		this.device = device;
		this.uiautomatorWire = uiautomatorWire;
		this.toolkitWire = toolkitWire;
	}

	@Override
	public void waitForIdle() {
		uiautomatorWire.sendCommand("waitForIdle", "{ \"timeout\" : 10 }");
	}

	@Override
	public Node dumpHierarchy() {
		final int sleep = 1000;
		final int attempts = 10;

		return Retry.retry(new Callable<Node>() {
			@Override
			public Node call() throws Exception {
				String uidumpXml = uiautomatorWire.sendCommand("dumpWindowHierarchy");
				Node node = new UiDumpXmlParser().parse(uidumpXml);
				return overwriteValidSelectors(node);
			}
		}, attempts, sleep);
	}

	// FIXME all clickable=true should be reachable (en)
	// FIXME remove this
	private Node overwriteValidSelectors(Node source) {

		final Validators.Validator1<Selector> selectorValidator = new JsonSelectorTranslator();

		Transform.Mutator mutator = new Transform.Mutator() {
			@Override
			public Element mutate(Element element) {
				if(selectorValidator.isValid(new Selector(element))) {
					return element;
				} else {
					return new Element(element.getAttributes(), element.getType(), element.isScrollable(), false);
				}
			}
		};
		Node target = Transform.transform(source, mutator);

		return target;
	}

	@Override
	public View.Id getCurrentViewId() {
		try {
			JSONObject response = toJson(toolkitWire.sendCommand("current_activity"));
			String packageName = response.getString("package");
			String className = response.getString("class");

			return new AndroidDevice.ViewId(packageName, className);
		} catch(JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public View.Id getHomeViewId(App.Handle app) {
		AndroidDevice.AppHandle appHandle = (AndroidDevice.AppHandle) app;
		String packageName = appHandle.packageHandle.packageName;
		String className = appHandle.packageHandle.activityName;

		return new AndroidDevice.ViewId(packageName, className);
	}

	@Override
	public BufferedImage takeScreenshot() {
		try {
			return toBufferedImage(device.getScreenshot());
		} catch (AdbCommandRejectedException | TimeoutException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private BufferedImage toBufferedImage(RawImage rawImage) {
		BufferedImage image = new BufferedImage(rawImage.width, rawImage.height, BufferedImage.TYPE_INT_ARGB);

		int index = 0;
		int indexInc = rawImage.bpp >> 3;
		for (int y = 0; y < rawImage.height; y++) {
			for (int x = 0; x < rawImage.width; x++) {
				int value = rawImage.getARGB(index) | 0xff000000;
				index += indexInc;
				image.setRGB(x, y, value);
			}
		}

		return image;
	}

	private static JSONObject toJson(String json) {
		try {
			return new JSONObject(json);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
