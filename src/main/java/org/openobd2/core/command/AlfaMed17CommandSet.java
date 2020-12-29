package org.openobd2.core.command;

import org.openobd2.core.command.at.CustomATCommand;
import org.openobd2.core.command.at.EchoCommand;
import org.openobd2.core.command.at.HeadersCommand;
import org.openobd2.core.command.at.LineFeedCommand;
import org.openobd2.core.command.at.ResetCommand;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.pid.PidDefinition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public  class AlfaMed17CommandSet <T extends Command> extends CommandSet<T> {

	// https://www.scantool.net/scantool/downloads/234/stn1100-frpm-preliminary.pdf

	public static final CommandSet<Command> CAN_INIT = CommandSet.of(
			new ResetCommand(),
			new LineFeedCommand(0), 
			new HeadersCommand(0), 
			new EchoCommand(0), 
			new CustomATCommand("PP 2CSV 01"),
			new CustomATCommand("PP 2C ON"), // activate baud rate PP.
			new CustomATCommand("PP 2DSV 01"),// activate addressing pp.
			new CustomATCommand("PP 2D ON"),
			new CustomATCommand("S0"),// Print spaces on*/off
			new CustomATCommand("SPB"),// set protocol to B
			new CustomATCommand("CP18"),// Set CAN priority to 18 (29 bit only)
			new CustomATCommand("CRA 18DAF110"),// Set CAN hardware filter,18DAF110
			new CustomATCommand("SH DA10F1"),// Set CAN request message header: DA10F1
			new CustomATCommand("AT0"),// Adaptive timing off, auto1*, auto2
			new CustomATCommand("ST19"),// Set OBD response timeout.
			new ObdCommand(new PidDefinition(0, "", "10", "03", "", "", "", ""))); // 50 03 003201F4
	
	// 3E00. keep the session open
}
