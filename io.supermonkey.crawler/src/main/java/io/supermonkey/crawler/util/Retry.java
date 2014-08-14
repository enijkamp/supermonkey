package io.supermonkey.crawler.util;

import com.google.common.base.Optional;
import io.supermonkey.crawler.exceptions.RetryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Callable;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 02.06.14
 */
public class Retry {

	private static final Log log = LogFactory.getLog(Retry.class);

	public static <T> T retry(Callable<T> callable, int attempts, int sleep) {
		Optional<Throwable> error = Optional.absent();
		for(int attempt = 0; attempt < attempts; attempt++) {
			try {
				return callable.call();
			} catch(Throwable e) {
				error = Optional.of(e);
				log.warn(e);
				sleep(sleep);
			}
		}

		if(error.isPresent()) {
			throw new RetryException(error.get());
		} else {
			throw new RetryException();
		}

	}

	private static void sleep(int sleep) {
		try {
			Thread.sleep(sleep);
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
