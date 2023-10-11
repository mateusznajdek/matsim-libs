package communication.service.server;

import communication.model.MessagesTypeEnum;

public interface WorkerSynchronisationService {

	void waitForAllWorkers(MessagesTypeEnum state);

	void handleWorker(MessagesTypeEnum state, String workerId);
}
