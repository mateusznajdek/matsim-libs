package playground.wrashid.DES;

import org.matsim.gbl.Gbl;

public class Scheduler {
	private double simTime = 0;
	private MessageQueue queue = new MessageQueue();
	private double simulationStartTime = System.currentTimeMillis();
	private double hourlyLogTime = 3600;

	public void schedule(Message m) {
		queue.putMessage(m);
	}

	public void unschedule(Message m) {
		queue.removeMessage(m);
	}

	public void startSimulation() {

		Message m;

		while (!queue.isEmpty()
				&& simTime < SimulationParameters.maxSimulationLength) {
			m = queue.getNextMessage();
			simTime = m.getMessageArrivalTime();
			m.processEvent();
			m.handleMessage();
			printLog();
		}
	}

	public double getSimTime() {
		return simTime;
	}

	private void printLog() {

		// print output each hour
		if (simTime / hourlyLogTime > 1) {
			hourlyLogTime = simTime + 3600;
			System.out.print("Simulation at " + simTime / 3600 + "[h]; ");
			System.out
					.println("s/r:"
							+ simTime
							/ (System.currentTimeMillis() - simulationStartTime)
							* 1000);
			Gbl.printMemoryUsage();
		}
	}

}
