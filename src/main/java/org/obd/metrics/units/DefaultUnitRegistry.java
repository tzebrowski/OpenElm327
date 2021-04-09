package org.obd.metrics.units;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultUnitRegistry implements UnitsRegistry {

	private final Map<Object, Units> units = new HashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public Optional<Units> findById(Long id) {
		if (null == id) {
			return Optional.empty();
		} else {
			return Optional.ofNullable(units.get(id));
		}
	}

	@Override
	public Optional<Units> findByName(String name) {
		if (null == name) {
			return Optional.empty();
		} else {
			return Optional.ofNullable(units.get(name));
		}
	}

	void load(final InputStream inputStream) {
		try {
			if (null == inputStream) {
				log.error("Was not able to load units configuration");
			} else {
				var readValue = objectMapper.readValue(inputStream, Units[].class);
				log.info("Load {} pid definitions", readValue.length);
				for (var unit : readValue) {
					units.put(unit.getName(), unit);
					units.put(unit.getId(), unit);
				}
			}
		} catch (IOException e) {
			log.error("Failed to load units configuration", e);
		}
	}
}
