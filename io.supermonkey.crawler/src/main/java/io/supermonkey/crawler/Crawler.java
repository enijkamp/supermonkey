package io.supermonkey.crawler;

import io.supermonkey.crawler.device.App;
import io.supermonkey.crawler.device.Device;
import io.supermonkey.crawler.device.View;
import io.supermonkey.crawler.hierarchy.Node;
import io.supermonkey.crawler.hierarchy.Printer;
import io.supermonkey.crawler.strategy.Command;
import io.supermonkey.crawler.strategy.Executor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.supermonkey.crawler.strategy.Strategy;
import io.supermonkey.crawler.util.Logs;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 27.05.14
 */
public class Crawler {

	public static class Result {

		public final Command command;
		public final Executor.Result result;
		public final View view;

		public Result(Command command, Executor.Result result, View view) {
			this.command = command;
			this.result = result;
			this.view = view;
		}

	}

	private static final Log log = LogFactory.getLog(Crawler.class);

	private final Device device;
	private final Strategy strategy;
	private final Executor executor;

	public Crawler(Device device, App.Handle appHandle, Strategy strategy) {
		this.device = device;
		this.strategy = strategy;
		this.executor = new Executor(device.getApps(), device.getInput(), appHandle);
	}

	public Result crawl() throws InterruptedException {

		View view = inspectView(device.getInspector());
		Command command = strategy.walk(view);
		Executor.Result result = executor.execute(command);

		return new Result(command, result, view);
	}

	private View inspectView(Device.Inspector inspector) {
		View.Id viewId = inspector.getCurrentViewId();
		Node hierarchy = inspector.dumpHierarchy();

		log.info("current view: " + viewId.getQualifiedName());
		new Printer(Logs.getDebugStream(log)).print(hierarchy);

		return new View(viewId, hierarchy);
	}

}