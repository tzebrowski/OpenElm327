package org.obd.metrics.pid;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.obd.metrics.units.UnitsRegistry;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

public interface PidRegistry {

	void register(PidDefinition def);

	void register(List<PidDefinition> pids);

	PidDefinition findBy(Long id);

	PidDefinition findBy(String pid);

	Collection<PidDefinition> findAllBy(String pid);

	Collection<PidDefinition> findAll();

	@Builder
	static PidRegistry build(@NonNull @Singular("source") List<InputStream> sources, UnitsRegistry unitsRegistry) {
		var instance = new DefaultRegistry(unitsRegistry);
		sources.forEach(inputStream -> {
			instance.load(inputStream);
		});
		return instance;
	}
}