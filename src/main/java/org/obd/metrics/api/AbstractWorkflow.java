package org.obd.metrics.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.obd.metrics.CommandLoop;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.GeneratorSpec;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.AdapterConnection;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.pid.Urls;
import org.obd.metrics.statistics.StatisticsRegistry;
import org.obd.metrics.units.UnitsRegistry;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractWorkflow implements Workflow {

	protected PidSpec pidSpec;
	protected Producer commandProducer;
	protected final UnitsRegistry unitsRegistry;
	protected final CommandsBuffer commandsBuffer = CommandsBuffer.instance();

	@Getter
	protected StatisticsRegistry statisticsRegistry = StatisticsRegistry.builder().build();

	@Getter
	protected final PidRegistry pidRegistry;

	protected ReplyObserver<Reply<?>> replyObserver;
	protected final String equationEngine;
	protected Lifecycle lifecycle;

	// just a single thread in a pool
	private static final ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
	        new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	abstract List<ReplyObserver<Reply<?>>> getObservers();

	abstract void init(Adjustments adjustments);

	abstract Supplier<Optional<Collection<ObdCommand>>> getCommandsSupplier(Adjustments adjustements, Query query);

	protected AbstractWorkflow(PidSpec pidSpec, String equationEngine, ReplyObserver<Reply<?>> observer,
	        Lifecycle statusObserver) throws IOException {
		this.pidSpec = pidSpec;
		this.equationEngine = equationEngine;
		this.replyObserver = observer;
		this.unitsRegistry = UnitsRegistry.instance();
		this.lifecycle = getLifecycle(statusObserver);

		List<InputStream> resources = Arrays.asList();
		try {
			resources = Urls.toStreams(pidSpec.getSources());
			this.pidRegistry = PidRegistry.builder().sources(resources).build();
		} finally {
			closeResources(resources);
		}
	}

	@Override
	public void stop() {
		log.info("Stopping the workflow: {}", getClass().getSimpleName());
		commandsBuffer.addFirst(new QuitCommand());
		log.info("Publishing lifecycle changes");
		lifecycle.onStopping();
	}

	@Override
	public void start(@NonNull AdapterConnection connection, @NonNull Query query, @NonNull Adjustments adjustements) {

		final Runnable task = () -> {
			var executorService = Executors.newFixedThreadPool(2);

			try {
				
				init(adjustements);

				log.info("Starting the workflow: {}. Batch enabled: {},generator: {}, selected PID's: {}",
				        getClass().getSimpleName(), adjustements.isBatchEnabled(), adjustements.getGenerator(),
				        query.getPids());

				statisticsRegistry.reset();

				final Supplier<Optional<Collection<ObdCommand>>> commandsSupplier = getCommandsSupplier(adjustements,
				        query);
				commandProducer = getProducer(adjustements, commandsSupplier);

				@SuppressWarnings("unchecked")
				var commandLoop = CommandLoop
				        .builder()
				        .connection(connection)
				        .buffer(commandsBuffer)
				        .observers(getObservers())
				        .observer(replyObserver)
				        .observer((ReplyObserver<Reply<?>>) statisticsRegistry)
				        .pids(pidRegistry)
				        .codecs(getCodecRegistry(adjustements.getGenerator()))
				        .lifecycle(lifecycle).build();

				executorService.invokeAll(Arrays.asList(commandLoop, commandProducer));

			} catch (InterruptedException e) {
				log.error("Failed to schedule workers.", e);
			} finally {
				log.info("Stopping the Workflow.");
				lifecycle.onStopped();
				executorService.shutdown();
			}
		};

		singleTaskPool.submit(task);
	}

	protected Producer getProducer(Adjustments adjustements, Supplier<Optional<Collection<ObdCommand>>> supplier) {
		return new Producer(statisticsRegistry, commandsBuffer, supplier, adjustements);
	}

	protected CodecRegistry getCodecRegistry(GeneratorSpec generatorSpec) {
		return CodecRegistry
				.builder()
				.unitsRegistry(unitsRegistry)
				.equationEngine(getEquationEngine(equationEngine))
				.generatorSpec(generatorSpec)
		        .build();
	}

	protected void closeResources(List<InputStream> resources) {
		resources.forEach(f -> {
			try {
				f.close();
			} catch (IOException e) {
			}
		});
	}

	protected Lifecycle getLifecycle(Lifecycle lifecycle) {
		return lifecycle == null ? Lifecycle.DEFAULT : lifecycle;
	}

	protected @NonNull String getEquationEngine(String equationEngine) {
		return equationEngine == null || equationEngine.length() == 0 ? "JavaScript" : equationEngine;
	}
}
