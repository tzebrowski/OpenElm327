package org.obd.metrics.api;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter class ConversionUnits {
	String name;
	String value;
}