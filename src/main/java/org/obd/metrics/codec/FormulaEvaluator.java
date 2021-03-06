package org.obd.metrics.codec;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluator implements Codec<Number> {

	private final MetricsDecoder decoder = new MetricsDecoder();

	private final List<String> params = IntStream.range(65, 91).boxed().map(ch -> String.valueOf((char) ch.byteValue()))
	        .collect(Collectors.toList()); // A - Z

	private final ScriptEngine jsEngine;

	FormulaEvaluator(String engine) {
		this.jsEngine = new ScriptEngineManager().getEngineByName(engine);
	}

	@Override
	public Number decode(PidDefinition pid, String rawData) {

		log.debug("Found PID definition: {}", pid);
		if (pid.getFormula() == null || pid.getFormula().length() == 0) {
			log.debug("No formula find in {} for: {}", pid, rawData);
		} else {
			if (decoder.isSuccessAnswerCode(pid, rawData)) {
				try {
					updateFormulaParameters(rawData, pid);

					var eval = jsEngine.eval(pid.getFormula());
					return TypesConverter.convert(pid, eval);

				} catch (Throwable e) {
					log.trace("Failed to evaluate the formula {}", pid.getFormula(), e);
					log.debug("Failed to evaluate the formula {}", pid.getFormula());
				}
			} else {
				log.debug("Answer code is incorrect for: {}", rawData);
			}
		}

		return null;
	}

	private void updateFormulaParameters(String rawData, PidDefinition pid) {
		var rawAnswerData = decoder.getRawAnswerData(pid, rawData);
		for (int i = 0, j = 0; i < rawAnswerData.length(); i += 2, j++) {
			final String hexValue = rawAnswerData.substring(i, i + 2);
			jsEngine.put(params.get(j), Integer.parseInt(hexValue, 16));
		}
	}
}
