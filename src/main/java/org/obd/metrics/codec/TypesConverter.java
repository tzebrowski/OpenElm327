package org.obd.metrics.codec;

import org.obd.metrics.pid.PidDefinition;

public interface TypesConverter {
	
	static Number convert(PidDefinition pid, Object eval) {

		var value = Number.class.cast(eval);

		if (pid.getType() == null) {
			return value.doubleValue();
		} else {
			switch (pid.getType()) {
			case INT:
				return value.intValue();
			case DOUBLE:
				return value.doubleValue();
			case SHORT:
				return value.shortValue();
			default:
				return value;
			}
		}
	}
}
