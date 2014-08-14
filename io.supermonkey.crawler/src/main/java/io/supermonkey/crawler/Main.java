package io.supermonkey.crawler;

import com.android.ddmlib.IDevice;
import com.google.common.base.Optional;
import io.supermonkey.crawler.device.*;
import io.supermonkey.crawler.ui.TraversalViewer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.util.config.Configuration;
import org.testobject.commons.util.config.Constants;
import io.supermonkey.crawler.device.android.AndroidDevice;
import io.supermonkey.crawler.device.genymotion.Genymotion;
import io.supermonkey.crawler.device.View;
import io.supermonkey.crawler.device.android.JsonSelectorTranslator;
import io.supermonkey.crawler.device.android.ViewIdValidator;
import io.supermonkey.crawler.strategy.Command;
import io.supermonkey.crawler.strategy.Strategy;
import io.supermonkey.crawler.strategy.TraceWalk;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 01.06.14
 */
public class Main {

	interface Callback {

		void changeToView(Command command, View oldView, View newView);

		void foundNewView(View view, BufferedImage screenshot);

		void executedCommand(Command command);
	}

	static class TraversalViewerCallback implements Callback {

		private final TraversalViewer traversalViewer;

		public TraversalViewerCallback(TraversalViewer traversalViewer) {
			this.traversalViewer = traversalViewer;
		}

		@Override
		public void changeToView(Command command, View oldView, View newView) {
			traversalViewer.addViewTransition(command, oldView, newView);
		}

		@Override
		public void foundNewView(View view, BufferedImage screenshot) {
			traversalViewer.addViewScreenshot(view, screenshot);
		}

		@Override
		public void executedCommand(Command command) {
			if (command instanceof Command.GoToHome) {
				traversalViewer.fadeToast("Going home");
			}

			if (command instanceof Command.Exit) {
				traversalViewer.fadeToast("Done :)");
			}
		}
	}

	private static final Log log = LogFactory.getLog(Main.class);

	public static void main(String ... args) throws Exception {
		TraversalViewer viewer = TraversalViewer.create();
		TraversalViewerCallback callback = new TraversalViewerCallback(viewer);

		viewer.showToast("Booting Genymotion ...");
		Device device = createGenymotionDevice();

		viewer.showToast("Launching App ...");
		App.Handle appHandle = launchApp(device, "/home/enijkamp/komoot.apk");

		viewer.fadeToast("Crawling App ...");
		crawl(device, appHandle, callback);
	}

	private static void crawl(Device device, App.Handle appHandle, Callback callback) throws InterruptedException {

		// Strategy strategy = new RandomWalk(appHandle, new ViewIdValidator());
		Strategy strategy = new TraceWalk(appHandle, device.getInspector().getHomeViewId(appHandle), new ViewIdValidator());

		Crawler crawler = new Crawler(device, appHandle, strategy);
		Set<View.Id> foundViews = new HashSet<>();
		Optional<View> previousView = Optional.absent();

		int step = 1;

		while(true) {

			log.info(StringUtils.repeat('-', 20) + (" step " + (step++) + " ") + StringUtils.repeat('-', 20));

			log.info("Waiting for idle");
			waitForDraw(device.getInspector());

			log.info("Taking screenshot");
			BufferedImage screen = device.getInspector().takeScreenshot();

			log.info("Searching for next click path");
			Crawler.Result result = crawler.crawl();

			// command result
			callback.executedCommand(result.command);

			// view transition
			if(previousView.isPresent()) {
				log.info("Transition from view '" + previousView.get().getId().getShortName() + "' to view '" + result.view.getId().getShortName() + "'");
				callback.changeToView(result.command, previousView.get(), result.view);
			}
			previousView = Optional.of(result.view);

			// view found
			if(!foundViews.contains(result.view.getId())) {
				log.info("Found new view '" + result.view.getId().getQualifiedName() + "'");
				foundViews.add(result.view.getId());
				callback.foundNewView(result.view, screen);
			}

			// done
			if (result.command instanceof Command.Exit) {
				log.info("Exit");
				return;
			}
		}
	}

	private static void waitForDraw(Device.Inspector inspector) throws InterruptedException {
		inspector.waitForIdle();
		Thread.sleep(1000);
	}

	private static App.Handle launchApp(Device device, String appFile) throws InterruptedException {
		io.supermonkey.crawler.device.Package.Handle packageHandle = device.getApps().installApp(new File(appFile));
		App.Handle appHandle = device.getApps().launchApp(packageHandle);
		Thread.sleep(5 * 1000);

		return appHandle;
	}

	private static Device createGenymotionDevice() throws IOException {
		String sdkPath = Configuration.getProperty(Constants.android_sdk_location);
		IDevice adbDevice = Genymotion.createDevice();
		return new AndroidDevice(sdkPath, adbDevice);
	}

}
