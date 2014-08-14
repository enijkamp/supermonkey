package io.supermonkey.crawler.trace;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

/**
 * Directed multi-graph.
 *
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 30.05.14
 */
public class Graph {

	private final Map<Node.Id, Node> nodes = new HashMap<>();
	private final Map<Edge.Id, Edge> edges = new HashMap<>();

	public Edge addEdge(Edge edge) {
		getOrAddNode(edge.getSource()).addEdge(edge);
		return edge;
	}

	public Node getOrAddNode(Node node) {
		Optional<Node> sourceNode = getNodeById(node.getId());
		if(sourceNode.isPresent()) {
			return sourceNode.get();
		} else {
			return addNode(node);
		}
	}

	public Node addNode(Node node) {
		Preconditions.checkState(!getNodeById(node.getId()).isPresent());
		nodes.put(node.getId(), node);

		return node;
	}

	public Optional<Node> getNodeById(Node.Id nodeId) {
		return Optional.fromNullable(nodes.get(nodeId));
	}

	public Optional<Edge> getEdgeById(Edge.Id edgeId) {
		return Optional.fromNullable(edges.get(edgeId));
	}
}
