package org.matsim.core.mobsim.qsim.communication.startingup;

import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.core.config.groups.ParallelizationConfigGroup;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.communication.Subscriber;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;
import org.matsim.core.mobsim.qsim.communication.model.WorkerId;
import org.matsim.core.mobsim.qsim.communication.model.messages.CompletedInitializationMessage;
import org.matsim.core.mobsim.qsim.communication.model.messages.Message;
import org.matsim.core.mobsim.qsim.communication.model.messages.ServerInitializationMessage;
import org.matsim.core.mobsim.qsim.communication.model.messages.WorkerConnectionMessage;
import org.matsim.core.mobsim.qsim.communication.service.worker.MessageReceiverService;
import org.matsim.core.mobsim.qsim.communication.service.worker.MessageSenderService;
import org.matsim.core.mobsim.qsim.communication.service.worker.MyWorkerId;
import org.matsim.core.mobsim.qsim.communication.service.worker.WorkerSubscriptionService;
import org.matsim.core.mobsim.qsim.communication.service.worker.sync.NeighbourManager;
import org.matsim.core.mobsim.qsim.communication.service.worker.sync.StepSynchronizationService;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class WorkerStrategyService implements Strategy, Runnable, Subscriber {
	private final static Logger LOG = LogManager.getLogger(WorkerStrategyService.class);
	private final WorkerSubscriptionService subscriptionService;
	private final ParallelizationConfigGroup configuration;
	private final MessageSenderService messageSenderService;
	private final MessageReceiverService messageReceiverService;
	private final ExecutorService simulationExecutor = newSingleThreadExecutor();
	public final MyWorkerId myWorkerId;
	private final StepSynchronizationService stepSynchronizationService;
	private final AtomicBoolean shouldRunSim = new AtomicBoolean(false);

	@Inject
	public WorkerStrategyService(WorkerSubscriptionService subscriptionService,
								 MessageSenderService messageSenderService,
								 MessageReceiverService messageReceiverService,
								 ParallelizationConfigGroup configuration,
								 MyWorkerId myWorkerId,
								 StepSynchronizationService stepSynchronizationService) {
		this.subscriptionService = subscriptionService;
		this.configuration = configuration;
		this.messageSenderService = messageSenderService;
		this.messageReceiverService = messageReceiverService;
		this.myWorkerId = myWorkerId;
		this.stepSynchronizationService = stepSynchronizationService;
		this.myWorkerId.create();
		//		init();
	}

	//	@PostConstruct
	public void init() {
		subscriptionService.subscribe(this, MessagesTypeEnum.RunSimulationMessage);
		subscriptionService.subscribe(this, MessagesTypeEnum.ServerInitializationMessage);
		subscriptionService.subscribe(this, MessagesTypeEnum.ShutDownMessage);
		subscriptionService.subscribe(this, MessagesTypeEnum.StopSimulationMessage);
		subscriptionService.subscribe(this, MessagesTypeEnum.ResumeSimulationMessage);
	}

	@Override
	public void executeStrategy(QSim qsim) {
		try {
			configuration.setWorkerId(myWorkerId.get());
			// For initialization connection with server address is extracted from socket it came from
			// The listening port of worker is only needed with its id
			messageSenderService.sendServerMessage(
				new WorkerConnectionMessage("", messageReceiverService.getPort(), myWorkerId.get())
			);
		} catch (Exception e) {
			LOG.error("Worker fail", e);
		}
	}

	@Override
	public void notify(Message message) {
		switch (message.getMessageType()) {
			case ServerInitializationMessage -> handleInitializationMessage(
				(ServerInitializationMessage) message);
			case RunSimulationMessage -> runSimulation();
			case ShutDownMessage -> shutDown();
			case StopSimulationMessage -> stopSimulation();
			case ResumeSimulationMessage -> resumeSimulation();
			default -> LOG.warn("Unhandled message " + message.getMessageType());
		}
	}

	private void shutDown() {
		LOG.info("Bye!");
		// TODO redo it! Find out how you can hook to close event?
//		int exitCode = SpringApplication.exit(context, (ExitCodeGenerator) () -> 0);
		System.exit(0);
	}

	private void handleInitializationMessage(ServerInitializationMessage message) {
		// TODO simulation initialization goes HERE...
		stepSynchronizationService.setupNeighboursConnections();
		try {
			messageSenderService.sendServerMessage(new CompletedInitializationMessage());
		} catch (IOException e) {
			LOG.error("Fail send CompletedInitializationMessage", e);
		}
	}

	private void runSimulation() {
		shouldRunSim.set(true);
		simulationExecutor.submit(this);
	}

	private void stopSimulation() {
		LOG.info("Worker with workerId: {} is stopped", myWorkerId.get());
		// here could be logic for stopping
	}

	private void resumeSimulation() {
		LOG.info("Worker with workerId: {} is resumed", myWorkerId.get());
		// here could be logic for resuming
	}

	@Override
	public void run() {
		// TODO proper simulation RUN IT (the simulation loop) HERE!

//		try {
//			LOG.info("Worker finish simulation");
//			messageSenderService.sendServerMessage(new FinishSimulationMessage(workerId.getId()));
//		} catch (IOException e) {
//			LOG.error("Error with send finish simulation message", e);
//		}
	}

	public void waitForSimToRun() {
		while (!this.shouldRunSim.get()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
