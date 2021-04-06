package org.obd.metrics.units;

import java.io.IOException;
import java.util.Optional;

public interface UnitsRegistry {

	Optional<Units> findByName(String name);

	Optional<Units> findById(Long id);

	static UnitsRegistry instance() throws IOException {
		var registry = new DefaultUnitRegistry();
		try (var openStream = Thread.currentThread().getContextClassLoader().getResource("units.json").openStream()) {
			registry.load(openStream);
		}
		return registry;
	}
}
