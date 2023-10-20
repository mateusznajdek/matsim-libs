package org.matsim.core.mobsim.qsim.communication.startingup;

import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.core.mobsim.qsim.communication.Configuration;
import org.matsim.core.mobsim.qsim.communication.Subscriber;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;
import org.matsim.core.mobsim.qsim.communication.model.WorkerId;
import org.matsim.core.mobsim.qsim.communication.model.messages.*;
import org.matsim.core.mobsim.qsim.communication.service.worker.MessageReceiverService;
import org.matsim.core.mobsim.qsim.communication.service.worker.MessageSenderService;
import org.matsim.core.mobsim.qsim.communication.service.worker.WorkerSubscriptionService;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class WorkerStrategyService implements Strategy, Runnable, Subscriber {
	private final static Logger LOG = LogManager.getLogger(WorkerStrategyService.class);
	private final WorkerSubscriptionService subscriptionService;
	private final Configuration configuration;
	private final MessageSenderService messageSenderService;
	private final MessageReceiverService messageReceiverService;
	private final ExecutorService simulationExecutor = newSingleThreadExecutor();
	private final WorkerId workerId;

	@Inject
	public WorkerStrategyService(WorkerSubscriptionService subscriptionService,
								 MessageSenderService messageSenderService,
								 MessageReceiverService messageReceiverService,
								 Configuration configuration) {
		this.subscriptionService = subscriptionService;
		this.configuration = configuration;
		this.messageSenderService = messageSenderService;
		this.messageReceiverService = messageReceiverService;
		this.workerId = WorkerId.unique(messageReceiverService.getPort());
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
	public void executeStrategy() {
		try {
			configuration.setWorkerId(workerId);
			// For initialization connection with server address is extracted from socket it came from
			// The listening port of worker is only needed with its id
			messageSenderService.sendServerMessage(
				new WorkerConnectionMessage("", messageReceiverService.getPort(), workerId.getId())
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
		// TODO redo it! Find out how you can hook to close event?
//		int exitCode = SpringApplication.exit(context, (ExitCodeGenerator) () -> 0);
//		System.exit(exitCode);
	}

	private void handleInitializationMessage(ServerInitializationMessage message) {
		// TODO simulation initialization goes HERE...
		try {
			messageSenderService.sendServerMessage(new CompletedInitializationMessage());
		} catch (IOException e) {
			LOG.error("Fail send CompletedInitializationMessage", e);
		}
	}

	private void runSimulation() {
		simulationExecutor.submit(this);
	}

	private void stopSimulation() {
		LOG.info("Worker with workerId: {} is stopped", workerId.getId());
		// here could be logic for stopping
	}

	private void resumeSimulation() {
		LOG.info("Worker with workerId: {} is resumed", workerId.getId());
		// here could be logic for resuming
	}

	@Override
	public void run() {

		// TODO proper simulation RUN IT (the simulation loop) HERE!

		try {
			LOG.info("Worker finish simulation");
			messageSenderService.sendServerMessage(new FinishSimulationMessage(workerId.getId()));
		} catch (IOException e) {
			LOG.error("Error with send finish simulation message", e);
		}
	}
}
