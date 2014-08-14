package io.supermonkey.crawler.hierarchy;

import org.testobject.commons.util.collections.Lists;
import io.supermonkey.crawler.util.Identity;

import java.util.LinkedList;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 29.05.14
 */
public class Selector {

	public static class Id extends Identity.With1<String> {
		public Id(String id) {
			super(id);
		}

		public static Id from(LinkedList<Element> path) {
			return new Id(path.getLast().getId().toString());
		}
	}

	private final Id id;
	private final LinkedList<Element> path;

	public Selector(LinkedList<Element> path) {
		this.id = Id.from(path);
		this.path = path;
	}

	public Selector(Element ... path) {
		this(Lists.newLinkedList(path));
	}

	public Id getId() {
		return id;
	}

	public LinkedList<Element> getPath() {
		return path;
	}

	@Override
	public String toString() {
		return path.getLast().getId().toString();
	}
}
