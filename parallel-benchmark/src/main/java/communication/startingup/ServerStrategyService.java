package communication.startingup;

import com.google.inject.Inject;
import communication.Configuration;
import communication.model.messages.RunSimulationMessage;
import communication.model.messages.ServerInitializationMessage;
import communication.model.messages.ShutDownMessage;
import communication.model.serializable.ConnectionDto;
import communication.model.serializable.WorkerDataDto;
import communication.service.server.ConnectionInitializationService;
import communication.service.server.MessageSenderServerService;
import communication.service.server.WorkerRepository;
import communication.service.server.WorkerSynchronisationService;
import communication.service.worker.MessageReceiverService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static communication.model.MessagesTypeEnum.CompletedInitializationMessage;
import static communication.model.MessagesTypeEnum.FinishSimulationMessage;
import static communication.model.MessagesTypeEnum.WorkerConnectionMessage;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class ServerStrategyService implements Strategy {
	private final static Logger LOG = LogManager.getLogger(ServerStrategyService.class);

	private final WorkerSynchronisationService workerSynchronisationService;
	private final ConnectionInitializationService connectionInitializationService;
	private final Configuration configuration;
	private final WorkerStrategyService workerStrategyService;
	private final ExecutorService workerPrepareExecutor = newSingleThreadExecutor();
	private final MessageSenderServerService messageSenderServerService;
	private final WorkerRepository workerRepository;


	@Inject
	public ServerStrategyService(WorkerSynchronisationService workerSynchronisationService,
								 ConnectionInitializationService connectionInitializationService,
								 Configuration configuration,
								 WorkerStrategyService workerStrategyService,
								 MessageSenderServerService messageSenderServerService,
								 WorkerRepository workerRepository) {
		this.workerSynchronisationService = workerSynchronisationService;
		this.connectionInitializationService = connectionInitializationService;
		this.configuration = configuration;
		this.workerStrategyService = workerStrategyService;
		this.messageSenderServerService = messageSenderServerService;
		this.workerRepository = workerRepository;
	}

	@Override
	public void executeStrategy() throws InterruptedException {
		LOG.info("Running server");
		connectionInitializationService.init();
		workerPrepareExecutor.submit(new PrepareWorkerTask());

		LOG.info("Start waiting for all workers be in state WorkerConnection");
		workerSynchronisationService.waitForAllWorkers(WorkerConnectionMessage);

		// TODO do it later! -> map partitioning and stuff
		calculateAndDistributeConfiguration();

		LOG.info("Waiting for all workers be in state CompletedInitialization");

		workerSynchronisationService.waitForAllWorkers(CompletedInitializationMessage);

		distributeRunSimulationMessage();

		LOG.info("Waiting for end simulation");
		workerSynchronisationService.waitForAllWorkers(FinishSimulationMessage);
		LOG.info("Simulation finished");

		messageSenderServerService.broadcast(new ShutDownMessage());
		Thread.sleep(1000);
		shutDown();
	}

	private void calculateAndDistributeConfiguration() {
		Map<String, ServerInitializationMessage> workerId2ServerInitializationMessage = new HashMap<>();
		workerRepository.getAllWorkersIds().forEach(workerId -> {
			List<String> allWorkers = new ArrayList<>(workerRepository.getAllWorkersIds());
			allWorkers.remove(workerId);

			List<WorkerDataDto> workerDataDtos = allWorkers // TODO for now everyone will speak with everyone else
				.stream()
				.map(e -> new WorkerDataDto(List.of(), ConnectionDto.builder()
					.id(workerRepository.get(e).getWorkerId())
					.address(workerRepository.get(e).getAddress())
					.port(workerRepository.get(e).getPort())
					.build()))
				.toList();
			ServerInitializationMessage serverInitializationMessage = ServerInitializationMessage.builder()
				.patchIds(Collections.emptyList()) // TODO this should be changed (initialize with proper ids...)
				.workerInfo(workerDataDtos)
				.bigWorker(true)
				.build();
			workerId2ServerInitializationMessage.put(workerId, serverInitializationMessage);
		});

		workerId2ServerInitializationMessage.entrySet().forEach(
			e -> messageSenderServerService.send(e.getKey(), e.getValue())
		);
	}

	private void distributeRunSimulationMessage() {
		messageSenderServerService.broadcast(new RunSimulationMessage());
	}

	private void shutDown() {
		// TODO shutdown gracefully
//		int exitCode = SpringApplication.exit(context, (ExitCodeGenerator) () -> 0);
//		System.exit(exitCode);
	}

	private class PrepareWorkerTask implements Runnable {

		@Override
		public void run() {
			try {
				Thread.sleep(1000);
				workerStrategyService.executeStrategy();
			} catch (InterruptedException e) {
				LOG.error("Worker not started", e);
			} catch (Exception e) {
				LOG.error("Unexpected exception occurred", e);
			}
		}
	}
}
