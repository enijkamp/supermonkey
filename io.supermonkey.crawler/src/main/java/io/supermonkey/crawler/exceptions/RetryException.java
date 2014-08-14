package io.supermonkey.crawler.exceptions;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 02.06.14
 */
public class RetryException extends RuntimeException {

	public RetryException() {
		super();
	}

	public RetryException(Throwable throwable) {
		super(throwable);
	}
}
