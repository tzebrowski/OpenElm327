package org.obd.metrics.pid;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(of = { "id" })
public class PidDefinition implements Comparable<PidDefinition> {

	public static enum Type {
		INT, DOUBLE, SHORT
	}

	@Getter
	@NonNull
	private Long id;

	@Getter
	@NonNull
	private int length;

	@Getter
	@NonNull
	private String formula;

	@Getter
	@NonNull
	private String mode;

	@Getter
	@NonNull
	private String pid;

	@Getter
	@NonNull
	private String units;

	@Getter
	@NonNull
	private String description;

	@Getter
	@NonNull
	private Number min;

	@Getter
	@NonNull
	private Number max;

	// optional
	@Getter
	@NonNull
	private Type type;

	@Getter
	private Integer priority = 5;

	
	@Getter
	private Long unitsId;

	
	@Override
	public int compareTo(PidDefinition o) {
		return o.priority.compareTo(this.priority);
	}
}
