package org.matsim.core.mobsim.qsim.communication.service.worker.sync;

import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;

import java.util.Collection;

public interface StepSynchronizationService {

  void sendSyncMessageToNeighbours();

  void sendFinishMessageToServer();

  void getSyncMessages();

	void collectCarsFromLane(Collection<QVehicle> outGoingVehicles);

	int getNumberOfNeighbours();

	void setupNeighboursConnections();

	void setNetsim(Netsim simulation);

	void setMobsimTimer(MobsimTimer mobsimTimer);
}
