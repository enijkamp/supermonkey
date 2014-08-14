package io.supermonkey.crawler.trace;

import io.supermonkey.crawler.strategy.Command;
import io.supermonkey.crawler.util.Identity;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 30.05.14
 */
public class Edge {

	public static class Id extends Identity.With3<Node.Id, Node.Id, Command.Id> {
		public Id(Node.Id source, Node.Id target, Command.Id command) {
			super(source, target, command);
		}

		public static Id from(Node.Id source, Node.Id target, Command.Id command) {
			return new Id(source, target, command);
		}
	}

	private final Id id;
	private final Node source;
	private final Node target;
	private final Command command;

	public Edge(Node source, Node target, Command command) {
		this.source = source;
		this.target = target;
		this.command = command;
		this.id = Id.from(source.getId(), target.getId(), command.getId());
	}

	public Id getId() {
		return id;
	}

	public Node getSource() {
		return source;
	}

	public Node getTarget() {
		return target;
	}

	public Command getCommand() {
		return command;
	}

	public boolean isLoop() {
		return source.getId().equals(target.getId());
	}
}
