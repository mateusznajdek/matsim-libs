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
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StepSynchronizationServiceImpl implements StepSynchronizationService, Subscriber {
	private final static Logger LOG = LogManager.getLogger(StepSynchronizationServiceImpl.class);

	private final WorkerSubscriptionService subscriptionService;
	//  private final TaskExecutorService taskExecutorService;
	private final MessageSenderService messageSenderService;
	private final BlockingQueue<SyncStepMessage> incomingMessages = new LinkedBlockingQueue<>();
	private final BlockingQueue<SyncStepMessage> futureIncomingMessages = new PriorityBlockingQueue<>();
	private final ParallelizationConfigGroup configuration;
	private final MyWorkerId myWorkerId;

	private final Network network;
	private final Map<WorkerId, Connection> neighbourRepository = new HashMap<>();

	// The key is the unwrapped WorkerId for which we want to accumulate vehicles to send to
	private final Map<String, Collection<QVehicle>> vehiclesToSend = new HashMap<>();

	private final AtomicInteger sendingStep = new AtomicInteger(0);
	private final AtomicInteger receivingStep = new AtomicInteger(0);

	private IncomingMessageBuffer incomingMessageBuffer = new IncomingMessageBuffer(incomingMessages, futureIncomingMessages);

	@PostConstruct
	void init() {
		subscriptionService.subscribe(this, MessagesTypeEnum.SyncStepMessage);
	}

	@Inject
	public StepSynchronizationServiceImpl(
		WorkerSubscriptionService subscriptionService,
		MessageSenderService messageSenderService,
		ParallelizationConfigGroup configuration,
		MyWorkerId myWorkerId,
		Network network
	) {
		this.myWorkerId = myWorkerId;
		this.network = network;
		this.messageSenderService = messageSenderService;
		this.subscriptionService = subscriptionService;
//		this.taskExecutorService = taskExecutorService;
		this.configuration = configuration;
		init();
	}


	@Override
	public void collectCarsFromLane(Collection<QVehicle> outGoingVehicles) {
		// TODO uncomment
		for (var veh : outGoingVehicles) {
			var _currLinkId = veh.getDriver().getCurrentLinkId();
			String partition = String.valueOf(network.getLinks().get(_currLinkId).getAttributes().getAttribute("partition"));
//				collect all vehicles going to each worker and send them in single iteration
			vehiclesToSend.putIfAbsent(partition, new ArrayList<>());
			vehiclesToSend.get(partition).add(veh);
		}
	}

	@Override
	public void setupNeighboursConnections() {
		getMyNeighboursIds()
			.stream()
			.map(rawWorkerId -> new WorkerId(String.valueOf(rawWorkerId)))
			.filter(messageSenderService.getConnectionMap()::containsKey)
			.forEach(workerId -> neighbourRepository.put(workerId, messageSenderService.getConnectionMap().get(workerId)));
		// vs

//		connectionMap.forEach((workerId, connection) -> {
//			if (neighbourManager.getMyNeighboursIds().contains(Integer.valueOf(workerId.getId())))
//				neighbourRepository.put(workerId, connectionMap.get(workerId));
//		});
	}

	@Override
	public int getNumberOfNeighbours() {
		return neighbourRepository.size();
	}

	@Override
	public void sendSyncMessageToNeighbours() {
		SyncStepMessage syncMsg = new SyncStepMessage(myWorkerId.get(), ThreadLocalRandom.current().nextInt(), sendingStep.getAndIncrement());
		sendToNeighbours(syncMsg);
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

	private Set<Integer> getMyNeighboursIds() {
		return network.getLinks().values().stream()
			.filter(l -> !l.getToNode().getAttributes().getAttribute("partition").equals(l.getFromNode().getAttributes().getAttribute("partition")))
			.filter(l -> l.getFromNode().getAttributes().getAttribute("partition").equals(Integer.valueOf(myWorkerId.get())) ||
				l.getToNode().getAttributes().getAttribute("partition").equals(Integer.valueOf(myWorkerId.get())))
			.map(l -> {
				if (!l.getToNode().getAttributes().getAttribute("partition").equals(Integer.valueOf(myWorkerId.get()))) {
					return (Integer) l.getToNode().getAttributes().getAttribute("partition");
				} else if (!l.getFromNode().getAttributes().getAttribute("partition").equals(Integer.valueOf(myWorkerId.get()))) {
					return (Integer) l.getFromNode().getAttributes().getAttribute("partition");
				} else {
					throw new RuntimeException("Whole world is destroyed!");
				}
			})
			.collect(Collectors.toSet());
	}

	private void sendToNeighbours(Message message) {
		neighbourRepository.forEach((workerId, connection) -> {
			try {
				connection.send(message);
				LOG.debug(Thread.currentThread() + "::: Sending sync " + message.getMessageType() + " to neighbour: "
					+ workerId.getId() + " with msgId: " + ((SyncStepMessage) message).getWorkerId() +
					", random: " + ((SyncStepMessage) message).getRandom()
					+ ", step: " + ((SyncStepMessage) message).getStep());
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void getSyncMessages() {
		int countOfNeighbours = this.getNumberOfNeighbours();
		LOG.debug(Thread.currentThread() + "::: Getting all sync messages from: " + countOfNeighbours + " neighbours"); // info for demonstration
//		List<Future<?>> injectIncomingCarFutures = new LinkedList<>();  TODO this will for actual processing
		incomingMessageBuffer.initConsumedMessages();
		while (incomingMessageBuffer.getConsumedMessages() < countOfNeighbours) {
			try {
				LOG.debug(Thread.currentThread() + " waiting for messages..."
					+ ", incomingMessages: " + incomingMessages.size()
					+ ", futureIM: " + futureIncomingMessages.size());
				SyncStepMessage msg = incomingMessages.take();

				LOG.debug(Thread.currentThread() + " got something here"
					+ ", incomingMessages: " + incomingMessages.size()
					+ ", futureIM: " + futureIncomingMessages.size());

				this.incomingMessageBuffer.processNewMsg(countOfNeighbours);

				LOG.debug(Thread.currentThread() + " processed");

//				consumedMessages = incomingMessageBuffer.take(consumedMessages, countOfNeighbours);

			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
//				List<Future<?>> f = taskExecutorService.executeBatchReturnFutures(
//					List.of(new InjectIncomingCarsTask(msg.getCars(), mapFragment)));
//				injectIncomingCarFutures.addAll(f);
			// injectIncomingCarTasks.addAll(List.of(new InjectIncomingCarsTask(msg.getCars(), mapFragment)));

		}

		LOG.debug(Thread.currentThread() + "::: Got all sync messages"
			+ ", incomingMessages: " + incomingMessages.size()
			+ ", futureIM: " + futureIncomingMessages.size());


//		taskExecutorService.waitForAllTaskFinished(injectIncomingCarFutures);
		// taskExecutorService.executeBatch(injectIncomingCarTasks);

		LOG.debug(Thread.currentThread() + "::: Drain"
			+ ", incomingMessages: " + incomingMessages.size()
			+ ", futureIM: " + futureIncomingMessages.size());
	}


	@Override
	public void notify(Message message) {
		if (message.getMessageType() != MessagesTypeEnum.SyncStepMessage) {
			return;
		}
		SyncStepMessage carTransferMessage = (SyncStepMessage) message;
		this.incomingMessageBuffer.give(carTransferMessage);
	}
}
