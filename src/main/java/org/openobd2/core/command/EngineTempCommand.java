package org.openobd2.core.command;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EngineTempCommand extends Command implements Transformation<Integer> {

	public EngineTempCommand() {
		super("01 05", "Get engine temperature");
	}

	@Override
	public Integer transform(@NonNull String data) {
		if (data.length() > 6) {
			final String noWhiteSpaces = data.substring(6).replaceAll("\\s", "");
			final int decimal = Integer.parseInt(noWhiteSpaces, 16);
			int value = decimal - 40;
			log.debug("Engine temp is: {}", value);
			return value;
		}
		return null;
	}
}