package org.matsim.core.mobsim.qsim.communication.service.worker.sync;

import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;

import java.util.Collection;

public interface StepSynchronizationService {

	void sendSyncMessageToNeighbours();

	void sendFinishMessageToServer();

	void getSyncMessages();

}
