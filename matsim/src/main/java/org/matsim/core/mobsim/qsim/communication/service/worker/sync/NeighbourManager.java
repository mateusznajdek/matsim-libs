package org.matsim.core.mobsim.qsim.communication.service.worker.sync;

import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;

import java.util.Collection;
import java.util.Set;

public interface NeighbourManager {

	void collectCarsFromLane(Collection<QVehicle> outGoingVehicles);

	Set<Integer> getMyNeighboursIds();
}
