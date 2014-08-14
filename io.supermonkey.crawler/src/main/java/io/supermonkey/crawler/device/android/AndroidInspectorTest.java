package io.supermonkey.crawler.device.android;

import org.junit.Test;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 31.05.14
 */
public class AndroidInspectorTest {

	@Test
	public void compareWithUiDump() {

	}

	public void dump(AndroidDevice device) {
		device.getApps().executeShellCommand("rm /sdcard/uidump.xml");
		device.getApps().executeShellCommand("/system/bin/uiautomator dump /sdcard/uidump.xml");
		String uidump = cat(device, "/sdcard/uidump.xml");
		System.out.println(uidump);
	}

	public static String cat(AndroidDevice device, String path) {
		String response = device.getApps().executeShellCommand("cat " + path);
		return response;
	}
}
