package io.supermonkey.crawler.device.android;

import com.android.ddmlib.*;
import io.supermonkey.crawler.device.*;
import io.supermonkey.crawler.device.Package;
import org.apache.commons.logging.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 30.05.14
 */
public class AndroidApps implements Device.Apps {

	private static final org.apache.commons.logging.Log log = LogFactory.getLog(AndroidApps.class);

	public static final String START_SERVICE_COMMAND_LEVEL_16 = "am startservice -n";
	public static final String START_SERVICE_COMMAND_LEVEL_17 = "am startservice --user 0 -n";

	private final IDevice device;
	private final String sdkPath;

	public AndroidApps(String sdkPath, IDevice device) {
		this.device = device;
		this.sdkPath = sdkPath;
	}

	@Override
	public Package.Handle installApp(File file) {

		installPackage(file);

		String packageId = ApkUtility.getPackageId(sdkPath, file.getPath());
		String activityId = ApkUtility.getLaunchableActivity(sdkPath, file.getPath());
		String launchableActivity = ApkUtility.getQualifiedLaunchableActivity(packageId, activityId);

		return new AndroidDevice.PackageHandle(packageId, activityId, launchableActivity);
	}

	@Override
	public App.Handle launchApp(Package.Handle handle) {
		AndroidDevice.PackageHandle androidPackageHandle = (AndroidDevice.PackageHandle) handle;
		launchActivity(androidPackageHandle.launchableActivityName);
		waitForProcessToStart(androidPackageHandle.packageName, 10 * 1000);

		return new AndroidDevice.AppHandle(androidPackageHandle);
	}

	@Override
	public void gotoHome(App.Handle app) {
		AndroidDevice.AppHandle androidAppHandle = (AndroidDevice.AppHandle) app;
		String activity = androidAppHandle.packageHandle.launchableActivityName;
		executeShellCommand("am start -a android.intent.action.MAIN -n " + activity + " --activity-clear-top", new LogShellReceiver(log));
	}

	public static String getStartServiceCommand(int apiLevel) {
		return apiLevel >= 17 ? START_SERVICE_COMMAND_LEVEL_17 : START_SERVICE_COMMAND_LEVEL_16;
	}

	public void pushFile(String local, String remote) {
		try {
			device.pushFile(local, remote);
		} catch (TimeoutException | AdbCommandRejectedException | SyncException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void forwardPort(int localPort, int remotePort) {
		try {
			device.createForward(localPort, remotePort);
		} catch (TimeoutException | AdbCommandRejectedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void installPackage(File file) {
		try {
			boolean reinstall = true;
			String errorMessage = device.installPackage(file.getPath(), reinstall);
			if (errorMessage != null) {
				throw new RuntimeException(errorMessage);
			}
		} catch (InstallException e) {
			throw new RuntimeException(e);
		}
	}

	public void waitForProcessToStart(String packageName, long timeout) {
		long startTime = java.lang.System.currentTimeMillis();

		while ((java.lang.System.currentTimeMillis() - startTime) < timeout) {
			if (getRunningProcesses().values().contains(packageName)) {
				return;
			}

			interruptableSleep(500);
		}

		throw new RuntimeException("Timeout while waiting for app '" + packageName + "' to start on device '" + device.getSerialNumber() + "'");
	}

	public static void interruptableSleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public Map<Integer, String> getRunningProcesses() {
		Map<Integer, String> processes = new HashMap<>();
		String output = executeShellCommand("ps");

		String[] lines = output.split("\n");
		for (int i = 1; i < lines.length; i++) {
			String line = lines[i].trim();
			String[] words = line.split("\\s+");
			if (words.length > 8) {
				processes.put(Integer.parseInt(words[1]), words[8]);
			}
		}
		return processes;
	}

	public void launchActivity(String activity) {
		executeShellCommand("am start -a android.intent.action.MAIN -n " + activity, new LogShellReceiver(log));
	}

	public void launchService(String launchableActivity) {
		executeShellCommand(getStartServiceCommand(19) + " " + launchableActivity, new LogShellReceiver(log));
	}

	public void executeShellCommand(String command, IShellOutputReceiver receiver) {
		log.info("Executing '" + command + "' on '" + device.getSerialNumber() + "'");
		try {
			device.executeShellCommand(formatShellCommand(command), receiver);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void executeShellCommand(String command, IShellOutputReceiver receiver, int timeout) {
		log.info("Executing '" + command + "' on '" + device.getSerialNumber() + "'");
		try {
			device.executeShellCommand(formatShellCommand(command), receiver, timeout);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String executeShellCommand(String command) {
		CollectingOutputReceiver receiver = new CollectingOutputReceiver();
		log.info(String.format("Executing '" + command + "' on '" + device.getSerialNumber() + "'"));
		executeShellCommand(formatShellCommand(command), receiver);
		return receiver.getOutput();
	}

	private static String formatShellCommand(String command) {
		return command.replace("$", "\\$");
	}

	private static long now() {
		return java.lang.System.currentTimeMillis();
	}

	private static JSONObject toJson(String json) {
		try {
			return new JSONObject(json);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

}
