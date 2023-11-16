package org.matsim.core.mobsim.qsim.communication.service.worker.sync;

import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.groups.ParallelizationConfigGroup;
import org.matsim.core.mobsim.qsim.communication.Connection;
import org.matsim.core.mobsim.qsim.communication.Subscriber;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;
import org.matsim.core.mobsim.qsim.communication.model.WorkerId;
import org.matsim.core.mobsim.qsim.communication.model.messages.FinishSimulationMessage;
import org.matsim.core.mobsim.qsim.communication.model.messages.Message;
import org.matsim.core.mobsim.qsim.communication.model.messages.SyncStepMessage;
import org.matsim.core.mobsim.qsim.communication.service.worker.MessageSenderService;
import org.matsim.core.mobsim.qsim.communication.service.worker.MyWorkerId;
import org.matsim.core.mobsim.qsim.communication.service.worker.WorkerSubscriptionService;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class StepSynchronizationServiceImpl implements StepSynchronizationService, Subscriber {
	private final static Logger LOG = LogManager.getLogger(StepSynchronizationServiceImpl.class);

	private final WorkerSubscriptionService subscriptionService;
	//  private final TaskExecutorService taskExecutorService;
	private final MessageSenderService messageSenderService;
	private final BlockingQueue<SyncStepMessage> incomingMessages = new LinkedBlockingQueue<>();
	private final BlockingQueue<SyncStepMessage> futureIncomingMessages = new LinkedBlockingQueue<>();
	private final NeighbourManager neighbourManager;
	private final MyWorkerId myWorkerId;

	@PostConstruct
	void init() {
		subscriptionService.subscribe(this, MessagesTypeEnum.SyncStepMessage);
	}

	@Inject
	public StepSynchronizationServiceImpl(
		WorkerSubscriptionService subscriptionService,
		MessageSenderService messageSenderService,
		NeighbourManager neighbourManager,
		MyWorkerId myWorkerId
	) {
		this.neighbourManager = neighbourManager;
		this.myWorkerId = myWorkerId;
		this.messageSenderService = messageSenderService;
		this.subscriptionService = subscriptionService;
//		this.taskExecutorService = taskExecutorService;
		init();
	}

	@Override
	public synchronized void sendSyncMessageToNeighbours() {
		neighbourManager.sendSyncMessageToNeighbours();
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
	public /*synchronized*/ void getSyncMessages() {
		int countOfNeighbours = neighbourManager.getNumberOfNeighbours();
//		LOG.info(Thread.currentThread() + "::: Getting all sync messages from: " + countOfNeighbours + " neighbours"); // info for demonstration
//		List<Future<?>> injectIncomingCarFutures = new LinkedList<>();  TODO this will for actual processing
		int consumedMessages = 0;
		// List<Runnable> injectIncomingCarTasks = new LinkedList<>();

		while (consumedMessages < countOfNeighbours) {
			try {
				LOG.info(Thread.currentThread() + " waiting for messages..."
					+ ", incomingMessages: " + incomingMessages.size()
					+ ", futureIM: " + futureIncomingMessages.size());

				SyncStepMessage msg = incomingMessages.take(); // TODO processing of this f$@#ing message

				LOG.info(Thread.currentThread() + " got something here"
					+ ", incomingMessages: " + incomingMessages.size()
					+ ", futureIM: " + futureIncomingMessages.size());

//				List<Future<?>> f = taskExecutorService.executeBatchReturnFutures(
//					List.of(new InjectIncomingCarsTask(msg.getCars(), mapFragment)));
//				injectIncomingCarFutures.addAll(f);
				// injectIncomingCarTasks.addAll(List.of(new InjectIncomingCarsTask(msg.getCars(), mapFragment)));
				consumedMessages += 1;

			} catch (InterruptedException e) {
				LOG.error("Exception when waiting for cars: " + e);
				throw new RuntimeException(e);
			}
		}

		LOG.info(Thread.currentThread() + "::: Got all sync messages"
			+ ", incomingMessages: " + incomingMessages.size()
			+ ", futureIM: " + futureIncomingMessages.size());

//		taskExecutorService.waitForAllTaskFinished(injectIncomingCarFutures);
		// taskExecutorService.executeBatch(injectIncomingCarTasks);
		incomingMessages.clear();
		futureIncomingMessages.drainTo(incomingMessages);

		LOG.info(Thread.currentThread() + "::: Drain"
			+ ", incomingMessages: " + incomingMessages.size()
			+ ", futureIM: " + futureIncomingMessages.size());
	}

	@Override
	public void notify(Message message) {
		if (message.getMessageType() != MessagesTypeEnum.SyncStepMessage) {
			return;
		}

		SyncStepMessage carTransferMessage = (SyncStepMessage) message;
		LOG.info(Thread.currentThread() + "SyncStepMessage from w: " + carTransferMessage.getWorkerId() +
			", random: " + carTransferMessage.getRandom()
			+ ", step: " + carTransferMessage.getStep()
			+ ", incomingMessages: " + incomingMessages.size()
			+ ", futureIM: " + futureIncomingMessages.size());
		if (incomingMessages.contains(carTransferMessage)) {
			futureIncomingMessages.add(carTransferMessage);
		} else {
			incomingMessages.add(carTransferMessage);
			// notifyAll();
		}
	}
}
