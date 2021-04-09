package org.obd.metrics;

import java.util.List;

import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.batch.Batchable;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.connection.Connector;
import org.obd.metrics.pid.PidRegistry;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
final class CommandExecutor {

	@Default
	private static final List<String> ERRORS = List.of("unabletoconnect", "stopped", "error", "canerror", "businit");

	private final CodecRegistry codecRegistry;
	private final Connector connector;
	private final Lifecycle lifecycle;
	private final HierarchicalPublishSubject<Reply<?>> publisher;
	private final PidRegistry pids;

	void execute(Command command) {

		connector.transmit(command);
		var data = connector.receive();
		if (null == data || data.contains("nodata")) {
			log.debug("Recieved no data.");
		} else if (ERRORS.contains(data)) {
			log.debug("Recieve device error: {}", data);
			lifecycle.onError(data, null);
		} else if (command instanceof Batchable) {
			((Batchable) command).decode(data).forEach(this::decodeConvertAndPublish);
		} else if (command instanceof ObdCommand) {
			decodeConvertAndPublish((ObdCommand) command, data);
		} else {
			publisher.onNext(Reply.builder().command(command).raw(data).build());
		}
	}

	private void decodeConvertAndPublish(final ObdCommand command, final String data) {
		var codec = codecRegistry.findCodec(command);
		var allVariants = pids.findAllBy(command.getPid().getPid());
		allVariants.forEach(pDef -> {
			var value = codec.map(p -> p.decode(pDef, data)).orElse(null);
			var metric = ObdMetric.builder().command(allVariants.size() == 1 ? command : new ObdCommand(pDef)).raw(data)
			        .value(value).build();

			publisher.onNext(metric);
		});
	}
}
