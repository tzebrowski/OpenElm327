package org.openobd2.core.converter;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.pid.PidDefinitionRegistry;

public class EngineRpmCommandTest {
	@Test
	public void possitiveTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("definitions.json")) {

			final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(source).build();

			FormulaEvaluator converterEngine = FormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();

			String rawData = "410c541B";
			Object temp = converterEngine.convert(rawData);
			Assertions.assertThat(temp).isEqualTo(5382.75);
		}

	}
}
