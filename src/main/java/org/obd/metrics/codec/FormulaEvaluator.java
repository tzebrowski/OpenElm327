package org.obd.metrics.codec;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.obd.metrics.pid.PidDefinition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class FormulaEvaluator implements Codec<Number> {

	private final MetricsDecoder decoder = new MetricsDecoder();

	private final List<String> params = IntStream.range(65, 91).boxed().map(ch -> String.valueOf((char) ch.byteValue()))
			.collect(Collectors.toList()); // A - Z

	private final ScriptEngine jsEngine;

	@Builder
	public static FormulaEvaluator build(@NonNull String engine) {
		return new FormulaEvaluator(new ScriptEngineManager().getEngineByName(engine));
	}

	@Override
	public Number decode(@NonNull PidDefinition pid, @NonNull String rawData) {

		log.debug("Found PID definition: {}", pid);
		if (pid.getFormula() == null || pid.getFormula().length() == 0) {
			log.debug("No formula find in {} for: {}", pid, rawData);
		} else {
			if (decoder.isSuccessAnswerCode(pid, rawData)) {
				try {
					updateFormulaParameters(rawData, pid);

					var eval = jsEngine.eval(pid.getFormula());
					var value = Number.class.cast(eval);
					return convert(pid, value);

				} catch (Throwable e) {
					log.trace("Failed to evaluate the formula {}", pid.getFormula(), e);
					log.error("Failed to evaluate the formula {}", pid.getFormula());
				}
			} else {
				log.warn("Answer code is incorrect for: {}", rawData);
			}
		}

		return null;
	}

	private Number convert(PidDefinition pid, Number value) {
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

	private void updateFormulaParameters(String rawData, PidDefinition pid) {
		var rawAnswerData = decoder.getRawAnswerData(pid, rawData);
		for (int i = 0, j = 0; i < rawAnswerData.length(); i += 2, j++) {
			final String hexValue = rawAnswerData.substring(i, i + 2);
			jsEngine.put(params.get(j), Integer.parseInt(hexValue, 16));
		}
	}
}
