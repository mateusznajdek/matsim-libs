package org.matsim.core.mobsim.qsim.communication.service.worker.sync;

import org.matsim.core.mobsim.qsim.communication.model.messages.Message;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;

import java.util.Collection;

public interface StepSynchronizationService {

  void sendSyncMessageToNeighbours();

  void sendFinishMessageToServer();

  void getSyncMessages();

	void collectCarsFromLane(Collection<QVehicle> outGoingVehicles);

//	void sendToNeighbours(Message message);

	int getNumberOfNeighbours();

	void setupNeighboursConnections();

}
