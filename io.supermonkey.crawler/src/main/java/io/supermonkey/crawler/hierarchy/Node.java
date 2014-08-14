package io.supermonkey.crawler.hierarchy;

import java.util.List;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 28.05.14
 */
public class Node {

	private final Element element;
	private final List<Node> childs;

	public Node(Element element, List<Node> childs) {
		this.element = element;
		this.childs = childs;
	}

	public Element getElement() {
		return element;
	}

	public List<Node> getChilds() {
		return childs;
	}

}
