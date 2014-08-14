package io.supermonkey.crawler.device;

import io.supermonkey.crawler.hierarchy.Node;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 05.06.14
 */
public class View {

	public interface Id {

		String getQualifiedName();

		String getShortName();
	}

	private final View.Id viewId;
	private final Node elements;

	public View(View.Id viewId, Node elements) {
		this.viewId = viewId;
		this.elements = elements;
	}

	public View.Id getId() {
		return viewId;
	}

	public Node getElements() {
		return elements;
	}
}
