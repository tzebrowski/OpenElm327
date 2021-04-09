package org.obd.metrics.api;

import java.util.ArrayList;
import java.util.List;

import org.obd.metrics.codec.GeneratorSpec;
import org.obd.metrics.codec.TargetUnit;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

/**
 * Defines an additional settings used by {@link Producer}
 * 
 * @since 0.6.0
 * @author tomasz.zebrowski
 */
@Builder
public final class Adjustments {

	public static Adjustments DEFAULT = Adjustments.builder().build();

	@Getter
	@Default
	private List<TargetUnit> units = new ArrayList<>();

	@Getter
	@Default
	private long initDelay = 5000l;

	@Getter
	@Default
	private final boolean batchEnabled = Boolean.FALSE;

	@Getter
	@Default
	private final GeneratorSpec generator = GeneratorSpec.DEFAULT;

	@Getter
	@Default
	private final AdaptiveTimeoutPolicy adaptiveTiming = AdaptiveTimeoutPolicy.DEFAULT;

	@Getter
	@Default
	private final ProducerPolicy producerPolicy = ProducerPolicy.DEFAULT;
}
