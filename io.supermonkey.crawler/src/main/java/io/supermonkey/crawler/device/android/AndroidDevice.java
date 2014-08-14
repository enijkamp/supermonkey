package io.supermonkey.crawler.device.android;

import com.android.ddmlib.*;
import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.supermonkey.crawler.device.*;
import io.supermonkey.crawler.device.Package;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 28.05.14
 */
public class AndroidDevice implements Device {

	public static class ViewId implements View.Id {

		public final String packageName;
		public final String className;
		public final String simpleClassName;

		public ViewId(String packageName, String className) {
			this.packageName = packageName;
			this.className = className;
			this.simpleClassName = toSimpleClassName(className);
		}

		private String toSimpleClassName(String className) {
			String[] parts = className.split("\\.");
			return parts[parts.length - 1];
		}

		@Override
		public String getQualifiedName() {
			return packageName + "/" + className;
		}

		@Override
		public String getShortName() {
			return simpleClassName;
		}

		@Override
		public boolean equals(Object other) {
			if(other instanceof ViewId == false) {
				return false;
			}
			ViewId otherId = (ViewId) other;
			return otherId.getQualifiedName().equals((this.getQualifiedName()));
		}

		@Override
		public int hashCode() {
			return getQualifiedName().hashCode();
		}
	}

	public static class PackageHandle implements Package.Handle {

		public final String packageName;
		public final String activityName;
		public final String launchableActivityName;

		public PackageHandle(String packageName, String activityName, String launchableActivityName) {
			this.packageName = packageName;
			this.activityName = activityName;
			this.launchableActivityName = launchableActivityName;
		}
	}

	public static class AppHandle implements App.Handle {

		public final PackageHandle packageHandle;

		public AppHandle(PackageHandle packageHandle) {
			this.packageHandle = packageHandle;
		}
	}

	private static final Log log = LogFactory.getLog(AndroidDevice.class);

	public static class Factory implements Device.Factory {

		@Override
		public Device create(Query query) {
			return null;
		}

	}
	public static final int PORT_TOOLKIT = 4824;

	public static final int PORT_UI_AUTOMATOR = 4724;
	private final String sdkPath;

	private final IDevice device;
	private final ExecutorService uiautomatorExecutor = createExecutor("uiautomator");

	private final JsonWire uiautomatorWire;
	private final JsonWire toolkitWire;

	private final AndroidApps deviceApps;
	private final AndroidInput deviceInput;
	private final AndroidInspector deviceInspector;

	public AndroidDevice(String sdkPath, IDevice device) {
		this.sdkPath = sdkPath;
		this.device = device;
		this.deviceApps = new AndroidApps(sdkPath, device);
		this.uiautomatorWire = openUiAutomator(deviceApps);
		this.toolkitWire = openToolkit(deviceApps);
		this.deviceInput = new AndroidInput(uiautomatorWire);
		this.deviceInspector = new AndroidInspector(device, uiautomatorWire, toolkitWire);
	}

	@Override
	public AndroidInput getInput() {
		return deviceInput;
	}

	@Override
	public AndroidApps getApps() {
		return deviceApps;
	}

	@Override
	public AndroidInspector getInspector() {
		return deviceInspector;
	}

	public IDevice getIDevice() {
		return device;
	}

	private JsonWire openToolkit(AndroidApps appRobot) {
		appRobot.installPackage(new File("/home/enijkamp/crawler-1.0.0-SNAPSHOT.apk"));
		appRobot.forwardPort(PORT_TOOLKIT, PORT_TOOLKIT);
		appRobot.launchService("org.testobject.android.crawler/.Main");
		log.info("Establishing channel to toolkit");
		JsonWire wire = openWire(PORT_TOOLKIT);
		log.info("Established channel to toolkit");

		return wire;
	}

	private JsonWire openWire(int port) {
		long timeout = now() + (60 * 1000);
		while (now() < timeout) {
			try {
				Thread.sleep(500);
				return connectTo(port);
			} catch(Throwable e) {
				log.debug(e.getMessage());
			}
		}

		throw new IllegalStateException("Cannot connect to toolkit");
	}

	private static JsonWire connectTo(int port) throws IOException, JSONException {

		JsonWire wire = new JsonWire(new java.net.Socket("localhost", port));
		String response = wire.sendCommand("ping");
		Preconditions.checkArgument(response.equals("pong"));

		return wire;
	}

	private JsonWire openUiAutomator(final AndroidApps appRobot) {
		this.uiautomatorExecutor.execute(new Runnable() {
			@Override
			public void run() {
				appRobot.pushFile("/home/enijkamp/uiautomator.jar", "/sdcard/uiautomator.jar");
				appRobot.executeShellCommand("/system/bin/uiautomator runtest /sdcard/uiautomator.jar", new LogShellReceiver(log), 0);
			}
		});

		appRobot.forwardPort(PORT_UI_AUTOMATOR, PORT_UI_AUTOMATOR);
		log.info("Establishing channel to uiautomator");
		JsonWire channel = openWire(PORT_UI_AUTOMATOR);
		log.info("Established channel to uiautomator");

		return channel;
	}

	@Override
	public void close() {
		Closeables.closeQuietly(this.toolkitWire);
		Closeables.closeQuietly(this.uiautomatorWire);
		Closeables.closeQuietly(new Closeable() {
			@Override
			public void close() throws IOException {
				uiautomatorExecutor.shutdownNow();
			}
		});
	}

	private static ExecutorService createExecutor(String name) {
		ThreadFactory threadFactory = new ThreadFactoryBuilder()
				.setNameFormat(name)
				.setUncaughtExceptionHandler(new LogExceptionHandler(log))
				.build();

		return Executors.newSingleThreadExecutor(threadFactory);
	}

	private static long now() {
		return java.lang.System.currentTimeMillis();
	}
}
