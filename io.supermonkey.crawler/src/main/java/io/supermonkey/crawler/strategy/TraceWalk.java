package io.supermonkey.crawler.strategy;

import com.google.common.base.Optional;
import io.supermonkey.crawler.device.App;
import io.supermonkey.crawler.device.View;
import io.supermonkey.crawler.hierarchy.Element;
import io.supermonkey.crawler.hierarchy.Node;
import io.supermonkey.crawler.hierarchy.Selector;
import io.supermonkey.crawler.hierarchy.Validators;
import io.supermonkey.crawler.trace.Brain;
import io.supermonkey.crawler.trace.Trace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *  The trace walk recursively explores and memorizes click paths, so that we can early exit
 *  click paths with low probability to reveal previously unseen views. In principle, this is
 *  similar to depth-first search.
 *
 *  1. start at origin view 'A'
 *  2. click element 'a' of 'A' if 'a' is traceable,
 *    i. 'a' was not clicked yet, or
 *    ii. 'a' leads a element which was not clicked yet
 *  3. if 'a' leads to view 'B', goto 2. for next views {'B', 'C', ...}
 *  4. if current view is recursively fully traced, goto origin view 'A'
 *  5. if origin view 'A' is resursively fully traced, then exit
 *
 *
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 31.05.14
 */
public class TraceWalk implements Strategy {

	private static final Log log = LogFactory.getLog(TraceWalk.class);

	private final App.Handle appHandle;
	private final View.Id homeView;
	private final Validators.Validator2<View.Id, App.Handle> viewIdValidator;

	private final Brain brain = new Brain();

	private Optional<Trace.Builder> trace = Optional.absent();

	public TraceWalk(App.Handle appHandle, View.Id homeView, Validators.Validator2<View.Id, App.Handle> viewIdValidator) {
		this.appHandle = appHandle;
		this.homeView = homeView;
		this.viewIdValidator = viewIdValidator;
	}

	@Override
	public Command walk(View view) {

		// keep trace
		if(trace.isPresent()) {
			trace.get().applyNewView(view);
			brain.addTrace(trace.get().build());
		}

		// next trace
		{
			trace = Optional.of(new Trace.Builder());
			Command command = nextCommand(view);
			trace.get().applyCommand(command);
			trace.get().applyOldView(view);

			return command;
		}
	}

	private Command nextCommand(View view) {

		if(!viewIdValidator.isValid(view.getId(), appHandle)) {
			log.info("Found invalid view '" + view.getId().getQualifiedName() + "'");
			return goHomeOrExit(view);
		}

		List<Selector> clickables = filterClickableElements(view.getElements());
		log.info("Found " + clickables.size() + " valid elements");
		if(clickables.isEmpty()) {
			return goHomeOrExit(view);
		}

		List<Selector> traceables = getTraceables(view, clickables);
		log.info("Found " + traceables.size() + " traceable elements");
		if(traceables.isEmpty()) {
			return goHomeOrExit(view);
		}

		Selector selector = traceables.get(0);

		return new Command.ClickOnElement(selector);
	}

	private Command goHomeOrExit(View view) {
		if(isHomeView(view)) {
			return new Command.Exit();
		} else {
			return new Command.GoToHome();
		}
	}

	private boolean isHomeView(View view) {
		return homeView.equals(view.getId());
	}

	private List<Selector> getTraceables(View view, List<Selector> clickables) {
		List<Selector> traceables = new ArrayList<>();
		for(Selector clickable : clickables) {
			if(brain.isTraceable(view, clickable)) {
				traceables.add(clickable);
			}
		}

		return traceables;
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
