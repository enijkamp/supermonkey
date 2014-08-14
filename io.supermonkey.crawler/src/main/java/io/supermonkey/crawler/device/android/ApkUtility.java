package io.supermonkey.crawler.device.android;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 30.05.14
 */
public class ApkUtility {

	private static final Pattern PACKAGE_ID_PATTERN = Pattern.compile("package: +name='([^']+)'");

	private static final Pattern LAUNCHABLE_ACTIVITY_PATTERN = Pattern.compile("launchable-activity: +name='([^']+)'");

	public static String getPackageId(String sdkPath, String apkPath) {
		return getAaptResult(sdkPath, apkPath, PACKAGE_ID_PATTERN);
	}

	public static String getLaunchableActivity(String sdkPath, String apkPath) {
		return getAaptResult(sdkPath, apkPath, LAUNCHABLE_ACTIVITY_PATTERN);
	}

	public static String getQualifiedLaunchableActivity(String packageId, String activity) {
		return packageId + '/' + activity;
	}

	private static String getAaptResult(String sdkPath, String apkPath, final Pattern pattern) {
		try {
			final File apkFile = new File(apkPath);
			final ByteArrayOutputStream aaptOutput = new ByteArrayOutputStream();
			final String command = getAaptDumpBadgingCommand(sdkPath, apkFile.getName());

			Process process = Runtime.getRuntime().exec(command, null, apkFile.getParentFile());

			InputStream inputStream = process.getInputStream();
			for (int last = inputStream.read(); last != -1; last = inputStream.read()) {
				aaptOutput.write(last);
			}

			String packageId = "";
			final String aaptResult = aaptOutput.toString();
			if (aaptResult.length() > 0) {
				final Matcher matcher = pattern.matcher(aaptResult);
				if (matcher.find()) {
					packageId = matcher.group(1);
				}
			}
			return packageId;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getAaptDumpBadgingCommand(String sdkPath, String apkName) {
		return sdkPath + "/build-tools/17.0.0/aapt dump badging " + apkName;
	}

}
