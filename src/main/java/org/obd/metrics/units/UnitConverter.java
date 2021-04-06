package org.obd.metrics.units;

import lombok.Getter;

@Getter
public final class UnitConverter {

	private String from;
	private String to;
	private String label;
	private String conversionRule;
}
