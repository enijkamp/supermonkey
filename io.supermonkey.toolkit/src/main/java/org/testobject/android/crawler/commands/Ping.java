package org.testobject.android.crawler.commands;

import org.testobject.android.crawler.Command;

/**
 * @author Erik Nijkamp
 * @since 28.05.14
 */
public class Ping implements Command {

	@Override
	public Command.Result call() throws Exception {
		return Command.Result.success("pong");
	}

}
