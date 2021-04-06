package org.obd.metrics.units;

import java.io.IOException;

public interface UnitsRegistry {

	Unit findByName(String name);

	static UnitsRegistry instance() throws IOException {
		var registry = new DefaultUnitRegistry();
		try (var openStream = Thread.currentThread().getContextClassLoader().getResource("units.json").openStream()) {
			registry.load(openStream);
		}
		return registry;
	}
}
