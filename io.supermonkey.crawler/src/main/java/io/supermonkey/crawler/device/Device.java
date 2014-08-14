package io.supermonkey.crawler.device;

import io.supermonkey.crawler.exceptions.SelectorTranslateException;
import io.supermonkey.crawler.hierarchy.Node;
import io.supermonkey.crawler.hierarchy.Selector;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 28.05.14
 */
public interface Device extends Closeable {

	class Query {

	}

	interface Factory {

		Device create(Query query);

	}

	interface Apps {

		Package.Handle installApp(File file);

		App.Handle launchApp(Package.Handle handle);

		void gotoHome(App.Handle app);
	}

	interface Inspector {

		void waitForIdle();

		Node dumpHierarchy();

		View.Id getCurrentViewId();

		View.Id getHomeViewId(App.Handle app);

		BufferedImage takeScreenshot();
	}

	interface Input {

		void click(Selector selector) throws SelectorTranslateException;

	}

	Apps getApps();

	Input getInput();

	Inspector getInspector();

	void close();
}
