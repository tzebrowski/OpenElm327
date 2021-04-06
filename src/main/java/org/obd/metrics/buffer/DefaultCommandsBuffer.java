package org.obd.metrics.buffer;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingDeque;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.group.CommandGroup;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultCommandsBuffer implements CommandsBuffer {

	// no synchronization need, already synchronized
	private volatile LinkedBlockingDeque<Command> deque = new LinkedBlockingDeque<Command>();

	@Override
	public CommandsBuffer clear() {
		deque.clear();
		return this;
	}

	@Override
	public DefaultCommandsBuffer add(CommandGroup<?> group) {
		addAll(group.getCommands());
		return this;
	}

	@Override
	public CommandsBuffer addAll(Collection<? extends Command> commands) {
		commands.forEach(this::addLast);
		return this;
	}

	@Override
	public <T extends Command> CommandsBuffer addFirst(T command) {
		try {
			deque.putFirst(command);
		} catch (InterruptedException e) {
			log.warn("Failed to add command to the queue", e);
		}
		return this;
	}

	@Override
	public <T extends Command> CommandsBuffer addLast(T command) {
		try {
			deque.putLast(command);
		} catch (InterruptedException e) {
			log.warn("Failed to add command to the queue", e);
		}
		return this;
	}

	@Override
	public Command get() throws InterruptedException {
		return deque.takeFirst();
	}
}
