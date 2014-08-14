package io.supermonkey.crawler.trace;

import io.supermonkey.crawler.device.View;
import io.supermonkey.crawler.util.Identity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 11.06.14
 */
public class Node {
	public static class Id extends Identity.With1<String> {
		public Id(String id) {
			super(id);
		}

		public static Id from(View view) {
			return new Id(view.getId().getQualifiedName());
		}
	}

	private final View view;
	private final Id id;
	private final List<Edge> edges = new ArrayList<>();

	public Node(View view) {
		this.view = view;
		this.id = Id.from(view);
	}

	public Id getId() {
		return id;
	}

	public void addEdge(Edge edge) {
		edges.add(edge);
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public View getView() {
		return view;
	}


}
