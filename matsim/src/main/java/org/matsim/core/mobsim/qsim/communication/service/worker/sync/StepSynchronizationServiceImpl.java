package org.matsim.core.mobsim.qsim.communication.service.worker.sync;

import com.google.inject.Inject;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.config.groups.ParallelizationConfigGroup;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.TeleportationEngine;
import org.matsim.core.mobsim.qsim.agents.BasicPlanAgentImpl;
import org.matsim.core.mobsim.qsim.agents.PersonDriverAgentImpl;
import org.matsim.core.mobsim.qsim.communication.Connection;
import org.matsim.core.mobsim.qsim.communication.Subscriber;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;
import org.matsim.core.mobsim.qsim.communication.model.WorkerId;
import org.matsim.core.mobsim.qsim.communication.model.matisim.DeserializeUtil;
import org.matsim.core.mobsim.qsim.communication.model.matisim.SerializedBasicPlanAgentImpl;
import org.matsim.core.mobsim.qsim.communication.model.matisim.SerializedQVehicle;
import org.matsim.core.mobsim.qsim.communication.model.messages.FinishSimulationMessage;
import org.matsim.core.mobsim.qsim.communication.model.messages.Message;
import org.matsim.core.mobsim.qsim.communication.model.messages.SyncStepMessage;
import org.matsim.core.mobsim.qsim.communication.model.messages.TeleportationMessage;
import org.matsim.core.mobsim.qsim.communication.service.worker.MessageSenderService;
import org.matsim.core.mobsim.qsim.communication.service.worker.MyWorkerId;
import org.matsim.core.mobsim.qsim.communication.service.worker.WorkerSubscriptionService;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.interfaces.NetsimNetwork;
import org.matsim.core.mobsim.qsim.qnetsimengine.QLaneI;
import org.matsim.core.mobsim.qsim.qnetsimengine.QLinkI;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicleImpl;
import org.matsim.core.population.routes.GenericRouteImpl;

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
import java.util.stream.IntStream;

public class StepSynchronizationServiceImpl implements StepSynchronizationService, Subscriber {
	private final static Logger LOG = LogManager.getLogger(StepSynchronizationServiceImpl.class);

	private final WorkerSubscriptionService subscriptionService;
	//  private final TaskExecutorService taskExecutorService;
	private final MessageSenderService messageSenderService;
	private final BlockingQueue<SyncStepMessage> incomingMessages = new LinkedBlockingQueue<>();
	private final BlockingQueue<SyncStepMessage> futureIncomingMessages = new PriorityBlockingQueue<>();
	private final ParallelizationConfigGroup configuration;
	private DeserializeUtil deserializeUtil;
	private final MyWorkerId myWorkerId;

	private final Network network;
	private final Map<WorkerId, Connection> neighbourRepository = new HashMap<>();

	// The key is the unwrapped WorkerId for which we want to accumulate vehicles to send to
	// partition -> List of Vehicles (for normal exchange of vehicles)
	private final Map<String, List<QVehicle>> vehiclesToSend = new HashMap<>();
	// linkId -> amount of space used on this link
	private final Map<String, Double> usedSpaceIncomingLanes = new HashMap<>();

	// partition -> Triple (for teleportation)
	private final Map<String, List<Triple<Double, SerializedBasicPlanAgentImpl, String/*Id<Link> */>>> teleportedAgents = new HashMap<>();

	private final AtomicInteger sendingStep = new AtomicInteger(0);

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
		Network network,
		DeserializeUtil deserializeUtil
	) {
		this.myWorkerId = myWorkerId;
		this.network = network;
		this.messageSenderService = messageSenderService;
		this.subscriptionService = subscriptionService;
//		this.taskExecutorService = taskExecutorService;
		this.configuration = configuration;
		this.deserializeUtil = deserializeUtil;
		init();
	}


	@Override
	public void prepareDataToBeSend(Collection<QVehicle> outGoingVehicles, Map<Id<Link>, Double> usedSpaceIncomingLanes) {
		for (var veh : outGoingVehicles) {
			if (veh == null) {// TODO vehicle gets lost at some point - dont know why, but matsim removes it at some point...
				LOG.warn("SKIPPING VEHICLE without vehicle ");
			} else if (veh.getDriver() != null) { // TODO driver gets lost at some point - dont know why, but matsim removes it at some point...
				var _currLinkId = veh.getDriver().getCurrentLinkId();
				if (_currLinkId != null) {
					String partition = String.valueOf(network.getLinks().get(_currLinkId).getAttributes().getAttribute("partition"));
//				collect all vehicles going to each worker and send them in single iteration
					vehiclesToSend.putIfAbsent(partition, new ArrayList<>());
					vehiclesToSend.get(partition).add(veh);

					var index = ((PersonDriverAgentImpl) (veh.getDriver())).getBasicAgentDelegate().getCurrentPlanElementIndex();
					var element = ((PersonDriverAgentImpl) (veh.getDriver())).getBasicAgentDelegate().getPlan().getPlanElements().get(index);
					var leg = (Leg) element;
					if (leg.getRoute() instanceof GenericRouteImpl) {
						System.out.println("dupa");
					}


				} else {
					LOG.warn("SKIPPING VEHICLE without current link");
					throw new RuntimeException("This one did not happened yet, so if there is a driver there is also a current link id..." +
						" If this exception is thrown, then we have another edge case... ");
				}
			} else {
				LOG.warn("SKIPPING VEHICLE without driver...");
			}
		}

		for (Map.Entry<Id<Link>, Double> entry : usedSpaceIncomingLanes.entrySet()) {
			this.usedSpaceIncomingLanes.put(entry.getKey().toString(), entry.getValue());
		}
	}

	@Override
	public void setupNeighboursConnections() {
		getMyNeighboursIds()
			.stream()
			.map(rawWorkerId -> new WorkerId(String.valueOf(rawWorkerId)))
			.filter(messageSenderService.getConnectionMap()::containsKey)
			.forEach(workerId -> neighbourRepository.put(workerId, messageSenderService.getConnectionMap().get(workerId)));

		getMyNeighboursIds().forEach(neighbour -> vehiclesToSend.putIfAbsent(String.valueOf(neighbour), new ArrayList<>()));

		IntStream.range(0, configuration.getWorkerCount())
			.mapToObj(rawWorkerId -> new WorkerId(String.valueOf(rawWorkerId)))
			.filter(messageSenderService.getConnectionMap()::containsKey)
			.forEach(workerId -> teleportedAgents.putIfAbsent(workerId.getId(), new ArrayList<>()));
	}

	@Override
	public void setNetsim(Netsim simulation) {
		this.deserializeUtil.setNetsim(simulation);
	}

	@Override
	public void addMosibAgentForTeleportation(double now, MobsimAgent agent, Id<Link> linkId) {
		if (agent instanceof BasicPlanAgentImpl) {
			String partition = String.valueOf(network.getLinks().get(linkId).getAttributes().getAttribute("partition"));
			teleportedAgents.putIfAbsent(partition, new ArrayList<>());
			teleportedAgents.get(partition).add(Triple.of(now, new SerializedBasicPlanAgentImpl((BasicPlanAgentImpl) agent), linkId.toString()));
		} else
			throw new RuntimeException("Some other type than BasicPlanAgentImpl when addMosibAgentForTeleportation");
	}

	@Override
	public void setMobsimTimer(MobsimTimer mobsimTimer) {
		this.deserializeUtil.setMobsimTimer(mobsimTimer);
	}

	@Override
	public int getNumberOfNeighbours() {
		return neighbourRepository.size();
	}

	@Override
	public void sendSyncMessageToNeighbours() {
		LOG.debug(Thread.currentThread() + "::: sendSyncMessageToNeighbours");
		int currentStep = sendingStep.getAndIncrement();
		sendToNeighbours(currentStep);
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

	private void sendToNeighbours(int step) {
		neighbourRepository.forEach((workerId, connection) -> {
			SyncStepMessage syncMsg = new SyncStepMessage(
				myWorkerId.get(),
				ThreadLocalRandom.current().nextInt(),
				step,
				vehiclesToSend.get(workerId.getId()).stream().map(SerializedQVehicle::new).collect(Collectors.toList()),
				usedSpaceIncomingLanes,
				teleportedAgents.get(workerId.getId())
			);
			try {
				connection.send(syncMsg);
				LOG.debug(Thread.currentThread() + "::: Sending sync " + syncMsg.getMessageType() + " to neighbour: "
					+ workerId.getId() + " with msgId: " + ((SyncStepMessage) syncMsg).getFromWorkerId() +
					", random: " + ((SyncStepMessage) syncMsg).getRandom()
					+ ", step: " + ((SyncStepMessage) syncMsg).getStep());
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			vehiclesToSend.get(workerId.getId()).clear();
			teleportedAgents.get(workerId.getId()).clear();
		});


		teleportedAgents.keySet().forEach(workerId -> {
			if (teleportedAgents.get(workerId).size() > 0) {
				try {
					messageSenderService.send(new WorkerId(workerId), new TeleportationMessage(myWorkerId.get(), teleportedAgents.get(workerId)));
				} catch (IOException e) {
					LOG.error("Problem while sending teleported agents: " + e);
					throw new RuntimeException(e);
				}
			}
			teleportedAgents.get(workerId).clear();
		});
		usedSpaceIncomingLanes.clear();
	}

	@Override
	public void getSyncMessagesAndProcess(NetsimNetwork netsimNetwork, TeleportationEngine teleportationEngine) {
		int countOfNeighbours = this.getNumberOfNeighbours();
		LOG.debug(Thread.currentThread() + "::: Getting all sync messages from: " + countOfNeighbours + " neighbours"); // info for demonstration
//		List<Future<?>> injectIncomingCarFutures = new LinkedList<>();  TODO this will for actual processing
		incomingMessageBuffer.initConsumedMessages();
		List<QVehicleImpl> receivedVehicles = new ArrayList<>();
		List<Triple<Double, MobsimAgent, String>> agentsToBeTeleported = new ArrayList<>();
		Map<String, Double> usedSpaceIncomingLanes = new HashMap<>();
		while (incomingMessageBuffer.getConsumedMessages() < countOfNeighbours) {
			try {
				LOG.debug(Thread.currentThread() + " waiting for messages..."
					+ ", incomingMessages: " + incomingMessages.size()
					+ ", futureIM: " + futureIncomingMessages.size());
				SyncStepMessage msg = incomingMessages.take();

				receivedVehicles.addAll(msg.getVehicles().stream().map(deserializeUtil::deserializeQVehicle).toList());
				usedSpaceIncomingLanes.putAll(msg.getUsedSpaceIncomingLanes());
				agentsToBeTeleported.addAll(msg.getTeleportedAgents().stream().map(
					triple -> Triple.of(triple.getLeft(), deserializeUtil.deserializeMobsimAgent(triple.getMiddle()), triple.getRight())).toList()
				);

				LOG.debug(Thread.currentThread() + " got something here"
					+ ", incomingMessages: " + incomingMessages.size()
					+ ", futureIM: " + futureIncomingMessages.size());

				this.incomingMessageBuffer.processNewMsg(countOfNeighbours);

				LOG.debug(Thread.currentThread() + " processed");
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		LOG.debug(Thread.currentThread() + "::: Got all sync messages"
			+ ", incomingMessages: " + incomingMessages.size()
			+ ", futureIM: " + futureIncomingMessages.size());


		// process it
		// setting the capacity from remote links
		for (Map.Entry<String, Double> entry : usedSpaceIncomingLanes.entrySet()) {
			Id<Link> laneId = Id.createLinkId(entry.getKey());
			Double usedSapceOnLane = entry.getValue();
			((QLinkI) netsimNetwork.getNetsimLinks().get(laneId)).setLoadIndicator(usedSapceOnLane);
		}

		// handling the teleported vehicles
		// TODO take teleported ones and call handleDeparture on this agent
		for (var agent : agentsToBeTeleported) {
			teleportationEngine.handleDeparture(agent.getLeft(), agent.getMiddle(), Id.createLinkId(agent.getRight()));
		}

		// insert received vehicles from neighbours
		receivedVehicles.forEach(vehicle -> {
			QLinkI currentQueueLink = (QLinkI) netsimNetwork.getNetsimLinks().get(vehicle.getDriver().getCurrentLinkId());
			QLaneI currentQueueLane = currentQueueLink.getAcceptingQLane();
			if (currentQueueLane.isAcceptingFromUpstream()) {
				currentQueueLane.addFromUpstream(vehicle);
			}
		});
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
