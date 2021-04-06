package org.obd.metrics.pid;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.obd.metrics.codec.MetricsDecoder;
import org.obd.metrics.units.UnitsRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultRegistry implements PidRegistry {

	private final MultiValuedMap<String, PidDefinition> definitions = new ArrayListValuedHashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final MetricsDecoder decoder = new MetricsDecoder();
	private final UnitsRegistry unitsRegistry;
	private String mode;

	@Override
	public void register(@NonNull PidDefinition pidDef) {
		log.debug("Register new pid: {}", pidDef);
		definitions.put(decoder.getPredictedAnswerCode(pidDef), pidDef);
		definitions.put((pidDef.getMode() + pidDef.getPid()).toLowerCase(), pidDef);
		definitions.put(toId(pidDef.getId()), pidDef);
	}

	@Override
	public void register(List<PidDefinition> pids) {
		pids.forEach(this::register);
	}

	@Override
	public PidDefinition findBy(@NonNull Long id) {
		return getFirstOne(toId(id));
	}

	@Override
	public PidDefinition findBy(String pid) {
		return getFirstOne((mode + pid).toLowerCase());
	}

	@Override
	public Collection<PidDefinition> findAllBy(String pid) {
		return definitions.get((mode + pid).toLowerCase());
	}

	@Override
	public Collection<PidDefinition> findAll() {
		return new HashSet<PidDefinition>(definitions.values());
	}

	void load(final InputStream inputStream) {
		try {
			if (null == inputStream) {
				log.error("Was not able to load pids configuration");
			} else {
				var definitions = objectMapper.readValue(inputStream, PidDefinition[].class);
				log.info("Load {} pid definitions", definitions.length);
				
				for (var pidDef : definitions) {
					this.definitions.put(decoder.getPredictedAnswerCode(pidDef), pidDef);
					this.definitions.put((pidDef.getMode() + pidDef.getPid()).toLowerCase(), pidDef);
					this.definitions.put(toId(pidDef.getId()), pidDef);
				}
				this.mode = definitions[0].getMode();
			}
		} catch (IOException e) {
			log.error("Failed to load pids configuration", e);
		}
	}

	private String toId(long id) {
		return "pid." + id;
	}

	private PidDefinition getFirstOne(String id) {
		return definitions.get(id).stream().findFirst().orElse(null);
	}
}
