package org.matsim.core.mobsim.qsim.communication.service.server;

import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;

public interface WorkerSynchronisationService {

	void waitForAllWorkers(MessagesTypeEnum state);

	void handleWorker(MessagesTypeEnum state, String workerId);
}
