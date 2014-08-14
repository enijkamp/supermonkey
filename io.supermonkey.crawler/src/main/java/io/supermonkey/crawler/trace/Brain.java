package io.supermonkey.crawler.trace;

import com.google.common.base.Optional;
import io.supermonkey.crawler.device.View;
import io.supermonkey.crawler.hierarchy.Selector;
import io.supermonkey.crawler.strategy.Command;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.supermonkey.crawler.hierarchy.Element;

import java.util.*;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 05.06.14
 */
public class Brain {

	private static final Log log = LogFactory.getLog(Brain.class);

	private final Graph graph = new Graph();

	public Edge addTrace(Trace trace) {
		Node sourceNode = getOrAddNode(graph, trace.oldView);
		Node targetNode = getOrAddNode(graph, trace.newView);

		Edge edge = graph.addEdge(new Edge(sourceNode, targetNode, trace.command));
		return edge;
	}

	public boolean isTraceable(View view, Selector selector) {
		log.info("tracing selector -> view : " + view.getId().getShortName() + " -> selector: " + selector);

		Optional<Node> source = graph.getNodeById(Node.Id.from(view));

		// unknown view
		if (!source.isPresent()) {
			log.info(indent(1) + "(1) traceable (unknown view) -> view: " + view.getId().getShortName());
			return true;
		}

		// edge to home
		if (hasGoToHomeEdge(source.get())) {
			log.info(indent(1) + "(1) not traceable (goto-home edge)");
			return false;
		}

		// traceable paths
		Optional<Edge> edge = getEdgeBySelector(source.get().getEdges(), selector);
		if (!edge.isPresent()) {
			log.info(indent(1) + "(1) traceable (unknown selector) -> view: " + view.getId().getShortName() + " -> selector: " + selector);
			return true;
		} else {
			Set<Node.Id> visitedNodes = new HashSet<Node.Id>();
			visitedNodes.add(source.get().getId());
			boolean traceable = isTransitiveTraceable(edge.get(), visitedNodes, 2);
			if(!traceable) {
				log.info(indent(1) + "(1) not traceable (transitive)");
			}

			return traceable;
		}
	}

	private boolean isTransitiveTraceable(Edge edgeIn, Set<Node.Id> visitedNodes, int depth) {
		// loops cannot point to unseen views
		if(edgeIn.isLoop()) {
			return false;
		}

		Node target = edgeIn.getTarget();

		// edge to home
		if (hasGoToHomeEdge(target)) {
			log.info(indent(depth) + "(" + depth + ") not traceable (goto-home edge)");
			return false;
		}

		// don't visit nodes twice to avoid loops
		if(visitedNodes.contains(target.getId())) {
			return false;
		} else {
			visitedNodes.add(target.getId());
		}

		// compare clickable elements of node with outgoing edges
		List<Edge> edgesOut = filterBySelector(target.getEdges());
		List<Selector> clickables = getClickables(target.getView().getElements());

		// recursively check edges of node
		for(Selector clickable : clickables) {

			Optional<Edge> edgeForClickable = getEdgeBySelector(edgesOut, clickable);
			if(!edgeForClickable.isPresent()) {
				// if we don't have an edge, then we can still trace this path
				log.info(indent(depth) + "(" + depth + ") traceable (unknown selector) -> view: " + target.getView().getId().getShortName() + " -> selector: " + clickable);
				return true;
			} else {
				// otherwise check recursively
				for(Edge edgeOut : edgesOut) {
					if(isTransitiveTraceable(edgeOut, visitedNodes, depth + 1)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean hasGoToHomeEdge(Node node) {
		Optional<Edge> edge = getEdgeByCommandType(node.getEdges(), Command.GoToHome.class);
		return edge.isPresent();
	}

	private Optional<Edge> getEdgeByCommandType(List<Edge> edges, Class<? extends Command> cls) {
		for(Edge edge : edges) {
			if(cls.isInstance(edge.getCommand())) {
				return Optional.of(edge);
			}
		}

		return Optional.absent();
	}

	private List<Edge> filterBySelector(List<Edge> edges) {
		List<Edge> result = new ArrayList<>();
		for(Edge edge : edges) {
			if (edge.getCommand() instanceof Command.HasSelector) {
				result.add(edge);
			}
		}

		return result;
	}

	private List<Selector> getClickables(io.supermonkey.crawler.hierarchy.Node node) {
		List<Selector> clickables = new ArrayList<>();
		LinkedList<Element> path = new LinkedList<>();
		path.add(node.getElement());
		getClickables(node, path, clickables);

		return clickables;
	}

	private void getClickables(io.supermonkey.crawler.hierarchy.Node node, LinkedList<Element> path, List<Selector> clickables) {

		if(node.getElement().isClickable()) {
			LinkedList<Element> pathToElement = new LinkedList<>(path);
			pathToElement.add(node.getElement());
			clickables.add(new Selector(pathToElement));
		}

		for(io.supermonkey.crawler.hierarchy.Node child : node.getChilds()) {
			getClickables(child, path, clickables);
		}
	}

	private Optional<Edge> getEdgeBySelector(List<Edge> edges, Selector selector) {
		for(Edge edge : edges) {
			if (edge.getCommand() instanceof Command.HasSelector) {
				Command.HasSelector hasSelector = (Command.HasSelector) edge.getCommand();
				Selector.Id id1 = selector.getId();
				Selector.Id id2 = hasSelector.getSelector().getId();
				if(id1.equals(id2)) {
					return Optional.of(edge);
				}
			}
		}

		return Optional.absent();
	}

	private Node getOrAddNode(Graph graph, View view) {
		Optional<Node> node = graph.getNodeById(Node.Id.from(view));
		if(node.isPresent()) {
			return node.get();
		} else {
			return graph.addNode(new Node(view));
		}
	}

	private String indent(int level) {
		return StringUtils.repeat(" ", level * 2);
	}

}
