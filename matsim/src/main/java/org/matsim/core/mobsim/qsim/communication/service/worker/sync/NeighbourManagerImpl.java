package org.matsim.core.mobsim.qsim.communication.service.worker.sync;

import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.mobsim.qsim.communication.Connection;
import org.matsim.core.mobsim.qsim.communication.model.WorkerId;
import org.matsim.core.mobsim.qsim.communication.model.messages.Message;
import org.matsim.core.mobsim.qsim.communication.model.messages.SyncStepMessage;
import org.matsim.core.mobsim.qsim.communication.service.worker.MessageSenderService;
import org.matsim.core.mobsim.qsim.communication.service.worker.MyWorkerId;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;

import java.io.IOException;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

// TODO merge it with messageSenderService???
public class NeighbourManagerImpl implements NeighbourManager {
	private final static Logger LOG = LogManager.getLogger(NeighbourManagerImpl.class);

	private final Network network;
	private final MyWorkerId myWorkerId;
	private final Map<WorkerId, Connection> neighbourRepository = new HashMap<>();

	// The key is the unwrapped WorkerId for which we want to accumulate vehicles to send to
	private final Map<String, Collection<QVehicle>> vehiclesToSend = new HashMap<>();

	private final MessageSenderService messageSenderService;

	private int tmpStep = 0;

	@Inject
	public NeighbourManagerImpl(Network network, MyWorkerId myWorkerId,
								MessageSenderService messageSenderService) {
		this.myWorkerId = myWorkerId;
		this.network = network;
		this.messageSenderService = messageSenderService;
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
	public void sendSyncMessageToNeighbours() {
		SyncStepMessage syncMsg = new SyncStepMessage(myWorkerId.get(), ThreadLocalRandom.current().nextInt(), tmpStep++);
		sendToNeighbours(syncMsg);
	}

	private void sendToNeighbours(Message message) {
		neighbourRepository.forEach((workerId, connection) -> {
			try {
				connection.send(message);
				LOG.info(Thread.currentThread() + "::: Sending sync " + message.getMessageType() + " to neighbour: "
					+ workerId.getId() + " with msgId: " + ((SyncStepMessage)message).getWorkerId() +
					", random: " + ((SyncStepMessage)message).getRandom()
					+ ", step: " + ((SyncStepMessage)message).getStep());
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public int getNumberOfNeighbours() {
		return neighbourRepository.size();
	}
}
