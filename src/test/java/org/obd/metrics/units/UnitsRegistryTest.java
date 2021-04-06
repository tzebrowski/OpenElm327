package org.obd.metrics.units;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class UnitsRegistryTest {

	@Test
	public void findByNameTest() throws IOException {
		var instance = UnitsRegistry.instance();
		var units = instance.findByName("Temperature");
		Assertions.assertThat(units).isNotNull();
		Assertions.assertThat(units.get().getName()).isEqualTo("Temperature");
	}

	@Test
	public void findByIdTest() throws IOException {
		var instance = UnitsRegistry.instance();
		var units = instance.findById(1l);
		Assertions.assertThat(units).isNotNull();
		Assertions.assertThat(units.get().getName()).isEqualTo("Temperature");
	}
}
