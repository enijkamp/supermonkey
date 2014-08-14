package io.supermonkey.crawler.device.android;

import org.apache.commons.logging.Log;

/**
* @author Erik Nijkamp
* @since 30.05.14
*/
public class LogExceptionHandler implements Thread.UncaughtExceptionHandler {

	private final Log log;

	public LogExceptionHandler(Log log) {
		this.log = log;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable e) {
		log.error(e.getMessage(), e);
	}
}
