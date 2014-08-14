package org.testobject.android.crawler.exceptions;

/**
 * @author Erik Nijkamp
 * @since 28.05.14
 */
@SuppressWarnings("serial")
public class SocketServerException extends Exception {

	String reason;

	public SocketServerException(final String msg) {
		super(msg);
		reason = msg;
	}

	public String getError() {
		return reason;
	}
}
