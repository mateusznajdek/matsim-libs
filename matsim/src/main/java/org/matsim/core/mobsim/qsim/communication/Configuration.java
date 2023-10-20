package org.matsim.core.mobsim.qsim.communication;

import org.matsim.core.mobsim.qsim.communication.model.WorkerId;
import lombok.*;

@Getter
@Setter
@Builder
public class Configuration {

	/**
	 * Preshared server Ip
	 */
	private String serverIp;

	/**
	 * Preshared server port
	 */
	private int serverPort;

	/**
	 * Count of workers
	 */
	private int workerCount;

	/**
	 * How long simulation should work
	 */
	private int simulationStep;

	/**
	 * Unique worker id
	 */
	private WorkerId workerId;

	/**
	 * Local variable not use in JSON file. This flag will by true only when this worker has server task
	 */
	private transient boolean serverOnThisMachine;


	// TODO this should be merged with final configuration
	public static Configuration getDefault() {
		return Configuration.builder()
			.serverIp("127.0.0.1")
			.serverPort(8081)
			.workerCount(4)
			.simulationStep(10)
			.serverOnThisMachine(false)
			.build();
	}
}
