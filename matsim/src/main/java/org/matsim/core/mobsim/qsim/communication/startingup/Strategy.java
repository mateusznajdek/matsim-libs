package org.matsim.core.mobsim.qsim.communication.startingup;

import org.matsim.core.mobsim.qsim.QSim;

public interface Strategy {
	void executeStrategy(QSim qsim) throws InterruptedException;

}
