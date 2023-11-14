package org.matsim.core.mobsim.qsim.communication.service.worker.sync;

import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.core.config.groups.ParallelizationConfigGroup;
import org.matsim.core.mobsim.qsim.communication.Subscriber;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;
import org.matsim.core.mobsim.qsim.communication.model.messages.FinishSimulationMessage;
import org.matsim.core.mobsim.qsim.communication.model.messages.Message;
import org.matsim.core.mobsim.qsim.communication.model.messages.SyncStepMessage;
import org.matsim.core.mobsim.qsim.communication.service.worker.MessageSenderService;
import org.matsim.core.mobsim.qsim.communication.service.worker.MyWorkerId;
import org.matsim.core.mobsim.qsim.communication.service.worker.WorkerSubscriptionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StepSynchronizationServiceImpl implements StepSynchronizationService, Subscriber {
	private final static Logger LOG = LogManager.getLogger(StepSynchronizationServiceImpl.class);

	private final WorkerSubscriptionService subscriptionService;
	//	private final TaskExecutorService taskExecutorService;
	private final MessageSenderService messageSenderService;
	private final List<SyncStepMessage> incomingMessages = new ArrayList<>();
	private final List<SyncStepMessage> futureIncomingMessages = new ArrayList<>();
	private final ParallelizationConfigGroup configuration;
	private final MyWorkerId myWorkerId;
	private final NeighbourManager neighbourManager;

	@Inject
	public StepSynchronizationServiceImpl(WorkerSubscriptionService subscriptionService,
//										  TaskExecutorService taskExecutorService,
										  MessageSenderService messageSenderService,
										  ParallelizationConfigGroup configuration,
										  MyWorkerId myWorkerId,
										  NeighbourManager neighbourManager) {
		this.subscriptionService = subscriptionService;
//		this.taskExecutorService = taskExecutorService;
		this.messageSenderService = messageSenderService;
		this.configuration = configuration;
		this.myWorkerId = myWorkerId;
		this.neighbourManager = neighbourManager;
		init();
	}

	private void init() {
		subscriptionService.subscribe(this, MessagesTypeEnum.SyncStepMessage);
	}

	@Override
	public void sendSyncMessageToNeighbours() {
		SyncStepMessage syncMsg = new SyncStepMessage(myWorkerId.get());
		neighbourManager.sendToNeighbours(syncMsg);
	}

	@Override
	public void sendFinishMessageToServer() {
		LOG.info("Worker finished simulation");
		FinishSimulationMessage finishSimulationMsg = new FinishSimulationMessage(myWorkerId.get());
		try {
			messageSenderService.sendServerMessage(finishSimulationMsg);
		} catch (IOException e) {
			LOG.error("Error with send finish simulation message", e);
		}
	}

	@Override
	public synchronized void getSyncMessages() {
		int countOfNeighbours = neighbourManager.getNumberOfNeighbours();
		LOG.info("Getting all sync messages from: " + countOfNeighbours + " neighbours"); // info for demonstration
		int readedMessage = 0;
		while (incomingMessages.size() < countOfNeighbours && readedMessage < countOfNeighbours) {
			try {
				this.wait(1000);
				readedMessage += applyMessages(readedMessage);
			} catch (InterruptedException e) {
				LOG.error(e.getMessage());
				throw new RuntimeException(e);
			}
		}

		applyMessages(readedMessage);
		incomingMessages.addAll(futureIncomingMessages);
		futureIncomingMessages.clear();

		LOG.info("Got all sync messages");
	}

	private int applyMessages(int start) {
		List<SyncStepMessage> injectIncomingCarTasks = incomingMessages.subList(start, incomingMessages.size());
//			.stream()
//			.map(message -> new InjectIncomingCarsTask(message.getCars(), mapFragment))
//			.collect(Collectors.toList());

//		taskExecutorService.executeBatch(injectIncomingCarTasks);
		return injectIncomingCarTasks.size();
	}

	@Override
	public synchronized void notify(Message message) {
		if (message.getMessageType() != MessagesTypeEnum.SyncStepMessage) {
			return;
		}

		SyncStepMessage carTransferMessage = (SyncStepMessage) message;
		if (incomingMessages.contains(carTransferMessage)) {
			futureIncomingMessages.add(carTransferMessage);
		} else {
			incomingMessages.add(carTransferMessage);
		}

		notifyAll();
	}
}
