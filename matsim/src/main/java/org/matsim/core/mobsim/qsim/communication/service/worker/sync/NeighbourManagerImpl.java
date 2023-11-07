package org.matsim.core.mobsim.qsim.communication.service.worker.sync;

import com.google.inject.Inject;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.mobsim.qsim.communication.Connection;
import org.matsim.core.mobsim.qsim.communication.service.worker.MyWorkerId;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;

import java.util.*;
import java.util.stream.Collectors;

public class NeighbourManagerImpl implements NeighbourManager {

	private final Network network;
	private final MyWorkerId myWorkerId;
	private final Map<String, Connection> neighbourRepository = new HashMap<>();

	// The key is the unwrapped WorkerId for which we want to accumulate vehicles to send to
	private final Map<String, Collection<QVehicle>> vehiclesToSend = new HashMap<>();

	@Inject
	public NeighbourManagerImpl(Network network, MyWorkerId myWorkerId) {
		this.myWorkerId = myWorkerId;
		this.network = network;
	}

	@Override
	public void collectCarsFromLane(Collection<QVehicle> outGoingVehicles) {
		for (var veh : outGoingVehicles) {
			var _currLinkId = veh.getDriver().getCurrentLinkId();
			String partition = String.valueOf(network.getLinks().get(_currLinkId).getAttributes().getAttribute("partition"));
//				collect all vehicles going to each worker and send them in single iteration
			vehiclesToSend.putIfAbsent(partition, new ArrayList<>());
			vehiclesToSend.get(partition).add(veh);
//			System.out.println("dupa");
		}
	}

	@Override
	public Set<Integer> getMyNeighboursIds() {
		return network.getLinks().values().stream()
			.filter(l -> l.getFromNode().getAttributes().getAttribute("partition").equals(myWorkerId) ||
				l.getToNode().getAttributes().getAttribute("partition").equals(myWorkerId))
			.filter(l -> !l.getToNode().getAttributes().getAttribute("partition").equals(l.getFromNode().getAttributes().getAttribute("partition")))
			.map(l -> {
				if (!l.getToNode().getAttributes().getAttribute("partition").equals(myWorkerId)) {
					return (Integer) l.getToNode().getAttributes().getAttribute("partition");
				} else if (l.getFromNode().getAttributes().getAttribute("partition").equals(myWorkerId)) {
					return (Integer) l.getFromNode().getAttributes().getAttribute("partition");
				} else throw new RuntimeException("Whole world is destroyed!");
			})
			.collect(Collectors.toSet());
	}
}
