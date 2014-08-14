package io.supermonkey.crawler.device.android;

import com.android.ddmlib.MultiLineReceiver;
import org.apache.commons.logging.Log;

/**
* @author Erik Nijkamp
* @since 30.05.14
*/
public class LogShellReceiver extends MultiLineReceiver {

	private final Log log;

	public LogShellReceiver(Log log) {
		this.log = log;
	}

	@Override
	public void processNewLines(String[] lines) {
		for (String line : lines) {
			log.trace("<logcat> " + line);
		}
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

}
