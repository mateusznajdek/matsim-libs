package org.matsim.core.mobsim.qsim.communication.model;


public enum MessagesTypeEnum {
	WorkerConnectionMessage,
	ServerInitializationMessage,
	/**
	 * Information from worker when map is parsed and worker is ready for start simulation
	 */
	CompletedInitializationMessage,

	RunSimulationMessage,

	WorkerDisconnectMessage,

	/**
	 * Stop simulation
	 */
	StopSimulationMessage,

	/**
	 * Resume simulation
	 */
	ResumeSimulationMessage,
	/**
	 * Information from worker about finish simulation
	 */
	FinishSimulationMessage,
	ShutDownMessage,
	CarTransferMessage,
}
