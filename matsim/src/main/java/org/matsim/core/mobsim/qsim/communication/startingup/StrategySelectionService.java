package org.matsim.core.mobsim.qsim.communication.startingup;

import com.google.inject.Inject;
import org.matsim.core.config.groups.ParallelizationConfigGroup;
import org.matsim.core.mobsim.qsim.QSim;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;

public class StrategySelectionService {

	public static final String SERVER_LOCK = "serverLock.txt";

	private final WorkerStrategyService workerStrategyService;
	private final ServerStrategyService serverStrategyService;

	private final ParallelizationConfigGroup configuration;

	@Inject
	public StrategySelectionService(WorkerStrategyService workerStrategyService,
									ServerStrategyService serverStrategyService,
									ParallelizationConfigGroup configuration) {
		this.workerStrategyService = workerStrategyService;
		this.serverStrategyService = serverStrategyService;
		this.configuration = configuration;
	}

	public void selectModeAndStartSimulation(QSim qsim) {
		init();

		try {
//			if (configuration.isTestMode()) {
//				singleWorkStrategyService.executeStrategy();
//			} else
			if (canWorkAsServer() && !isServerRunning()) {
				serverStrategyService.executeStrategy(qsim);
			} else {
				workerStrategyService.executeStrategy(qsim);
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
		if (configuration.getServerOnThisMachine()) {
			File serverLock = new File(SERVER_LOCK);
			serverLock.deleteOnExit();
		}
	}
}
