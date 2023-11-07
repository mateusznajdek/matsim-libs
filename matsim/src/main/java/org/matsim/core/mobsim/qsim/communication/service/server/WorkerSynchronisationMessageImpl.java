package org.matsim.core.mobsim.qsim.communication.service.server;

import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.core.config.groups.ParallelizationConfigGroup;
import org.matsim.core.mobsim.qsim.communication.model.MessagesTypeEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class WorkerSynchronisationMessageImpl implements WorkerSynchronisationService {
	private final static Logger LOG = LogManager.getLogger(WorkerSynchronisationMessageImpl.class);

	private final Map<MessagesTypeEnum, Set<String>> messageTypeWorkerRepository = new HashMap<>();
	private final ParallelizationConfigGroup configuration;

	@Inject
	public WorkerSynchronisationMessageImpl(ParallelizationConfigGroup configuration) {
		this.configuration = configuration;
	}

	@Override
	public synchronized void waitForAllWorkers(MessagesTypeEnum state) {
		while (messageTypeWorkerRepository.get(state) == null || messageTypeWorkerRepository.get(state).size() < configuration.getWorkerCount()) {
			try {
				wait();
			} catch (InterruptedException e) {
				LOG.error("Error while waiting for state: " + state, e);
			}
		}
		messageTypeWorkerRepository.get(state); // TODO just for debugging remove this line
	}

	@Override
	public synchronized void handleWorker(MessagesTypeEnum state, String workerId) {
		messageTypeWorkerRepository.putIfAbsent(state, new TreeSet<>());
		messageTypeWorkerRepository.get(state)
			.add(workerId);
		LOG.info(String.format("Server receive info -> Worker id: %s has reached the state %s, connection status %d / %d", workerId, state, messageTypeWorkerRepository.get(state).size(), configuration.getWorkerCount()));
		notifyAll();

	}
}
