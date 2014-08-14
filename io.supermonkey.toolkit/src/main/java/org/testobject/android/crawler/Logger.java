package org.testobject.android.crawler;

/**
 * @author Erik Nijkamp
 * @since 28.05.14
 */
public class Logger {

	private static String prefix = "[CRAWLER-TOOLKIT]";
	private static String suffix = "[/CRAWLER-TOOLKIT]";

	public static void debug(final String msg) {
		System.out.println(Logger.prefix + " [debug] " + msg + Logger.suffix);
	}

	public static void error(final String msg) {
		System.out.println(Logger.prefix + " [error] " + msg + Logger.suffix);
	}

	public static void info(final String msg) {
		System.out.println(Logger.prefix + " [info] " + msg + Logger.suffix);
	}
}
