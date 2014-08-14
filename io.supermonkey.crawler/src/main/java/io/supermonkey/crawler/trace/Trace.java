package io.supermonkey.crawler.trace;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.supermonkey.crawler.device.View;
import io.supermonkey.crawler.strategy.Command;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 05.06.14
 */
public class Trace {

	public static class Builder {

		public Optional<View> oldView = Optional.absent();
		public Optional<View> newView = Optional.absent();
		public Optional<Command> command = Optional.absent();

		public Builder applyOldView(View view) {
			this.oldView = Optional.of(view);
			return this;
		}

		public Builder applyNewView(View view) {
			this.newView = Optional.of(view);
			return this;
		}

		public Builder applyCommand(Command command) {
			this.command = Optional.of(command);
			return this;
		}

		public Trace build() {
			Preconditions.checkState(isPresent());
			return new Trace(command.get(), oldView.get(), newView.get());
		}

		public boolean isPresent() {
			return oldView.isPresent() && newView.isPresent() && command.isPresent();
		}
	}

	public final Command command;
	public final View oldView;
	public final View newView;

	public Trace(Command command, View oldView, View newView) {
		this.command = command;
		this.oldView = oldView;
		this.newView = newView;
	}

}
