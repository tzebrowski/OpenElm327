package org.obd.metrics.command.obd;

import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;

import lombok.Getter;

public class ObdCommand extends Command {

	@Getter
	protected PidDefinition pid;

	public ObdCommand(String query) {
		super(query, "Custom: " + query);
	}

	public ObdCommand(PidDefinition pid) {
		super(pid.getMode() + pid.getPid(), pid.getDescription());
		this.pid = pid;
	}

	@Override
	public String toString() {

		var builder = new StringBuilder();
		builder.append("[pid=");
		if (pid != null) {
			builder.append(pid.getDescription());
		}

		builder.append(", query=");
		builder.append(query);
		builder.append("]");
		return builder.toString();
	}
}
