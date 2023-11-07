package org.matsim.core.mobsim.qsim.communication.service.worker.sync;

public interface StepSynchronizationService {

  void sendSyncMessageToNeighbours();

  void sendFinishMessageToServer();

  void getSyncMessages();

}
