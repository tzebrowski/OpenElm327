package org.obd.metrics.units;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = { "id" })
public final class Unit {

	private long id;
	private String name;
	private List<UnitConverter> converters;
}
