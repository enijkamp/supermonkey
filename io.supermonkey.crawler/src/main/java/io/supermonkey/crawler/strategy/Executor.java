package io.supermonkey.crawler.strategy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.supermonkey.crawler.device.App;
import io.supermonkey.crawler.device.Device;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 31.05.14
 */
public class Executor {

	private static final Log log = LogFactory.getLog(Executor.class);

	public enum Result { CONTINUE, EXIT }

	private final Device.Apps apps;
	private final Device.Input input;
	private final App.Handle appHandle;

	public Executor(Device.Apps apps, Device.Input input, App.Handle appHandle) {
		this.apps = apps;
		this.input = input;
		this.appHandle = appHandle;
	}

	public Result execute(Command command) {
		log.info("Executing command '" + command.toString() + "'");

		if(command instanceof Command.Exit) {
			return Result.EXIT;
		}

		if(command instanceof Command.ClickOnElement) {
			// FIXME handle not found element exception (en)
			Command.ClickOnElement click = (Command.ClickOnElement) command;
			input.click(click.selector);

			return Result.CONTINUE;
		}

		if(command instanceof Command.GoToHome) {

			apps.gotoHome(appHandle);

			return Result.CONTINUE;
		}

		throw new IllegalArgumentException("Unknown command '" + command.getClass().getSimpleName() + "'");
	}

}
