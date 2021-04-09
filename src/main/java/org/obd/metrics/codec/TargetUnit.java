package org.obd.metrics.codec;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TargetUnit {

	public static enum UnitName {
		Temperature,
	}

	private UnitName name;
	private String value;
}