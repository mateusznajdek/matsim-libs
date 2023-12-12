package org.matsim.core.mobsim.qsim.communication.service.worker.sync;

import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicleImpl;

import java.util.Collection;
import java.util.List;

public interface StepSynchronizationService {

	void sendSyncMessageToNeighbours();

	void sendFinishMessageToServer();

	List<QVehicleImpl> getSyncMessages();

	void collectCarsFromLane(Collection<QVehicle> outGoingVehicles);

	int getNumberOfNeighbours();

	void setupNeighboursConnections();

	void setNetsim(Netsim simulation);

	void setMobsimTimer(MobsimTimer mobsimTimer);
}
