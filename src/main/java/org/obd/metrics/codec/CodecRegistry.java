package org.obd.metrics.codec;

import java.util.Optional;

import org.obd.metrics.command.Command;

import lombok.Builder;
import lombok.NonNull;

public interface CodecRegistry {

	void register(Command command, Codec<?> codec);

	Optional<Codec<?>> findCodec(Command command);

	@Builder
	public static DefaultRegistry of(@NonNull String equationEngine, GeneratorSpec generatorSpec) {
		Codec<Number> evaluator = FormulaEvaluator
				.builder()
				.engine(equationEngine)
				.build();

		if (generatorSpec != null && generatorSpec.isEnabled()) {
			evaluator = new Generator(evaluator, generatorSpec);
		}

		return new DefaultRegistry(evaluator);
	}
}