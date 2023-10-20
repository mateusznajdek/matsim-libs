package org.matsim.core.mobsim.qsim.communication.startingup;

import com.google.inject.Inject;
import org.matsim.core.mobsim.qsim.communication.Configuration;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;

public class StrategySelectionService {

	public static final String SERVER_LOCK = "serverLock.txt";

	private final WorkerStrategyService workerStrategyService;
	private final ServerStrategyService serverStrategyService;

	private final Configuration configuration;

	@Inject
	public StrategySelectionService(WorkerStrategyService workerStrategyService,
									ServerStrategyService serverStrategyService,
									Configuration configuration) {
		this.workerStrategyService = workerStrategyService;
		this.serverStrategyService = serverStrategyService;
		this.configuration = configuration;
	}

	public void selectModeAndStartSimulation() {
		init();

		try {
//			if (configuration.isTestMode()) {
//				singleWorkStrategyService.executeStrategy();
//			} else
			if (canWorkAsServer() && !isServerRunning()) {
				serverStrategyService.executeStrategy();
			} else {
				workerStrategyService.executeStrategy();
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	private void init() {
		workerStrategyService.init();
	}

	private boolean canWorkAsServer() {
//		if (HiPUTS.globalInitArgs.size() < 2) {
//			return true;
//		}
//		return HiPUTS.globalInitArgs.get(1).equals("SERVER");
		return true; // TODO return true for now, but this should check condition of input params
	}

	private boolean isServerRunning() throws IOException {
		File serverLock = new File(SERVER_LOCK);
		if (serverLock.createNewFile()) {
			configuration.setServerOnThisMachine(true);
			return false;
		}
		return true;
	}

	@PreDestroy
	public void onExit() {
		if (configuration.isServerOnThisMachine()) {
			File serverLock = new File(SERVER_LOCK);
			serverLock.deleteOnExit();
		}
	}
}
