package io.supermonkey.crawler.strategy;

import io.supermonkey.crawler.device.App;
import io.supermonkey.crawler.device.View;
import io.supermonkey.crawler.hierarchy.Element;
import io.supermonkey.crawler.hierarchy.Node;
import io.supermonkey.crawler.hierarchy.Selector;
import io.supermonkey.crawler.hierarchy.Validators;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 31.05.14
 */
public class RandomWalk implements Strategy {

	private static final Log log = LogFactory.getLog(RandomWalk.class);

	private final App.Handle appHandle;
	private final Validators.Validator2<View.Id, App.Handle> viewIdValidator;

	private final Random random = new Random();

	public RandomWalk(App.Handle appHandle, Validators.Validator2<View.Id, App.Handle> viewIdValidator) {
		this.appHandle = appHandle;
		this.viewIdValidator = viewIdValidator;
	}

	@Override
	public Command walk(View view) {

		if(!viewIdValidator.isValid(view.getId(), appHandle)) {
			return new Command.GoToHome();
		}

		List<Selector> selectors = filterClickableElements(view.getElements());

		if(selectors.isEmpty()) {
			return new Command.GoToHome();
		}

		log.info("Found " + selectors.size() + " clickable elements");
		Collections.shuffle(selectors, random);
		for(Selector selector : selectors) {
			System.err.println(selector.getPath().getLast().getAttributes().get("resource-id"));
		}
		Selector selector = selectors.get(0);

		return new Command.ClickOnElement(selector);
	}

	private List<Selector> filterClickableElements(Node node) {
		List<Selector> selectors = new ArrayList<>();
		filterClickableElements(node, selectors);

		return selectors;
	}

	private void filterClickableElements(Node node, List<Selector> selectors) {
		Element element = node.getElement();
		if(element.isClickable()) {
			selectors.add(new Selector(element));
		}

		for(Node child : node.getChilds()) {
			filterClickableElements(child, selectors);
		}
	}
}
