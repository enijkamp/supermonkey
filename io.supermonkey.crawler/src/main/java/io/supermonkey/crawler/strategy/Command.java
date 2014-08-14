package io.supermonkey.crawler.strategy;

import io.supermonkey.crawler.hierarchy.Selector;
import io.supermonkey.crawler.device.View;
import io.supermonkey.crawler.util.Identity;

/**
* @author Erik Nijkamp
* @since 31.05.14
*/
public interface Command {

	Id getId();

	class Id extends Identity.With1<String> {
		public Id(String id) {
			super(id);
		}

		public static Id from(String id) {
			return new Id(id);
		}
	}

	interface HasSelector {
		Selector getSelector();
	}

	class ClickOnElement implements Command, HasSelector {

		public final Selector selector;

		public ClickOnElement(Selector selector) {
			this.selector = selector;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "(" + selector.getId() + ")";
		}

		@Override
		public Selector getSelector() {
			return selector;
		}

		@Override
		public Id getId() {
			return Id.from(getClass().getSimpleName() + "(" + selector.getId() + ")");
		}
	}

	class GoToView implements Command {

		public final View.Id view;

		public GoToView(View.Id view) {
			this.view = view;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName();
		}

		@Override
		public Id getId() {
			return Id.from(getClass().getSimpleName() + "()");
		}
	}

	class GoToHome implements Command {

		@Override
		public String toString() {
			return getClass().getSimpleName();
		}

		@Override
		public Id getId() {
			return Id.from(getClass().getSimpleName() + "()");
		}

	}

	class Exit implements Command {

		@Override
		public String toString() {
			return getClass().getSimpleName();
		}

		@Override
		public Id getId() {
			return Id.from(getClass().getSimpleName() + "()");
		}

	}

}
