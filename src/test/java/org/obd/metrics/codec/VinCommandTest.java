package org.obd.metrics.codec;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.command.VinCommand;

public class VinCommandTest {

	@Test
	public void correctVin() {
		String raw = "0140:4902015756571:5a5a5a314b5a412:4d363930333932";

		String decode = new VinCommand().decode(null, raw);
		Assertions.assertThat(decode).isEqualTo("WVWZZZ1KZAM690392");
	}

	@Test
	public void noSuccessCode() {
		String raw = "0140:4802015756571:5a5a5a314b5a412:4d363930333932";

		String decode = new VinCommand().decode(null, raw);
		Assertions.assertThat(decode).isEqualTo(null);
	}

	@Test
	public void incorrectHex() {
		String raw = "0140:4902015756571:5a5a5a314b5a412:4d363930333";

		String decode = new VinCommand().decode(null, raw);
		Assertions.assertThat(decode).isEqualTo(null);
	}

}
