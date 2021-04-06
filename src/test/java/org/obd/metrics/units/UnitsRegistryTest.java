package org.obd.metrics.units;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class UnitsRegistryTest {

	@Test
	public void findByNameTest() throws IOException {
		var instance = UnitsRegistry.instance();
		Unit unit = instance.findByName("Temperature");
		Assertions.assertThat(unit).isNotNull();
		Assertions.assertThat(unit.getName()).isEqualTo("Temperature");
	}
	
	
	@Test
	public void findByIdTest() throws IOException {
		var instance = UnitsRegistry.instance();
		Unit unit = instance.findById(1l);
		Assertions.assertThat(unit).isNotNull();
		Assertions.assertThat(unit.getName()).isEqualTo("Temperature");
	}
}
