package org.matsim.core.mobsim.qsim.communication.service.worker;

import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.core.config.groups.ParallelizationConfigGroup;
import org.matsim.core.mobsim.qsim.communication.model.WorkerId;

public class MyWorkerId {
	private final static Logger LOG = LogManager.getLogger(MyWorkerId.class);
	private final ParallelizationConfigGroup config;

	@Inject
	public MyWorkerId(ParallelizationConfigGroup parallelizationConfigGroup) {
		this.config = parallelizationConfigGroup;
	}

	WorkerId id;

	public void create() {
		this.id = new WorkerId(config.getWorkerId().isEmpty() ? WorkerId.random().getId() : config.getWorkerId());
		LOG.info("Created new workerId = " + id.getId());
	}

	public String get() {
		return id.getId();
	}
}
