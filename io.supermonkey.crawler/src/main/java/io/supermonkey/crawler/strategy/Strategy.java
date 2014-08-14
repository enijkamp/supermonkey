package io.supermonkey.crawler.strategy;

import io.supermonkey.crawler.device.View;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 30.05.14
 */
public interface Strategy {

	Command walk(View view);

}
